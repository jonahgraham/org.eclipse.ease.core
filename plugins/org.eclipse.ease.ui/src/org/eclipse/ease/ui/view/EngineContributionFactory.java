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
package org.eclipse.ease.ui.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.handler.SwitchEngine;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

public final class EngineContributionFactory extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator fServiceLocator;

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		fServiceLocator = serviceLocator;
	}

	@Override
	protected IContributionItem[] getContributionItems() {

		List<IContributionItem> contributions = new ArrayList<IContributionItem>();

		IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		if (scriptService != null) {
			Collection<EngineDescription> engines = scriptService.getEngines();

			final List<CommandContributionItemParameter> items = new ArrayList<CommandContributionItemParameter>();
			for (final EngineDescription description : engines) {

				// set parameter for command
				final HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put(SwitchEngine.PARAMETER_ID, description.getID());

				final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(fServiceLocator, null,
						SwitchEngine.COMMAND_ID, CommandContributionItem.STYLE_PUSH);
				contributionParameter.parameters = parameters;
				contributionParameter.label = description.getName();
				contributionParameter.visibleEnabled = true;
				contributionParameter.icon = Activator.getImageDescriptor("/icons/eobj16/engine.png");

				items.add(contributionParameter);
			}

			// sort contributions
			Collections.sort(items, new Comparator<CommandContributionItemParameter>() {

				@Override
				public int compare(final CommandContributionItemParameter o1, final CommandContributionItemParameter o2) {
					return o1.label.compareTo(o2.label);
				}
			});

			for (final CommandContributionItemParameter item : items)
				contributions.add(new CommandContributionItem(item));
		}

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
