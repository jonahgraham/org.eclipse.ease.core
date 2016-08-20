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

package org.eclipse.ease.security;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ISecurityCheck;
import org.osgi.service.prefs.Preferences;

public class ScriptUIAccess implements ISecurityCheck {

	/** Singleton instance. */
	private static ISecurityCheck INSTANCE = new ScriptUIAccess();

	public static ISecurityCheck getInstance() {
		return INSTANCE;
	}

	private ScriptUIAccess() {
		// hide constructor from public
	}

	@Override
	public boolean doIt(ActionType action, Object... data) throws SecurityException {
		if ((ActionType.INJECT_CODE == action) && (data.length >= 2) && (data[1] instanceof Boolean) && ((Boolean) data[1])) {
			final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);
			final boolean allowUIAccess = prefs.getBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, Activator.DEFAULT_SCRIPTS_ALLOW_UI_ACCESS);
			if (!allowUIAccess)
				throw new SecurityException("Script UI access disabled by user preferences.");
		}

		return true;
	}
}
