/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.touchpoints;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.ui.PlatformUI;

/**
 * Touchpoint to register a new script location for a given plug-in. Requires parameter <i>plugin</i> which contains to plug-in name to be registered.
 */
public class AddScriptLocationTouchpoint extends ProvisioningAction {

	public static void main(final String[] args) {
		System.out.println(Boolean.parseBoolean(null));
	}

	@Override
	public IStatus execute(final Map<String, Object> parameters) {

		Object pluginName = parameters.get("plugin");
		if (pluginName != null) {
			boolean recursive = parameters.containsKey("recursive") ? Boolean.parseBoolean(parameters.get("recursive").toString()) : true;
			String subFolder = parameters.containsKey("folder") ? "/" + parameters.get("folder") : "";

			IRepositoryService repositoryService = PlatformUI.getWorkbench().getService(IRepositoryService.class);
			repositoryService.addLocation("platform:/plugin/" + pluginName + subFolder, false, recursive);
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(final Map<String, Object> parameters) {
		return Status.OK_STATUS;
	}
}
