/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunScript extends AbstractHandler implements IHandler {

	public static final String COMMAND_ID = "org.eclipse.ease.commands.script.run";
	public static final String PARAMETER_NAME = COMMAND_ID + ".name";

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		IScript script = null;

		final String scriptName = event.getParameter(PARAMETER_NAME);
		if (scriptName != null) {
			// script name provided as parameter
			final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
			script = repositoryService.getScript(scriptName);

		} else {
			// look for active script selection
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof IScript)
					script = (IScript) element;
			}
		}

		if (script != null) {
			// see if we may execute this in the current view or as stand-alone
			IWorkbenchPart part = HandlerUtil.getActivePart(event);

			if (part instanceof IScriptEngineProvider)
				// execute in current view
				((IScriptEngineProvider) part).getScriptEngine().executeAsync("include('script:/" + script.getPath() + "');");
			else
				// execute stand-alone
				script.run();
		}

		return null;
	}
}
