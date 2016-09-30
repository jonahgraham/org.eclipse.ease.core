/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *     Christian Pontesegger - fixed help lookup
 *******************************************************************************/
package org.eclipse.ease.ui.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenHelp extends AbstractHandler implements IHandler {

	public static final String COMMAND_ID = "org.eclipse.ease.commands.modulesexplorer.openhelp";

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {

		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {

			final Object element = ((IStructuredSelection) selection).getFirstElement();

			if (element instanceof ModuleDefinition) {
				final ModuleDefinition module = (ModuleDefinition) element;
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(module.getHelpLocation(null));

			} else if (element instanceof ModuleEntry) {
				if (((ModuleEntry) element).getEntry() instanceof Method) {
					final ModuleDefinition module = ((ModuleEntry) element).getModuleDefinition();
					if (module != null)
						PlatformUI.getWorkbench().getHelpSystem()
								.displayHelpResource(module.getHelpLocation(((Method) ((ModuleEntry) element).getEntry()).getName()));

				} else if (((ModuleEntry) element).getEntry() instanceof Field) {
					final ModuleDefinition module = ((ModuleEntry) element).getModuleDefinition();
					if (module != null)
						PlatformUI.getWorkbench().getHelpSystem()
								.displayHelpResource(module.getHelpLocation(((Field) ((ModuleEntry) element).getEntry()).getName()));
				}
			}
		}

		return null;
	}
}
