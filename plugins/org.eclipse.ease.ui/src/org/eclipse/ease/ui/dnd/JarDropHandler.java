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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;

/**
 * Drop handler accepting jar files. Calls loadJar() on dropped files.
 */
public class JarDropHandler extends AbstractFileDropHandler {

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
			Method loadJarMethod = EnvironmentModule.class.getMethod("loadJar", Object.class);

			String call = codeFactory.createFunctionCall(loadJarMethod, getFileURI(element));
			scriptEngine.executeAsync(call);

		} catch (Exception e) {
			Logger.error(Activator.PLUGIN_ID, "loadJar() method not found in Environment module", e);
		}
	}

	@Override
	protected Collection<String> getAcceptedFileExtensions() {
		return Arrays.asList("jar");
	}
}