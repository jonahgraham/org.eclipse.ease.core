/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.dnd;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;

public class ModulesDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		return ((element instanceof ModuleDefinition) || (element instanceof ModuleEntry));
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
			final Method loadModuleMethod = EnvironmentModule.class.getMethod("loadModule", String.class);

			if (element instanceof ModuleDefinition) {
				final String call = codeFactory.createFunctionCall(loadModuleMethod, ((ModuleDefinition) element).getPath().toString());
				scriptEngine.executeAsync(call);

			} else if (element instanceof ModuleEntry) {
				if (((ModuleEntry) element).getEntry() instanceof Method) {
					final ModuleDefinition declaringModule = ((ModuleEntry) element).getModuleDefinition();
					if (!ModuleHelper.getLoadedModules(scriptEngine).contains(declaringModule)) {
						// module not loaded yet

						final String call = codeFactory.createFunctionCall(loadModuleMethod, declaringModule.getPath().toString());
						scriptEngine.executeAsync(call);
					}

					// FIXME we need to find reasonable default values for mandatory parameters
					final String call = codeFactory.createFunctionCall((Method) ((ModuleEntry) element).getEntry());
					scriptEngine.executeAsync(call);

				} else if (((ModuleEntry) element).getEntry() instanceof Field) {
					final ModuleDefinition declaringModule = ((ModuleEntry) element).getModuleDefinition();
					if (!ModuleHelper.getLoadedModules(scriptEngine).contains(declaringModule)) {
						// module not loaded yet

						final String call = codeFactory.createFunctionCall(loadModuleMethod, declaringModule.getPath().toString());
						scriptEngine.executeAsync(call);
					}

					scriptEngine.executeAsync(((Field) element).getName());
				}

			} else
				// fallback solution
				scriptEngine.executeAsync(element);

		} catch (final Exception e) {
			Logger.error(Activator.PLUGIN_ID, "loadModule() method not found in Environment module", e);

			// fallback solution
			scriptEngine.executeAsync(element);
		}
	}
}
