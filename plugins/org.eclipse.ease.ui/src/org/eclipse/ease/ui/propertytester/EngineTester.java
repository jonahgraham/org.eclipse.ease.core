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
package org.eclipse.ease.ui.propertytester;

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class EngineTester extends PropertyTester {

	private static final String HAS_ENGINE = "hasEngine";
	private static final String HAS_DEBUG_ENGINE = "hasDebugEngine";

	public EngineTester() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean test(Object receiver, final String property, final Object[] args, final Object expectedValue) {
		if (receiver instanceof Collection)
			receiver = ((Collection) receiver).iterator().next();

		if (receiver instanceof FileEditorInput)
			receiver = ((FileEditorInput) receiver).getFile();

		if (!(receiver instanceof IFile))
			receiver = Platform.getAdapterManager().getAdapter(receiver, IResource.class);

		if (receiver instanceof IFile) {
			final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
			ScriptType scriptType = scriptService.getScriptType(ResourceTools.toAbsoluteLocation(receiver, null));
			if (scriptType != null) {

				Collection<EngineDescription> engines = scriptType.getEngines();

				if (HAS_ENGINE.equals(property))
					return !engines.isEmpty();

				if (HAS_DEBUG_ENGINE.equals(property)) {
					for (EngineDescription description : engines) {
						if (description.supportsDebugging())
							return true;
					}
				}
			}
		}

		return false;
	}
}
