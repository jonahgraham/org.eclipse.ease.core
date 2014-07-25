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
package org.eclipse.ease.ui.scripts.ui;

import java.util.HashMap;

import org.eclipse.ease.ui.handler.RenameScript;
import org.eclipse.ease.ui.handler.RunScript;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Provide context menu entries for macros.
 */
public class ScriptContextMenuEntries extends AbstractContributionFactory {

	/**
	 * Constructor.
	 * 
	 * @param location
	 *            location to add factory to.
	 */
	public ScriptContextMenuEntries(final String location) {
		super(location, null);
	}

	@Override
	public final void createContributionItems(final IServiceLocator serviceLocator, final IContributionRoot additions) {
		final ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			if (!((IStructuredSelection) selection).isEmpty()) {

				final StringBuffer names = new StringBuffer();
				for (final Object object : ((IStructuredSelection) selection).toArray()) {
					if (object instanceof IScript)
						names.append(((IScript) object).getPath()).append(';');
				}

				if (names.length() > 0) {
					names.deleteCharAt(names.length() - 1);
					final HashMap<String, String> parameters = new HashMap<String, String>();

					// add "run" entry
					parameters.put(RunScript.PARAMETER_NAME, names.toString());
					final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator, null,
							RunScript.COMMAND_ID, CommandContributionItem.STYLE_PUSH);
					contributionParameter.label = "Run";
					contributionParameter.visibleEnabled = true;
					contributionParameter.parameters = parameters;
					CommandContributionItem contribution = new CommandContributionItem(contributionParameter);
					additions.addContributionItem(contribution, null);

					// add separator
					additions.addContributionItem(new Separator(), null);

					// TODO re-implement
					// add "edit" entry
					// parameters.clear();
					// parameters.put(Edit.PARAMETER_NAME, names.toString());
					// contributionParameter.commandId = Edit.COMMAND_ID;
					// contributionParameter.label = "Edit";
					// contribution = new CommandContributionItem(contributionParameter);
					// additions.addContributionItem(contribution, null);

					// add "rename" entry
					parameters.clear();
					parameters.put(RenameScript.PARAMETER_NAME, names.toString());
					contributionParameter.commandId = RenameScript.COMMAND_ID;
					contributionParameter.label = "Rename";
					contribution = new CommandContributionItem(contributionParameter);
					additions.addContributionItem(contribution, null);

					// TODO re-implement
					// add "delete" entry
					// parameters.clear();
					// parameters.put(Delete.PARAMETER_NAME, names.toString());
					// contributionParameter.commandId = Delete.COMMAND_ID;
					// contributionParameter.label = "Delete";
					// contribution = new CommandContributionItem(contributionParameter);
					// additions.addContributionItem(contribution, null);
				}
			}
		}
	}
}
