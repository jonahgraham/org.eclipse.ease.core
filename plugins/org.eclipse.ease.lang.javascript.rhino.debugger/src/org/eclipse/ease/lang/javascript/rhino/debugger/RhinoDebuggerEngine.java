/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Mathieu Velten - Bug correction
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino.debugger;

import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.debugging.EventDispatchJob;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.lang.javascript.rhino.debugger.model.RhinoDebugTarget;
import org.mozilla.javascript.debug.Debugger;

/**
 * A script engine to execute/debug JavaScript code on a Rhino interpreter.
 */
public class RhinoDebuggerEngine extends RhinoScriptEngine implements IDebugEngine {

	/**
	 * Creates a new Rhino Debugger interpreter.
	 */
	public RhinoDebuggerEngine() {
		super("Rhino Debugger");

		setDebugger(new LineNumberDebugger(this));
	}

	@Override
	public void setOptimizationLevel(final int level) {
		// ignore as debugging requires not to use optimizations
	}

	@Override
	public void setupDebugger(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		final RhinoDebugTarget debugTarget = new RhinoDebugTarget(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		launch.addDebugTarget(debugTarget);

		final RhinoDebugger debugger = new RhinoDebugger(this, showDynamicCode);
		setDebugger(debugger);

		final EventDispatchJob dispatcher = new EventDispatchJob(debugTarget, debugger);
		debugTarget.setDispatcher(dispatcher);
		debugger.setDispatcher(dispatcher);
		dispatcher.schedule();
	}

	@Override
	public List<IScriptDebugFrame> getStackTrace() {
		final Debugger debugger = getDebugger();

		if (debugger instanceof RhinoDebugger)
			return ((RhinoDebugger) debugger).getStacktrace();

		return super.getStackTrace();
	}
}
