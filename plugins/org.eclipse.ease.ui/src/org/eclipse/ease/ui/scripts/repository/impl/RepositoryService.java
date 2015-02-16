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
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.IHeaderParser;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.preferences.PreferencesHelper;
import org.eclipse.ease.ui.repository.IRepositoryFactory;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.repository.IScriptLocation;
import org.eclipse.ease.ui.repository.IStorage;
import org.eclipse.ease.ui.repository.impl.RepositoryFactoryImpl;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScriptListener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.PlatformUI;

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
				Logger.logError("Could not store script repositories");
			}

			return Status.OK_STATUS;
		}
	};

	private final ListenerList fListeners = new ListenerList();

	/**
	 * Initialize the repository service.
	 */
	private RepositoryService() {
		RepositoryFactoryImpl.init();

		// load stored data
		final IPath path = Activator.getDefault().getStateLocation().append(CACHE_FILE_NAME);
		final File file = path.toFile();
		if ((file != null) && (file.exists())) {

			final ResourceSet resourceSet = new ResourceSetImpl();
			final Resource resource = resourceSet.createResource(URI.createURI(file.toURI().toString()));
			try {
				resource.load(null);
				fRepository = (IStorage) resource.getContents().get(0);

			} catch (final IOException e) {
				// we could not load an existing model, but we will refresh it in a second
			}
		}

		// create repository if empty
		long updateDelay = 0;
		if (fRepository == null) {
			// create an empty repository to start with
			fRepository = IRepositoryFactory.eINSTANCE.createStorage();

		} else {
			// wait for the workspace to be loaded before updating, we have cached data anyway
			updateDelay = DEFAULT_DELAY;
		}

		// detect script parameter changes and fire change events
		// fRepository.eAdapters().add(new EContentAdapter() {
		// @Override
		// public void notifyChanged(final Notification notification) {
		// if (IScript.class.isAssignableFrom(notification.getNotifier().getClass())) {
		// if (IRepositoryPackage.SCRIPT__SCRIPT_PARAMETERS == notification.getFeatureID(EReferenceImpl.class)) {
		// // script parameter changed. triggered by a change to the source file
		//
		// final Object oldValue = notification.getOldValue();
		// final Object newValue = notification.getNewValue();
		//
		// final IScript script = (IScript) notification.getNotifier();
		// if (newValue instanceof ParameterMapImpl) {
		// if (script.getUserParameters().containsKey(((ParameterMapImpl) newValue).getKey()))
		// // we have a user override, do nothing
		// return;
		// } else if (oldValue instanceof ParameterMapImpl) {
		// if (script.getUserParameters().containsKey(((ParameterMapImpl) oldValue).getKey()))
		// // we have a user override, do nothing
		// return;
		// }
		//
		// fireScriptEvent(new ScriptEvent((IScript) notification.getNotifier(), ScriptEvent.PARAMETER_CHANGE, new ParameterDelta(oldValue,
		// newValue)));
		// }
		// }
		//
		// super.notifyChanged(notification);
		// }
		//
		// });

		// apply UI integrations
		new UIIntegrationJob(this);

		// update repository
		fUpdateJob = new UpdateRepositoryJob(this);
		fUpdateJob.schedule(updateDelay);

		// add workspace resource listener in case we have workspace locations registered
		for (final IScriptLocation location : getLocations()) {
			if (location.getLocation().startsWith("workspace://")) {
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
				break;
			}
		}
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

	private void fireScriptEvent(final ScriptEvent event) {
		for (final Object listener : fListeners.getListeners())
			((IScriptListener) listener).notify(event);
	}

	@Override
	public Collection<IScript> getScripts() {
		return Collections.unmodifiableCollection(fRepository.getScripts());
	}

	@Override
	public void addScriptListener(final IScriptListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeScriptListener(final IScriptListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public Collection<IScriptLocation> getLocations() {
		return Collections.unmodifiableCollection(fRepository.getEntries());
	}

	// void updateScript(final IScript script, final Map<String, String> parameters) {
	//
	// // store current parameters
	// Map<String, String> oldParameters = new HashMap<String, String>(script.getParameters());
	//
	// script.getScriptParameters().clear();
	// script.getScriptParameters().putAll(parameters);
	//
	// // get new parameters (merged with user parameters)
	// Map<String, String> newParameters = new HashMap<String, String>(script.getParameters());
	//
	// // now look for changes
	// if (!oldParameters.equals(newParameters)) {
	// // some parameters changed
	// notifyListeners(new ScriptRepositoryEvent(script, ScriptRepositoryEvent.PARAMETER_CHANGE, new ParameterDelta(oldParameters, newParameters)));
	// }
	//
	// script.setUpdatePending(false);
	// }

	// @Override
	// public IScriptLocation getDefaultLocation() {
	// for (IScriptLocation entry : getLocations())
	// if (entry.isDefault())
	// return entry;
	//
	// return null;
	// }

	@Override
	public void updateLocation(final IScriptLocation entry, final String location, final long lastChanged) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		final ScriptType scriptType = scriptService.getScriptType(ResourceTools.toAbsoluteLocation(location, null));

		IScript script = getScriptByLocation(location);
		if (script == null) {
			// new script detected
			script = IRepositoryFactory.eINSTANCE.createScript();
			script.setEntry(entry);
			script.setLocation(location);

			entry.getScripts().add(script);
			fireScriptEvent(new ScriptEvent(script, ScriptEvent.ADD, null));

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

		if (!oldParameters.equals(newParameters))
			fireScriptEvent(new ScriptEvent(script, ScriptEvent.PARAMETER_CHANGE, new ParameterDelta(oldParameters, newParameters)));

		// script is up to date
		script.setTimestamp(lastChanged);
		script.setUpdatePending(false);
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
			final IHeaderParser parser = type.getHeaderParser();
			if (parser != null)
				return parser.parse(stream);
		}

		return Collections.emptyMap();
	}

	void removeScript(final IScript script) {
		script.getEntry().getScripts().remove(script);
		fireScriptEvent(new ScriptEvent(script, ScriptEvent.DELETE, null));
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

				for (final IScript script : entry.getScripts())
					removeScript(script);

				save();

				// no need to traverse further as locationURIs need to be unique
				break;
			}
		}

		PreferencesHelper.removeLocation(locationURI);
	}
}
