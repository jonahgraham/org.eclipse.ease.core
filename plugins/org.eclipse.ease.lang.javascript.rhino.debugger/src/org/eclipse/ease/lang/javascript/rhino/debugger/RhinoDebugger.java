/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Bernhard Wedl - added Nativ
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino.debugger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.AbstractScriptDebugger;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class RhinoDebugger extends AbstractScriptDebugger implements Debugger {

	public class RhinoDebugFrame extends ScriptDebugFrame implements DebugFrame, IScriptDebugFrame {

		private final String fFunctionName;

		private Scriptable fScope;

		public RhinoDebugFrame(final DebuggableScript fnOrScript) {
			super(RhinoDebugger.this.getScript(fnOrScript), 0, fnOrScript.isFunction() ? TYPE_FUNCTION : TYPE_FILE);
			fFunctionName = fnOrScript.getFunctionName();
		}

		@Override
		public void onEnter(final Context cx, final Scriptable activation, final Scriptable thisObj, final Object[] args) {
			// nothing to do
			fScope = activation;
		}

		@Override
		public void onLineChange(final Context cx, final int lineNumber) {
			setLineNumber(lineNumber);
			if (isTrackedScript(getScript())) {
				// static code or dynamic code activated

				// TODO in future we should not add stuff to the stack we do not track anyway, so we can get rid of the "if" statement
				processLine(getScript(), lineNumber);
			}
		}

		@Override
		public void onExceptionThrown(final Context cx, final Throwable ex) {
			setExceptionStacktrace();

			// we do not need the scope any longer
			fScope = null;
		}

		@Override
		public void onExit(final Context cx, final boolean byThrow, final Object resultOrException) {
			getStacktrace().remove(this);

			// we do not need the scope any longer
			fScope = null;
		}

		@Override
		public void onDebuggerStatement(final Context cx) {
			// nothing to do
		}

		@Override
		public String getName() {
			if (getType() == IScriptDebugFrame.TYPE_FUNCTION) {
				String title = getScript().getTitle();
				if (title == null)
					title = "";

				return title + ":" + fFunctionName + "()";

			} else {
				String title = getScript().getTitle();
				if (title == null)
					title = "(Dynamic)";

				return title;
			}
		}

		@Override
		public Map<String, Object> getVariables() {
			final Map<String, Object> result = RhinoScriptEngine.getVariables(fScope);

			return result;
		}

		@Override
		public Map<String, Object> getVariables(final Object parent) {
			if (parent instanceof NativeObject) {
				Map<String, Object> children = new TreeMap<String, Object>();
				for (Object key : ((NativeObject) parent).getIds())
					children.put(key.toString(), ((NativeObject) parent).get(key));

				return children;
			} else if (parent instanceof NativeArray) {
				Map<String, Object> children = new LinkedHashMap<String, Object>();

				for (Object key : ((NativeArray) parent).getIds())
					children.put("[" + key + "]", ((NativeArray) parent).get(key));

				return children;
			}

			return super.getVariables(parent);
		}
	}

	private final Map<Integer, Script> fFrameToSource = new HashMap<Integer, Script>();

	private Script fLastScript = null;

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
			script = fLastScript;

		if (script == null)
			return null;

		// register script source
		final DebuggableScript parentScript = getParentScript(fnOrScript);
		if (!fFrameToSource.containsKey(parentScript.hashCode()))
			fFrameToSource.put(parentScript.hashCode(), script);

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
			fLastScript = script;
			break;

		case ENGINE_END:
			fFrameToSource.clear();
			fLastScript = null;
			break;

		default:
			// unknown event
			break;
		}

		super.notify(engine, script, status);
	}

	private Script getScript(final DebuggableScript rhinoScript) {
		return fFrameToSource.get(getParentScript(rhinoScript).hashCode());
	}

	private static DebuggableScript getParentScript(DebuggableScript rhinoScript) {
		while (rhinoScript.getParent() != null)
			rhinoScript = rhinoScript.getParent();

		return rhinoScript;
	}
}
