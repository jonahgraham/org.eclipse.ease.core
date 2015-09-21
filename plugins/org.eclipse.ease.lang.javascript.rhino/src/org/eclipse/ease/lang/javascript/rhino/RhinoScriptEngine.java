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
package org.eclipse.ease.lang.javascript.rhino;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.swt.widgets.Display;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.debug.Debugger;

/**
 * A script engine to execute JavaScript code on a Rhino interpreter.
 */
public class RhinoScriptEngine extends AbstractScriptEngine {

	static {
		// set context factory that is able to terminate script execution
		ContextFactory.initGlobal(new ObservingContextFactory());

		// set a custom class loader to find everything in the eclipse universe
		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				ContextFactory.getGlobal().initApplicationClassLoader(RhinoClassLoader.getInstance());
				return null;
			}
		});
	}

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.rhino";

	/** Rhino Scope. Created when interpreter is initialized */
	private ScriptableObject mScope;

	private Context mContext;

	private Debugger mDebugger = null;

	private int mOptimizationLevel = 9;

	/**
	 * Creates a new Rhino interpreter.
	 */
	public RhinoScriptEngine() {
		super("Rhino");
	}

	/**
	 * Creates a new Rhino interpreter.
	 *
	 * @param name
	 *            name of interpreter (used for the jobs name)
	 */
	protected RhinoScriptEngine(final String name) {
		super(name);
	}

	public void setOptimizationLevel(final int level) {
		mOptimizationLevel = level;
	}

	@Override
	protected synchronized boolean setupEngine() {
		mContext = getContext();

		if (mDebugger != null) {
			mContext.setOptimizationLevel(-1);
			mContext.setGeneratingDebug(true);
			mContext.setGeneratingSource(true);
			mContext.setDebugger(mDebugger, null);

		} else {
			mContext.setGeneratingDebug(false);
			mContext.setOptimizationLevel(mOptimizationLevel);
			mContext.setDebugger(null, null);
		}

		mScope = new ImporterTopLevel(mContext);

		// enable script termination support
		mContext.setGenerateObserverCount(true);
		mContext.setInstructionObserverThreshold(10);

		return true;
	}

	@Override
	protected synchronized boolean teardownEngine() {
		// remove debugger to allow for garbage collection
		mContext.setDebugger(null, null);

		// cleanup context
		Context.exit();
		mContext = null;
		mScope = null;

		// unregister from classloader
		RhinoClassLoader.unregisterEngine(this);

		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Throwable {
		if (uiThread) {
			// run in UI thread
			final RunnableWithResult<Entry<Object, Throwable>> runnable = new RunnableWithResult<Entry<Object, Throwable>>() {

				@Override
				public void run() {
					// initialize scope
					getContext().initStandardObjects(mScope);

					// call execute again, now from correct thread
					try {
						setResult(new AbstractMap.SimpleEntry<Object, Throwable>(internalExecute(script, reference, fileName), null));
					} catch (final Throwable e) {
						setResult(new AbstractMap.SimpleEntry<Object, Throwable>(null, e));
					}
				}
			};

			Display.getDefault().syncExec(runnable);

			// evaluate result
			final Entry<Object, Throwable> result = runnable.getResult();
			if (result.getValue() != null)
				throw (result.getValue());

			return result.getKey();

		} else
			// run in engine thread
			return internalExecute(script, reference, fileName);
	}

	private Object internalExecute(final Script script, final Object reference, final String fileName) throws Throwable {
		// remove an eventually cached terminate request
		((ObservingContextFactory) ContextFactory.getGlobal()).cancelTerminate(getContext());

		final InputStreamReader codeReader = new InputStreamReader(script.getCodeStream());
		try {
			final Object result;

			if (script.getCommand() instanceof NativeFunction)
				result = ((NativeFunction) script.getCommand()).call(getContext(), mScope, mScope, ScriptRuntime.emptyArgs);

			else if (script.getCommand() instanceof org.mozilla.javascript.Script)
				// execute anonymous functions
				result = ((org.mozilla.javascript.Script) script.getCommand()).exec(getContext(), mScope);

			else
				result = getContext().evaluateReader(mScope, codeReader, fileName, 1, null);

			if ((result == null) || (result instanceof Undefined))
				return null;

			else if (result instanceof NativeJavaObject)
				return ((NativeJavaObject) result).unwrap();

			else if (result.getClass().getName().equals("org.mozilla.javascript.InterpretedFunction"))
				return null;

			return result;

		} catch (final WrappedException e) {
			final Throwable wrapped = e.getWrappedException();
			if (wrapped instanceof ScriptExecutionException)
				throw wrapped;

			else if (wrapped instanceof Throwable)
				throw new ScriptExecutionException(wrapped.getMessage(), e.columnNumber(), e.lineSource(), "JavaError",
						getExceptionStackTrace(script, e.lineNumber()), wrapped);

		} catch (final EcmaError e) {
			throw new ScriptExecutionException(e.getErrorMessage(), e.columnNumber(), e.lineSource(), e.getName(),
					getExceptionStackTrace(script, e.lineNumber()), null);

		} catch (final JavaScriptException e) {
			final String message = (e.getValue() != null) ? e.getValue().toString() : null;
			throw new ScriptExecutionException(message, e.lineNumber(), e.lineSource(), "ScriptException", getExceptionStackTrace(script, e.lineNumber()),
					null);

		} catch (final EvaluatorException e) {
			throw new ScriptExecutionException(e.getMessage(), e.columnNumber(), e.lineSource(), "SyntaxError", getExceptionStackTrace(script, e.lineNumber()),
					null);

		} catch (final RhinoException e) {
			throw new ScriptExecutionException("Error running script", e.columnNumber(), e.lineSource(), "Error",
					getExceptionStackTrace(script, e.lineNumber()), null);

		} finally {
			try {
				if (codeReader != null)
					codeReader.close();
			} catch (final IOException e) {
				// we did our best, give up
			}
		}

		return null;
	}

	/**
	 * Get a stack trace in case of a script exception. On exceptions a trace might not have picked up the topmost script. So we try to update the trace in case
	 * we have more accurate information than the script engine itself. Seems the Rhino debugger does not add compilation units to the stack before the
	 * exception is thrown.
	 *
	 * @param script
	 *            expected topmost script
	 * @param lineNumber
	 *            line number of exception root cause
	 * @return updated stack trace
	 */
	protected List<IScriptDebugFrame> getExceptionStackTrace(final Script script, final int lineNumber) {
		final List<IScriptDebugFrame> stackTrace = new ArrayList<IScriptDebugFrame>(getStackTrace());
		if ((script != null) && (!script.equals(stackTrace.get(0).getScript()))) {
			// topmost script is not what we expected, seems it was not put on the stack
			stackTrace.add(0, new ScriptDebugFrame(script, lineNumber, IScriptDebugFrame.TYPE_FILE));
		}

		return stackTrace;
	}

	public Context getContext() {
		Context context = Context.getCurrentContext();
		if (context == null) {
			synchronized (ContextFactory.getGlobal()) {
				context = Context.enter();
			}
		}

		return context;
	}

	@Override
	public void terminateCurrent() {
		// typically requested by a different thread, so do not use getContext() here
		((ObservingContextFactory) ContextFactory.getGlobal()).terminate(mContext);
	}

	public void setDebugger(final Debugger debugger) {
		mDebugger = debugger;
	}

	protected Debugger getDebugger() {
		return mDebugger;
	}

	@Override
	public synchronized void registerJar(final URL url) {
		RhinoClassLoader.registerURL(this, url);
	}

	@Override
	public synchronized void reset() {
		RhinoClassLoader.unregisterEngine(this);

		super.reset();

		setupEngine();
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return getVariable(mScope, name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return getVariables(mScope);
	}

	public static Map<String, Object> getVariables(final Scriptable scope) {
		final Map<String, Object> result = new TreeMap<String, Object>();

		for (final Object key : scope.getIds()) {
			final Object value = getVariable(scope, key.toString());
			if ((value == null) || (!value.getClass().getName().startsWith("org.mozilla.javascript.gen")))
				result.put(key.toString(), value);
		}

		// add parent scope
		final Scriptable parent = scope.getParentScope();
		if (parent != null)
			result.putAll(getVariables(parent));

		return result;
	}

	public static Object getVariable(final Scriptable scope, final String name) {
		final Object value = scope.get(name, scope);
		if (value instanceof NativeJavaObject)
			return ((NativeJavaObject) value).unwrap();

		return value;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		final Object value = mScope.get(name, mScope);
		return !Scriptable.NOT_FOUND.equals(value);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		final Scriptable scope = mScope;

		final Object jsOut = internaljavaToJS(content, scope);
		scope.put(name, scope, jsOut);
	}

	@Override
	protected Object internalRemoveVariable(final String name) {
		final Object result = getVariable(name);
		mScope.delete(name);

		return result;
	}

	private Object internaljavaToJS(final Object value, final Scriptable scope) {
		Object result = null;
		if (isPrimitiveType(value) || (value instanceof Scriptable)) {
			result = value;
		} else if (value instanceof Character) {
			result = String.valueOf(((Character) value).charValue());
		} else {
			result = getContext().getWrapFactory().wrap(getContext(), scope, value, null);
		}
		return result;

	}

	private boolean isPrimitiveType(final Object value) {
		return (value instanceof String) || (value instanceof Number) || (value instanceof Boolean);
	}

	@Override
	public String getSaveVariableName(final String name) {
		return JavaScriptHelper.getSaveName(name);
	}
}
