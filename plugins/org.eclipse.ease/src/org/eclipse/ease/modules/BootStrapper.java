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
package org.eclipse.ease.modules;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.ScriptService;

/**
 * Loads basic module support for script engines. The {@link EnvironmentModule} provides basic functionality to manage modules, include other source files and
 * to print data. It will be loaded automatically when a script engine is started.
 */
public class BootStrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {
		ICodeFactory codeFactory = ScriptService.getCodeFactory(engine);
		if (codeFactory != null) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(codeFactory.classInstantiation(EnvironmentModule.class, new String[0]));
			stringBuilder.append(".loadModule(\"");
			stringBuilder.append(EnvironmentModule.MODULE_NAME);
			stringBuilder.append("\");\n");

			engine.executeAsync(new Script("Bootloader", stringBuilder.toString()));
		}
	}
}
