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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ui.PlatformUI;

public class ResourceDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		if ((element instanceof IFile) || (element instanceof File) || (element instanceof URI))
			return scriptEngine.getDescription().getSupportedScriptTypes().contains(getScriptType(element));

		return false;
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		try {
			ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
			Method includeMethod = EnvironmentModule.class.getMethod("include", String.class);

			if ((element instanceof IFile) || (element instanceof File)) {
				String call = codeFactory.createFunctionCall(includeMethod, ResourceTools.toAbsoluteLocation(element, null));
				scriptEngine.executeAsync(call);

			} else if (element instanceof URI) {
				String call = codeFactory.createFunctionCall(includeMethod, element.toString());
				scriptEngine.executeAsync(call);

			} else
				// fallback solution
				scriptEngine.executeAsync(element);

		} catch (Exception e) {
			Logger.error(Activator.PLUGIN_ID, "include() method not found in Environment module", e);

			// fallback solution
			scriptEngine.executeAsync(element);
		}
	}

	private static ScriptType getScriptType(final Object element) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getScriptType(ResourceTools.toAbsoluteLocation(element, null));
	}
}
