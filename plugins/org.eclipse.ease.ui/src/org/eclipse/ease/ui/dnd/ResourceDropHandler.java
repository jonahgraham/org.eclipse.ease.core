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
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IModuleWrapper;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
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
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		try {
			IModuleWrapper moduleWrapper = scriptService.getModuleWrapper(scriptEngine.getDescription().getID());
			Method includeMethod = EnvironmentModule.class.getMethod("include", String.class);

			if ((element instanceof IFile) || (element instanceof File)) {
				String call = moduleWrapper.createFunctionCall(includeMethod, ResourceTools.toAbsoluteLocation(element, null));
				scriptEngine.executeAsync(call);

			} else if (element instanceof URI) {
				String call = moduleWrapper.createFunctionCall(includeMethod, element.toString());
				scriptEngine.executeAsync(call);

			} else
				// fallback solution
				scriptEngine.executeAsync(element);

		} catch (Exception e) {
			Logger.logError("include() method not found in Environment module", e);

			// fallback solution
			scriptEngine.executeAsync(element);
		}
	}

	private static ScriptType getScriptType(final Object element) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getScriptType(ResourceTools.toAbsoluteLocation(element, null));
	}
}
