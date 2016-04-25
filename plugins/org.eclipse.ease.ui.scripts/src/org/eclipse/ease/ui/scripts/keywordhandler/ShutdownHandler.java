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

package org.eclipse.ease.ui.scripts.keywordhandler;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class ShutdownHandler implements EventHandler, IWorkbenchListener {

	public ShutdownHandler() {
		PlatformUI.getWorkbench().addWorkbenchListener(this);
	}

	/** Registered shutdown scripts. */
	Collection<IScript> fShutdownScripts = new HashSet<IScript>();

	/** Default script timeout: 10s. */
	private long fShutdownTimeout = 10 * 1000;

	@Override
	public void handleEvent(final Event event) {
		final IScript script = (IScript) event.getProperty("script");
		String value = (String) event.getProperty("value");

		if (value == null)
			fShutdownScripts.remove(script);
		else {
			fShutdownScripts.add(script);

			if (!value.isEmpty()) {
				try {
					fShutdownTimeout = Math.max(fShutdownTimeout, Integer.parseInt(value) * 1000);
				} catch (NumberFormatException e) {
					Logger.error(Activator.PLUGIN_ID, "Invalid onShutdown timeout for script: " + script.getLocation());
				}
			}
		}
	}

	@Override
	public boolean preShutdown(final IWorkbench workbench, final boolean forced) {
		if (!forced) {
			long startTime = System.currentTimeMillis();
			Collection<IScriptEngine> engines = new HashSet<IScriptEngine>();
			for (IScript script : fShutdownScripts)
				engines.add(script.run());

			// wait for engines to be completed
			for (IScriptEngine engine : engines) {
				long timeout = (startTime + fShutdownTimeout) - System.currentTimeMillis();
				if (timeout > 0) {
					try {
						engine.join(timeout);
					} catch (InterruptedException e) {
					}
				} else
					break;
			}

			// terminate engines that are not completed
			for (IScriptEngine engine : engines) {
				if (!engine.isFinished())
					engine.terminate();
			}
		}

		return true;
	}

	@Override
	public void postShutdown(final IWorkbench workbench) {
	}
}
