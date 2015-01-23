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
package org.eclipse.ease.lang.javascript.rhino.debugger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.AbstractScriptDebugger;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class RhinoDebugger extends AbstractScriptDebugger implements Debugger {

	public class RhinoDebugFrame implements DebugFrame, IScriptDebugFrame {

		private int mLineNumber = 0;

		private final DebuggableScript mFnOrScript;

		public RhinoDebugFrame(final DebuggableScript fnOrScript) {
			mFnOrScript = fnOrScript;
		}

		@Override
		public void onEnter(final Context cx, final Scriptable activation, final Scriptable thisObj, final Object[] args) {
			// nothing to do
		}

		@Override
		public void onLineChange(final Context cx, final int lineNumber) {
			mLineNumber = lineNumber;
			if (isTrackedScript(getScript())) {
				// static code or dynamic code activated

				// TODO in future we should not add stuff to the stack we do not track anyway, so we can get rid of the "if" statement
				processLine(getScript(), lineNumber);
			}
		}

		@Override
		public void onExceptionThrown(final Context cx, final Throwable ex) {
			setExceptionStacktrace();
		}

		@Override
		public void onExit(final Context cx, final boolean byThrow, final Object resultOrException) {
			getStacktrace().remove(this);
		}

		@Override
		public void onDebuggerStatement(final Context cx) {
			// nothing to do
		}

		@Override
		public int getLineNumber() {
			return mLineNumber;
		}

		@Override
		public Script getScript() {
			return RhinoDebugger.this.getScript(mFnOrScript);
		}

		@Override
		public int getType() {
			return mFnOrScript.isFunction() ? TYPE_FUNCTION : TYPE_FILE;
		}

		@Override
		public String getName() {
			if (mFnOrScript.isFunction())
				return mFnOrScript.getFunctionName() + "()";

			else {
				final Object file = getScript().getFile();
				if (file != null) {
					if (file instanceof IFile)
						return ((IFile) file).getName();

					else if (file instanceof File)
						return ((File) file).getName();

				} else {
					// dynamic script
					final String title = getScript().getTitle();
					return (title != null) ? "Dynamic: " + title : "(Dynamic)";
				}
			}

			return "(unknown source)";
		}

		@Override
		public Map<String, Object> getVariables() {
			return getEngine().getVariables();
		}
	}

	private final Map<DebuggableScript, Script> mFrameToSource = new HashMap<DebuggableScript, Script>();

	private Script mLastScript = null;

	public RhinoDebugger(final IScriptEngine engine, final boolean showDynamicCode) {
		super(engine, showDynamicCode);
	}

	@Override
	public void handleCompilationDone(final Context cx, final DebuggableScript fnOrScript, final String source) {
	}

	@Override
	public DebugFrame getFrame(final Context cx, final DebuggableScript fnOrScript) {

		Script script = getScript(fnOrScript);
		if (script == null)
			script = mLastScript;

		if (script == null)
			return null;

		// register script source
		final DebuggableScript parentScript = getParentScript(fnOrScript);
		if (!mFrameToSource.containsKey(parentScript))
			mFrameToSource.put(parentScript, script);

		// create debug frame
		final RhinoDebugFrame debugFrame = new RhinoDebugFrame(fnOrScript);
		// mDebugFrames.add(0, debugFrame);

		getStacktrace().add(0, debugFrame);

		return debugFrame;
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		switch (status) {

		case SCRIPT_START:
			// fall through
		case SCRIPT_INJECTION_START:
			mLastScript = script;
			break;

		default:
			// unknown event
			break;
		}

		super.notify(engine, script, status);
	}

	private Script getScript(final DebuggableScript rhinoScript) {
		return mFrameToSource.get(getParentScript(rhinoScript));
	}

	private static DebuggableScript getParentScript(DebuggableScript rhinoScript) {
		while (rhinoScript.getParent() != null)
			rhinoScript = rhinoScript.getParent();

		return rhinoScript;
	}
}
