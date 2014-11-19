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
package org.eclipse.ease.ui.scripts.repository.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ease.ui.LocationImageDescriptor;
import org.eclipse.ease.ui.handler.RunScript;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class ScriptContributionItem extends CommandContributionItem {

	private static ImageDescriptor getImageDescriptor(final String imageLocation) {
		if (imageLocation != null)
			return LocationImageDescriptor.createFromLocation(imageLocation);

		return null;
	}

	private static Map<String, String> getParameters(final IScript script) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put(RunScript.PARAMETER_NAME, script.getPath().toString());

		return parameters;
	}

	private final IScript fScript;

	public ScriptContributionItem(final IScript script) {
		super(new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), script.getLocation(), RunScript.COMMAND_ID,
				getParameters(script), getImageDescriptor(script.getParameters().get("image")), null, null, script.getName(), null, null, STYLE_PUSH, null,
				true));

		fScript = script;
	}

	@Override
	public void update() {
		setLabel(fScript.getParameters().get("name"));
		ParameterizedCommand command = getCommand();
		command.getParameterMap().putAll(getParameters(fScript));

		super.update();
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
	}
}
