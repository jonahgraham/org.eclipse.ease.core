/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.ease.ui.scripts.preferences.PreferencesHelper;
import org.eclipse.ease.ui.scripts.repository.IRepositoryFactory;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IScriptLocation;
import org.eclipse.ease.ui.scripts.repository.IStorage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.EventHandler;

public class RepositoryService implements IRepositoryService, IResourceChangeListener {

	// TODO find a nice delay value here
	static final long UPDATE_URI_INTERVAL = 1000; // update daily
	// TODO find a nice delay value here
	static final long UPDATE_SCRIPT_INTERVAL = 1000; // update hourly

	private static RepositoryService fInstance;

	private static final String CACHE_FILE_NAME = "script.repository";

	// TODO find a nice delay value here
	private static final long DEFAULT_DELAY = 60 * 1000; // 1 minute
	public static final long UPDATE_STREAM_INTERVAL = 0;
	private static final String EXTENSION_KEYWORD_ID = "org.eclipse.ease.ui.scripts.keyword";
	private static final String EXTENSION_KEYWORD_HANDLER = "handler";
	private static final String EXTENSION_KEYWORD_HANDLER_CLASS = "class";
	private static final String EXTENSION_KEYWORD_HANDLER_KEYWORDS = "keywords";

	/**
	 * Get the repository service singleton.
	 *
	 * @return repository service
	 */
	public static RepositoryService getInstance() {
		if (fInstance == null)
			fInstance = new RepositoryService();

		return fInstance;
	}

	private IStorage fRepository = null;

	private final UpdateRepositoryJob fUpdateJob;

	IEventBroker fEventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);

	private final Job fSaveJob = new Job("Save Script Repositories") {

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			final IPath path = Activator.getDefault().getStateLocation().append(CACHE_FILE_NAME);
			final File file = path.toFile();

			// Obtain a new resource set
			final ResourceSet resSet = new ResourceSetImpl();
			final Resource resource = resSet.createResource(URI.createFileURI(file.getAbsolutePath()));
			resource.getContents().add(fRepository);
			try {
				resource.save(Collections.emptyMap());
			} catch (final IOException e) {
				Logger.error(Activator.PLUGIN_ID, "Could not store script repositories", e);
			}

			return Status.OK_STATUS;
		}
	};

	/**
	 * Initialize the repository service.
	 */
	private RepositoryService() {

		Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Starting repository service");
		RepositoryFactoryImpl.init();

		startKeywordHandlers();

		// load stored data
		final IPath path = Activator.getDefault().getStateLocation().append(CACHE_FILE_NAME);
		final File file = path.toFile();
		if ((file != null) && (file.exists())) {

			final ResourceSet resourceSet = new ResourceSetImpl();
			final Resource resource = resourceSet.createResource(URI.createURI(file.toURI().toString()));
			try {
				resource.load(null);
				fRepository = (IStorage) resource.getContents().get(0);

				// push all updates to the event bus
				for (IScript script : fRepository.getScripts()) {
					for (Entry<String, String> entry : script.getParameters().entrySet())
						fireKeywordEvent(script, entry.getKey(), entry.getValue(), "");
				}

				Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Loaded cached scripts");

			} catch (final IOException e) {
				// we could not load an existing model, but we will refresh it in a second
			}
		}

		// create repository if empty
		long updateDelay = 0;
		if (fRepository == null) {
			// create an empty repository to start with
			fRepository = IRepositoryFactory.eINSTANCE.createStorage();
			Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Created clean script repository");

		} else if (!fRepository.getScripts().isEmpty()) {
			// wait for the workspace to be loaded before updating, we have cached data anyway
			updateDelay = DEFAULT_DELAY;
		}

		// verify that cached repositories match preference settings
		if (!equals(fRepository.getEntries(), PreferencesHelper.getLocations())) {
			Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Cached scripts are dirty, cleanup");
			fRepository.getEntries().clear();
			fRepository.getEntries().addAll(PreferencesHelper.getLocations());
			save();
		}

		// update repository
		fUpdateJob = new UpdateRepositoryJob(this);

		for (final IScriptLocation location : getLocations())
			location.setUpdatePending(true);

		fUpdateJob.schedule(updateDelay);

		// add workspace resource listener in case we have workspace locations registered
		for (final IScriptLocation location : getLocations()) {
			if (location.getLocation().startsWith("workspace://")) {
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
				break;
			}
		}
	}

	/**
	 *
	 */
	private void startKeywordHandlers() {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_KEYWORD_ID);

		for (final IConfigurationElement e : config) {

			if (EXTENSION_KEYWORD_HANDLER.equals(e.getName())) {
				try {
					Object listener = e.createExecutableExtension(EXTENSION_KEYWORD_HANDLER_CLASS);
					if (listener instanceof EventHandler) {
						String keywords = e.getAttribute(EXTENSION_KEYWORD_HANDLER_KEYWORDS);
						for (String keyword : keywords.split(","))
							fEventBroker.subscribe(BROKER_CHANNEL_SCRIPT_KEYWORDS + keyword, (EventHandler) listener);

					} else
						Logger.error(Activator.PLUGIN_ID, "Invalid keyword handler detected: " + e.getAttribute("id"));

				} catch (Exception e1) {
					Logger.error(Activator.PLUGIN_ID, "Invalid keyword handler detected: " + e.getAttribute("id"), e1);
				}
			}
		}
	}

	/**
	 * Post a keyword change to the event bus.
	 *
	 * @param script
	 *            script that created the event
	 * @param key
	 *            keyword that changed
	 * @param value
	 *            value for that keyword
	 * @param oldValue
	 *            previous value for that keyword
	 */
	private void fireKeywordEvent(final IScript script, final String key, final String value, final String oldValue) {
		Object service = PlatformUI.getWorkbench().getService(IEventBroker.class);
		if (service instanceof IEventBroker) {
			HashMap<String, Object> eventData = new HashMap<String, Object>();
			eventData.put("script", script);
			eventData.put("keyword", key);
			eventData.put("value", value);
			eventData.put("oldValue", oldValue);
			((IEventBroker) service).post(BROKER_CHANNEL_SCRIPT_KEYWORDS + key, eventData);
		}
	}

	/**
	 * Compare 2 collections of {@link IScriptLocation}s for equality.
	 *
	 * @param first
	 *            first collection
	 * @param second
	 *            second collection
	 * @return <code>true</code> when equal
	 */
	private boolean equals(final Collection<IScriptLocation> first, final Collection<IScriptLocation> second) {
		if (first.size() != second.size())
			return false;

		for (IScriptLocation firstEntry : first) {
			boolean found = false;
			for (IScriptLocation secondEntry : second) {
				if (firstEntry.getLocation().equals(secondEntry.getLocation())) {
					found = true;
					break;
				}
			}

			if (!found)
				return false;
		}

		return true;
	}

	@Override
	public void update(final boolean force) {

		if (force) {
			for (final IScript script : getScripts())
				script.setTimestamp(0);
		}

		fUpdateJob.update();
	}

	@Override
	public IScript getScript(final String name) {
		for (final IScript script : fRepository.getScripts()) {
			if (name.equals(script.getPath().toString()))
				return script;
		}

		return null;
	}

	/**
	 * Trigger delayed save action. Store the repository to disk after a given delay.
	 */
	void save() {
		fSaveJob.cancel();
		fSaveJob.schedule(500);
	}

	@Override
	public Collection<IScript> getScripts() {
		return Collections.unmodifiableCollection(fRepository.getScripts());
	}

	@Override
	public Collection<IScriptLocation> getLocations() {
		return Collections.unmodifiableCollection(fRepository.getEntries());
	}

	@Override
	public void updateLocation(final IScriptLocation entry, final String location, final long lastChanged) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);

		IScript script = getScriptByLocation(location);
		final ScriptType scriptType = scriptService.getScriptType(location);
		if (scriptType != null) {

			if (script == null) {
				// new script detected
				script = IRepositoryFactory.eINSTANCE.createScript();
				script.setEntry(entry);
				script.setLocation(location);

				entry.getScripts().add(script);
				HashMap<String, Object> eventData = new HashMap<String, Object>();
				eventData.put("script", script);
				fEventBroker.post(BROKER_CHANNEL_SCRIPTS_NEW, eventData);

			} else if (script.getTimestamp() == lastChanged) {
				// no update needed
				script.setUpdatePending(false);
				return;
			}

			// update script parameters
			final Map<String, String> oldParameters = script.getParameters();

			final Map<String, String> parameters = extractParameters(scriptType, script.getInputStream());
			script.getScriptParameters().clear();
			script.getScriptParameters().putAll(parameters);

			final Map<String, String> newParameters = script.getParameters();

			if (!oldParameters.equals(newParameters)) {
				// changed script parameters, push events

				// find deleted parameters
				for (String oldParameter : oldParameters.keySet()) {
					if (!newParameters.containsKey(oldParameter))
						fireKeywordEvent(script, oldParameter, "", oldParameters.get(oldParameter));
				}

				// find changed/new parameters
				for (Entry<String, String> newEntry : newParameters.entrySet()) {
					if (!newEntry.getValue().equals(oldParameters.get(newEntry.getKey())))
						fireKeywordEvent(script, newEntry.getKey(), newEntry.getValue(), oldParameters.get(newEntry.getKey()));
				}
			}

			// script is up to date
			script.setTimestamp(lastChanged);
			script.setUpdatePending(false);

		} else if (script != null)
			// we have a script in the cache which is no longer supported by any engine
			removeScript(script);
	}

	private IScript getScriptByLocation(final String location) {
		for (final IScript script : getScripts()) {
			if (script.getLocation().equals(location))
				return script;
		}

		return null;
	}

	private static Map<String, String> extractParameters(final ScriptType type, final InputStream stream) {
		if (type != null) {
			final ICodeParser parser = type.getCodeParser();
			if (parser != null)
				return parser.parse(stream);
		}

		return Collections.emptyMap();
	}

	void removeScript(final IScript script) {
		script.getEntry().getScripts().remove(script);

		// unregister script keywords
		for (Entry<String, String> entry : script.getParameters().entrySet())
			fireKeywordEvent(script, entry.getKey(), "", entry.getValue());

		HashMap<String, Object> eventData = new HashMap<String, Object>();
		eventData.put("script", script);
		fEventBroker.post(BROKER_CHANNEL_SCRIPTS_REMOVED, eventData);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {

				@Override
				public boolean visit(final IResourceDelta delta) throws CoreException {
					// TODO currently we update the whole location on a simple change, just focus on the changed files in future
					final IResource resource = delta.getResource();
					final String location = "workspace:/" + resource.getFullPath();
					for (final IScriptLocation entry : getLocations()) {
						if (entry.getLocation().equals(location)) {
							// TODO currently updates a whole repository for eg a small file content change
							fUpdateJob.update(entry);
							return false;
						}
					}

					return true;
				}
			});
		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addLocation(final String locationURI, final boolean defaultLocation, final boolean recursive) {
		final IScriptLocation entry = IRepositoryFactory.eINSTANCE.createScriptLocation();
		entry.setLocation(locationURI);
		entry.setRecursive(recursive);
		entry.setDefault(defaultLocation);

		for (final IScriptLocation location : new HashSet<IScriptLocation>(getLocations())) {
			if (location.getLocation().equals(locationURI)) {
				// already registered, ev. we need to update defaultLocation/recursive?
				if (!EcoreUtil.equals(location, entry))
					removeLocation(location.getLocation());
				else
					// same location already registered, do not update
					return;
			}
		}

		PreferencesHelper.addLocation(entry);
		Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Script location added: " + locationURI);

		fRepository.getEntries().add(entry);

		if (entry.getLocation().startsWith("workspace://"))
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

		fUpdateJob.update(entry);
	}

	@Override
	public void removeLocation(final String locationURI) {
		for (final IScriptLocation entry : new HashSet<IScriptLocation>(fRepository.getEntries())) {
			if (entry.getLocation().equals(locationURI)) {
				fRepository.getEntries().remove(entry);

				for (final IScript script : new HashSet<IScript>(entry.getScripts()))
					removeScript(script);

				save();
				Logger.trace(Activator.PLUGIN_ID, TRACE_REPOSITORY_SERVICE, Activator.PLUGIN_ID, "Script location removed: " + locationURI);

				// no need to traverse further as locationURIs need to be unique
				break;
			}
		}

		PreferencesHelper.removeLocation(locationURI);
	}
}
