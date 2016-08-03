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
package org.eclipse.ease.applications;

import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class ListEngines implements IApplication {

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		System.out.println("Name: engineID");
		System.out.println("==============");
		IScriptService service = ScriptService.getService();
		for (EngineDescription description : service.getEngines())
			System.out.println("\t" + description.getName() + ": " + description.getID());

		return 0;
	}

	@Override
	public void stop() {
	}
}
