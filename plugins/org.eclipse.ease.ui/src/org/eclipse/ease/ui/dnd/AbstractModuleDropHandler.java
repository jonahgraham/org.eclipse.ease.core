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

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractEnvironment;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;

/**
 * Helper class for drop handlers. Adds convenience methods to keep dedicated handlers simpler.
 */
public abstract class AbstractModuleDropHandler implements IShellDropHandler {

	/**
	 * Load a dedicated module if it is not already loaded.
	 *
	 * @param scriptEngine
	 *            script engine to load module
	 * @param moduleID
	 *            moduleID to look for
	 * @param force
	 *            if set to <code>false</code> load only when not already loaded
	 * @return module instance or <code>null</code>
	 */
	protected Object loadModule(final IScriptEngine scriptEngine, final String moduleID, final boolean force) {

		IEnvironment environment = AbstractEnvironment.getEnvironment(scriptEngine);
		if (environment != null) {
			if ((force) || (environment.getModule(moduleID) == null)) {
				if (ModuleHelper.resolveName(moduleID) != null) {

					ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);

					try {
						String functionCall = codeFactory.createFunctionCall(EnvironmentModule.class.getMethod("loadModule", String.class), moduleID);
						return scriptEngine.executeSync(functionCall);

					} catch (NoSuchMethodException e) {
						Logger.error(Activator.PLUGIN_ID, "Method loadModule() not found", e);
					} catch (SecurityException e) {
						Logger.error(Activator.PLUGIN_ID, "Method loadModule() not accessible", e);
					} catch (InterruptedException e) {
						Logger.error(Activator.PLUGIN_ID, "Script execution interrupted", e);
					}

				} else
					Logger.error(Activator.PLUGIN_ID, "Module \"" + moduleID + "\" cannot be found");

			} else
				return environment.getModule(moduleID);
		}

		return null;
	}
}
