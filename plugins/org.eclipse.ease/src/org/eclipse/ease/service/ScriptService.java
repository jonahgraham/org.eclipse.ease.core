/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.ModuleCategoryDefinition;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class ScriptService implements IScriptService, BundleListener {

	private static final String ENGINE = "engine";

	private static final String ENGINE_ID = "engineID";

	private static final Object EXTENSION_MODULE = "module";

	private static final Object EXTENSION_CATEGORY = "category";

	private static final String EXTENSION_LANGUAGE_ID = "org.eclipse.ease.language";

	private static final String EXTENSION_MODULES_ID = "org.eclipse.ease.modules";

	private static final String EXTENSION_SCRIPTTYPE_ID = "org.eclipse.ease.scriptType";

	private static final String SCRIPTTYPE_NAME = "name";

	private static final String LAUNCH_EXTENSION = "launchExtension";

	private static ScriptService fInstance = null;

	public static IScriptService getService() {
		try {
			return PlatformUI.getWorkbench().getService(IScriptService.class);
		} catch (final IllegalStateException e) {
			// workbench has not been created yet, might be running in headless mode
			return ScriptService.getInstance();
		}
	}

	public synchronized static ScriptService getInstance() {
		if (fInstance == null)
			fInstance = new ScriptService();

		return fInstance;
	}

	private Map<String, ModuleDefinition> fAvailableModules = null;

	private Map<String, EngineDescription> fEngineDescriptions = null;

	private Map<String, ScriptType> fScriptTypes = null;

	private Map<String, ModuleCategoryDefinition> fAvailableModuleCategories = null;

	private ScriptService() {
		Activator.getDefault().getContext().addBundleListener(this);
	}

	@Override
	public EngineDescription getEngineByID(final String engineID) {
		return getEngineDescriptions().get(engineID);
	}

	@Override
	public synchronized Map<String, ModuleDefinition> getAvailableModules() {
		if (fAvailableModules == null) {
			fAvailableModules = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_MODULES_ID);
			for (final IConfigurationElement e : config) {
				if (e.getName().equals(EXTENSION_MODULE)) {
					// module extension detected
					final ModuleDefinition definition = new ModuleDefinition(e);
					if (definition.getModuleClass() != null)
						fAvailableModules.put(definition.getPath().toString(), definition);
					else
						Logger.warning(Activator.PLUGIN_ID,
								"Module <" + definition.getName() + "> in plugin <" + definition.getBundleID() + "> could not be located!");
				}
			}
		}
		return fAvailableModules;
	}

	@Override
	public Collection<EngineDescription> getEngines() {
		return getEngineDescriptions().values();
	}

	@Override
	public List<EngineDescription> getEngines(final String scriptType) {
		final List<EngineDescription> result = new ArrayList<>();

		for (final EngineDescription description : getEngines()) {
			if (description.supports(scriptType))
				result.add(description);
		}

		// sort by priority
		Collections.sort(result, new Comparator<EngineDescription>() {

			@Override
			public int compare(final EngineDescription o1, final EngineDescription o2) {
				return o2.getPriority() - o1.getPriority();
			}
		});

		return result;
	}

	private Map<String, EngineDescription> getEngineDescriptions() {
		if (fEngineDescriptions == null) {
			fEngineDescriptions = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_LANGUAGE_ID);

			for (final IConfigurationElement e : config) {
				if (ENGINE.equals(e.getName())) {
					final EngineDescription engine = new EngineDescription(e);
					fEngineDescriptions.put(engine.getID(), engine);
				}
			}
		}
		return fEngineDescriptions;
	}

	@Override
	public Collection<IScriptEngineLaunchExtension> getLaunchExtensions(final String engineID) {
		final Collection<IScriptEngineLaunchExtension> extensions = new HashSet<>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_LANGUAGE_ID);

		for (final IConfigurationElement e : config) {
			try {
				if (LAUNCH_EXTENSION.equals(e.getName())) {
					if (e.getAttribute(ENGINE_ID).equals(engineID)) {
						final Object extension = e.createExecutableExtension("class");
						if (extension instanceof IScriptEngineLaunchExtension)
							extensions.add((IScriptEngineLaunchExtension) extension);
					}
				}
			} catch (final InvalidRegistryObjectException e1) {
			} catch (final CoreException e1) {
			}
		}

		return extensions;
	}

	@Override
	public Map<String, ScriptType> getAvailableScriptTypes() {
		if (fScriptTypes == null) {
			fScriptTypes = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_SCRIPTTYPE_ID);

			for (final IConfigurationElement e : config) {
				if ("scriptType".equals(e.getName()))
					fScriptTypes.put(e.getAttribute(SCRIPTTYPE_NAME), new ScriptType(e));
			}
		}

		return fScriptTypes;
	}

	@Override
	public ScriptType getScriptType(final String location) {
		final Object resource = ResourceTools.getResource(location);
		try {
			if (resource instanceof IFile) {
				// try to resolve by content type
				final IContentDescription description = ((IFile) resource).getContentDescription();
				if (description != null) {
					final IContentType contentType = description.getContentType();

					for (final ScriptType scriptType : getAvailableScriptTypes().values()) {
						if (scriptType.getContentTypes().contains(contentType.getId()))
							return scriptType;
					}
				}
			}
		} catch (final CoreException e) {
			// could not retrieve content type, continue using file extension
		}

		// try to resolve by extension
		final int pos = location.lastIndexOf('.');
		if (pos != -1) {
			final String extension = location.substring(pos + 1);

			// FIXME search all extensions, not only default one
			for (final ScriptType scriptType : getAvailableScriptTypes().values()) {
				if (scriptType.getDefaultExtension().equalsIgnoreCase(extension))
					return scriptType;
			}
		}

		return null;
	}

	@Override
	public EngineDescription getEngine(final String scriptType) {
		final List<EngineDescription> engines = getEngines(scriptType);
		if (!engines.isEmpty())
			return engines.get(0);

		return null;
	}

	@Override
	public Map<String, ModuleCategoryDefinition> getAvailableModuleCategories() {
		if (fAvailableModuleCategories == null) {
			fAvailableModuleCategories = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_MODULES_ID);
			for (final IConfigurationElement e : config) {
				if (e.getName().equals(EXTENSION_CATEGORY)) {
					// module category detected
					final ModuleCategoryDefinition definition = new ModuleCategoryDefinition(e);
					fAvailableModuleCategories.put(definition.getId(), definition);
				}
			}
		}
		return fAvailableModuleCategories;
	}

	@Override
	public ModuleDefinition getModuleDefinition(final String moduleId) {
		for (final ModuleDefinition definition : getAvailableModules().values()) {
			if (definition.getId().equals(moduleId))
				return definition;
		}

		return null;
	}

	/**
	 * Get the default {@link ICodeFactory} for a given script engine.
	 *
	 * @param engine
	 *            script engine to look up
	 * @return code factory or <code>null</code>
	 */
	public static ICodeFactory getCodeFactory(final IScriptEngine engine) {
		final EngineDescription description = engine.getDescription();
		if (description != null) {
			final List<ScriptType> scriptTypes = description.getSupportedScriptTypes();
			if (!scriptTypes.isEmpty())
				return scriptTypes.get(0).getCodeFactory();
		}

		return null;
	}

	/**
	 * Get the default {@link ICodeParser} for a given script engine.
	 *
	 * @param engine
	 *            script engine to look up
	 * @return code factory or <code>null</code>
	 */
	public static ICodeParser getCodeParser(final IScriptEngine engine) {
		final EngineDescription description = engine.getDescription();
		if (description != null) {
			final List<ScriptType> scriptTypes = description.getSupportedScriptTypes();
			if (!scriptTypes.isEmpty())
				return scriptTypes.get(0).getCodeParser();
		}

		return null;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		final int type = event.getType();
		if ((type == BundleEvent.RESOLVED) || (type == BundleEvent.STARTED) || (type == BundleEvent.STOPPED) || (type == BundleEvent.UPDATED)) {
			// clear cached entries
			fAvailableModules = null;
			fEngineDescriptions = null;
			fScriptTypes = null;
			fAvailableModuleCategories = null;
		}
	}
}