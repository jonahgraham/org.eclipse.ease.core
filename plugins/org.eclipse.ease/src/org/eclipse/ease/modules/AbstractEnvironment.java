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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractEnvironment extends AbstractScriptModule implements IEnvironment {

	/** Stores ordering of wrapped elements. */
	private final List<Object> fModules = new ArrayList<>();

	/** Stores beautified names of loaded modules. */
	private final Map<String, Object> fModuleNames = new HashMap<>();

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
				for (final Entry<String, Boolean> entry : definition.getDependencies().entrySet()) {
					final ModuleDefinition requiredModule = scriptService.getModuleDefinition(entry.getKey());

					if (requiredModule == null)
						throw new RuntimeException("Could not resolve module dependency \"" + entry + "\"");

					if ((!fModuleNames.containsKey(requiredModule.getPath().toString())) || (entry.getValue())) {
						// only load if module was never loaded or reload is set to true
						try {
							loadModule(requiredModule.getPath().toString());
						} catch (final RuntimeException e) {
							throw new RuntimeException("Could not load module dependency \"" + requiredModule.getPath().toString() + "\"", e);
						}
					}
				}

				// print deprecation warning
				if (definition.isDeprecated())
					printError("Module \"" + moduleName + "\" is deprecated. Consider updating your code.");

				module = definition.createModuleInstance();
				if (module instanceof IScriptModule)
					((IScriptModule) module).initialize(getScriptEngine(), this);

				fModuleNames.put(moduleName, module);

				// we need to track this module already as we need it in case we want to manipulate functions of already loaded modules
				fModules.add(module);

				// scripts changing functions force reloading of the whole module stack
				if (module instanceof IScriptFunctionModifier) {
					final List<Object> reverseList = new ArrayList<>(fModules);
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
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
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

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules().values());

		modules.sort((m1, m2) -> {
			return m1.getPath().toString().compareTo(m2.getPath().toString());
		});

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
		print(output, true);

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

	/**
	 * Write a message to the output stream of the script engine.
	 *
	 * @param text
	 *            message to write
	 * @param lineFeed
	 *            <code>true</code> to add a line feed after the text
	 */
	@Override
	@WrapToScript
	public final void print(final @ScriptParameter(defaultValue = "") Object text, final @ScriptParameter(defaultValue = "true") boolean lineFeed) {
		if (lineFeed)
			getScriptEngine().getOutputStream().println(text);
		else
			getScriptEngine().getOutputStream().print(text);
	}

	/**
	 * Write a message to the error stream of the script engine.
	 *
	 * @param text
	 *            message to write
	 */
	@WrapToScript
	public final void printError(final @ScriptParameter(defaultValue = "") Object text) {
		getScriptEngine().getErrorStream().println(text);
	}

	@Override
	public void addModuleListener(final IModuleListener listener) {
		fModuleListeners.add(listener);
	}

	@Override
	public void removeModuleListener(final IModuleListener listener) {
		fModuleListeners.remove(listener);
	}

	/**
	 * Read a single line of data from the default input stream of the script engine. Depending on the <i>blocking</i> parameter this method will wait for user
	 * input or return immediately with available data.
	 *
	 * @param blocking
	 *            <code>true</code> results in a blocking call until data is available, <code>false</code> returns in any case
	 * @return string data from input stream or <code>null</code>
	 * @throws IOException
	 *             when reading on the input stream fails
	 */
	@WrapToScript
	public String readInput(@ScriptParameter(defaultValue = "true") final boolean blocking) throws IOException {
		final InputStream inputStream = getScriptEngine().getInputStream();
		boolean doRead = blocking;
		if (!doRead) {
			try {
				doRead = (inputStream.available() > 0);
			} catch (final IOException e) {
				// no data to read available
			}
		}

		if (doRead)
			// read a single line
			return new BufferedReader(new InputStreamReader(inputStream)).readLine();

		return null;
	}

	protected void fireModuleEvent(final Object module, final int type) {
		for (final Object listener : fModuleListeners.getListeners())
			((IModuleListener) listener).notifyModule(module, type);
	}

	public static IEnvironment getEnvironment(final IScriptEngine engine) {
		for (final Object variable : engine.getVariables().values()) {
			if (variable instanceof IEnvironment)
				return (IEnvironment) variable;
		}

		return null;
	}
}
