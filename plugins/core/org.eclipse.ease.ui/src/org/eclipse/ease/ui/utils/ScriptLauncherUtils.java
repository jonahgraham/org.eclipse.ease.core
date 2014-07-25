/**
 *   Copyright (c) 2013 Atos
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *       Arthur Daussy - initial implementation
 */
package org.eclipse.ease.ui.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.storedscript.storedscript.IStoredScript;
import org.eclipse.ease.storedscript.storedscript.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.ease.ui.metadata.UIMetadataUtils;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class ScriptLauncherUtils {

	public static void launchStoredScript(final IStoredScript script) {
		ScriptType scriptType = script.getScriptType();

		IScriptEngine engine = null;
		IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		if (scriptService != null) {
			List<EngineDescription> engines = scriptService.getEngines(scriptType.getType());
			if (engines.isEmpty()) {
				String message = "Unable to find a script engine for script " + script.getUri();
				ErrorDialog
						.openError(Display.getDefault().getActiveShell(), "No engine found", message, Logger.createErrorStatus(message, Activator.PLUGIN_ID));
				return;
			}

			engine = engines.get(0).createEngine();
		}

		if (UIMetadataUtils.generateCodeInjectionFile(script)) {
			engine.addExecutionListener(new EffectiveScriptGenerator());
		}
		ScriptConsole console = ScriptConsole.create(engine.getName() + ": " + script.getUri(), engine);
		engine.setOutputStream(console.getOutputStream());
		engine.setErrorStream(console.getErrorStream());
		engine.setTerminateOnIdle(true);
		try {
			// First try to run it as a file
			File file = script.getFile();
			if (file != null) {
				engine.executeAsync(file);
			} else {
				// If is not file run from input stream
				engine.executeAsync(script.getInputStream());
			}
		} catch (IOException e2) {
			e2.printStackTrace();
			Logger.logError(e2.getMessage());
		}

		engine.schedule();
	}
}
