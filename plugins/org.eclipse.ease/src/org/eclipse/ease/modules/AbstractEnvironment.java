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
package org.eclipse.ease.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractEnvironment extends AbstractScriptModule implements IEnvironment {

	/** Stores ordering of wrapped elements. */
	private final List<Object> fModules = new ArrayList<Object>();

	/** Stores beautified names of loaded modules. */
	private final Map<String, Object> fModuleNames = new HashMap<String, Object>();

	private final ListenerList fModuleListeners = new ListenerList();

	/**
	 * Load a module. Loading a module generally enhances the script environment with new functions and variables. If a module was already loaded before, it
	 * gets refreshed and moved to the top of the module stack. When a module is loaded, all its dependencies are loaded too. So loading one module might change
	 * the whole module stack.
	 *
	 * @param name
	 *            name of module to load
	 * @return loaded module instance
	 */
	@Override
	@WrapToScript
	public final Object loadModule(final String identifier) {
		// resolve identifier
		final String moduleName = ModuleHelper.resolveName(identifier);

		Object module = getModule(moduleName);
		if (module == null) {
			// not loaded yet
			final IScriptService scriptService = ScriptService.getService();
			final Map<String, ModuleDefinition> availableModules = scriptService.getAvailableModules();

			final ModuleDefinition definition = availableModules.get(moduleName);
			if (definition != null) {
				// module exists

				// load dependencies; always load to bring dependencies on top of modules stack
				for (final String dependencyId : definition.getDependencies()) {
					final ModuleDefinition requiredModule = scriptService.getModuleDefinition(dependencyId);

					if (requiredModule == null)
						throw new RuntimeException("Could not resolve module dependency \"" + dependencyId + "\"");

					try {
						loadModule(requiredModule.getPath().toString());
					} catch (final RuntimeException e) {
						throw new RuntimeException("Could not load module dependency \"" + requiredModule.getPath().toString() + "\"", e);
					}
				}

				module = definition.createModuleInstance();
				if (module instanceof IScriptModule)
					((IScriptModule) module).initialize(getScriptEngine(), this);

				fModuleNames.put(moduleName, module);

				// scripts changing functions force reloading of the whole module stack
				if (module instanceof IScriptFunctionModifier) {
					final List<Object> reverseList = new ArrayList<Object>(fModules);
					Collections.reverse(reverseList);

					for (final Object loadedModule : reverseList)
						wrap(loadedModule);
				}
			} else
				throw new RuntimeException("Could not find module \"" + identifier + "\"");
		}

		// first take care that module is tracked as it might modify itself implementing IScriptFunctionModifier
		// move module up to first position
		fModules.remove(module);
		fModules.add(0, module);

		// create function wrappers
		wrap(module);

		return module;
	}

	@Override
	public void initialize(IScriptEngine engine, IEnvironment environment) {
		super.initialize(engine, environment);

		fModules.add(this);
		fModuleNames.put(EnvironmentModule.MODULE_NAME, this);
	}

	/**
	 * List all available (visible) modules. Returns a list of visible modules. Loaded modules are indicated.
	 *
	 * @return string containing module information
	 */
	@WrapToScript
	public final String listModules() {

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		final Collection<ModuleDefinition> modules = scriptService.getAvailableModules().values();

		final StringBuilder output = new StringBuilder();

		// add header
		output.append("available modules\n=================\n\n");

		// add modules
		for (final ModuleDefinition module : modules) {

			if (module.isVisible()) {
				output.append('\t');

				output.append(module.getPath().toString());
				if (getModule(module.getPath().toString()) != null)
					output.append(" [LOADED]");

				output.append('\n');
			}
		}

		// write to default output
		print(output);

		return output.toString();
	}

	/**
	 * Resolves a loaded module and returns the Java instance. Will only query previously loaded modules.
	 *
	 * @param name
	 *            name of the module to resolve
	 * @return resolved module instance or <code>null</code>
	 */
	@Override
	@WrapToScript
	public final Object getModule(final String name) {
		return fModuleNames.get(name);
	}

	/**
	 * Resolves a loaded module by its class.
	 *
	 * @param clazz
	 *            module class to look resolve
	 * @return resolved module instance or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object, U extends Class<T>> T getModule(final U clazz) {
		for (final Object module : getModules()) {
			if (clazz.isAssignableFrom(module.getClass()))
				return (T) module;
		}

		return null;
	}

	@Override
	public List<Object> getModules() {
		return Collections.unmodifiableList(fModules);
	}

	@Override
	@WrapToScript
	public final void print(final @ScriptParameter(defaultValue = "") Object text) {
		getScriptEngine().getOutputStream().println(text);
	}

	@Override
	public void addModuleListener(final IModuleListener listener) {
		fModuleListeners.add(listener);
	}

	@Override
	public void removeModuleListener(final IModuleListener listener) {
		fModuleListeners.remove(listener);
	}

	protected void fireModuleEvent(final Object module, final int type) {
		for (final Object listener : fModuleListeners.getListeners())
			((IModuleListener) listener).notifyModule(module, type);
	}

	public static IEnvironment getEnvironment(IScriptEngine engine) {
		for (final Object variable : engine.getVariables().values()) {
			if (variable instanceof IEnvironment)
				return (IEnvironment) variable;
		}

		return null;
	}
}
