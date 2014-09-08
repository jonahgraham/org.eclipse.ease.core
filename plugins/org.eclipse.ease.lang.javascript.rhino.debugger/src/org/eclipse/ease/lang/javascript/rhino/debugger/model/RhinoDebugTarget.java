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
package org.eclipse.ease.lang.javascript.rhino.debugger.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.ScriptDebugTarget;

public class RhinoDebugTarget extends ScriptDebugTarget {

	public RhinoDebugTarget(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad) {
		super(launch, suspendOnStartup, suspendOnScriptLoad);
	}

	@Override
	public String getName() throws DebugException {
		return "EASE Rhino Debugger";
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************

	@Override
	protected IBreakpoint[] getBreakpoints(final Script script) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.eclipse.wst.jsdt.debug.model");
	}
}
