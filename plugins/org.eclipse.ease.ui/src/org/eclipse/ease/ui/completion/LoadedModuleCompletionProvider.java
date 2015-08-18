/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Martin Kloesch - extension to parsing logic
 *******************************************************************************/
package org.eclipse.ease.ui.completion;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;

public class LoadedModuleCompletionProvider extends ModuleCompletionBase {

	private static final Pattern LOAD_MODULE_PATTERN = Pattern.compile("loadModule\\([\"'](.*)[\"']\\)");

	/**
	 * List of all currently loaded modules.
	 */
	private final Collection<ModuleDefinition> fLoadedModules = new HashSet<ModuleDefinition>();
	private final static Collection<ModuleDefinition> fStaticLoadedModules = new HashSet<ModuleDefinition>();

	/**
	 * Constructor only initializes members and adds default modules.
	 */
	public LoadedModuleCompletionProvider() {
		// add environment module
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		fLoadedModules.add(scriptService.getAvailableModules().get(EnvironmentModule.MODULE_NAME));
		fStaticLoadedModules.add(scriptService.getAvailableModules().get(EnvironmentModule.MODULE_NAME));
	}
	
	/*
	 *  Return the list of loaded modules
	 */
	public static Collection<ModuleDefinition> getStaticLoadedModules() {
		return fStaticLoadedModules;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.IContextProvider#addCode(String)
	 */
	@Override
	public void addCode(final String code) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		Collection<String> modules = getModuleNames(code);
		while (!modules.isEmpty()) {
			String candidate = modules.iterator().next();
			modules.remove(candidate);

			String fullName = ModuleHelper.resolveName(candidate);
			ModuleDefinition definition = scriptService.getAvailableModules().get(fullName);
			if (definition != null) {
				fLoadedModules.add(definition);
				fStaticLoadedModules.add(definition);

				// add dependencies to list
				for (String moduleID : definition.getDependencies())
					modules.add(scriptService.getModuleDefinition(moduleID).getPath().toString());
			}
		}
	}

	/**
	 * Getter method for available modules.
	 * 
	 * @return all currently loaded modules.
	 */
	protected Collection<ModuleDefinition> getModules() {
		return fLoadedModules;
	}

	/**
	 * Extract names of loaded modules within provided code. Will only detect string literals, so if the loadModule parameter is not a single string, extraction
	 * will fail.
	 *
	 * @param code
	 *            code to parse
	 * @return collection of module names
	 */
	private Collection<String> getModuleNames(final String code) {
		Collection<String> modules = new HashSet<String>();

		Matcher matcher = LOAD_MODULE_PATTERN.matcher(code);
		while (matcher.find())
			modules.add(matcher.group(1));

		return modules;
	}
}