/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.py4j.internal;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.EnvironmentModule;

/**
 * Loads basic module support for script Py4J based engines
 */
public class Py4JBootstrap implements IScriptEngineLaunchExtension {

	private static final String BOOTSTRAP_CODE = EnvironmentModule.class.getName() + "().loadModule(\"" + EnvironmentModule.MODULE_NAME + "\")";

	@Override
	public void createEngine(IScriptEngine engine) {
		engine.executeAsync(new Script(Py4JBootstrap.class.getSimpleName(), BOOTSTRAP_CODE));
	}

}
