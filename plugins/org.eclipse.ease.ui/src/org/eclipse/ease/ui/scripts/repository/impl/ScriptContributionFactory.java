/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ease.ui.handler.RunScript;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

public class ScriptContributionFactory extends AbstractContributionFactory {

	public static final String DYNAMIC_ID = "dynamic";
	public static final String STATIC_ID = "static.one.time";

	private final List<IScript> fScripts = new ArrayList<IScript>();

	public ScriptContributionFactory(String location, String namespace) {
		super(location, namespace);
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		for (IContributionItem item : createContributions(DYNAMIC_ID, serviceLocator))
			additions.addContributionItem(item, null);

		System.out.println("added dynamic toolbar entry");

	}

	public List<IContributionItem> createContributions(String id, IServiceLocator serviceLocator) {
		List<IContributionItem> items = new ArrayList<IContributionItem>();

		for (IScript script : fScripts) {

			CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator, id, RunScript.COMMAND_ID,
					CommandContributionItem.STYLE_PUSH);

			contributionParameter.label = script.getName();
			contributionParameter.visibleEnabled = true;
			contributionParameter.parameters = new HashMap<String, String>();
			contributionParameter.parameters.put(RunScript.PARAMETER_NAME, script.getPath().toString());

			items.add(new CommandContributionItem(contributionParameter) {
				@Override
				public boolean isDynamic() {
					return true;
				}

			});
		}

		return items;
	}

	public void addScript(IScript script) {
		if (!fScripts.contains(script))
			fScripts.add(script);
	}
}
