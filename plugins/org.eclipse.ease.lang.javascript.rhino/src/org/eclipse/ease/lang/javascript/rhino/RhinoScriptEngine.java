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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.swt.widgets.Display;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
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

	/** Rhino Scope. Created when interpreter is initialized */
	protected ScriptableObject mScope;

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

		mScope = mContext.initStandardObjects();

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

		// unregister from classloader
		RhinoClassLoader.unregisterEngine(this);

		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		if (uiThread) {
			// run in UI thread
			final RunnableWithResult<Entry<Object, Exception>> runnable = new RunnableWithResult<Entry<Object, Exception>>() {

				@Override
				public void run() {
					// initialize scope
					getContext().initStandardObjects(getScope());

					// call execute again, now from correct thread
					try {
						setResult(new AbstractMap.SimpleEntry<Object, Exception>(internalExecute(script, reference, fileName), null));
					} catch (final Exception e) {
						setResult(new AbstractMap.SimpleEntry<Object, Exception>(null, e));
					}
				}
			};

			Display.getDefault().syncExec(runnable);

			// evaluate result
			final Entry<Object, Exception> result = runnable.getResult();
			if (result.getValue() != null)
				throw (result.getValue());

			return result.getKey();

		} else
			// run in engine thread
			return internalExecute(script, reference, fileName);
	}

	private Object internalExecute(final Script script, final Object reference, final String fileName) throws Exception {
		// remove an eventually cached terminate request
		((ObservingContextFactory) ContextFactory.getGlobal()).cancelTerminate(getContext());

		final InputStreamReader codeReader = new InputStreamReader(script.getCodeStream());
		try {
			final Object result;

			if (script.getCommand() instanceof NativeFunction)
				result = ((NativeFunction) script.getCommand()).call(getContext(), getScope(), getScope(), ScriptRuntime.emptyArgs);

			else if (script.getCommand() instanceof org.mozilla.javascript.Script)
				// execute anonymous functions
				result = ((org.mozilla.javascript.Script) script.getCommand()).exec(getContext(), getScope());

			else
				result = getContext().evaluateReader(getScope(), codeReader, fileName, 1, null);

			if (result instanceof Undefined)
				return null;

			else if (result instanceof NativeJavaObject)
				return ((NativeJavaObject) result).unwrap();

			return result;

		} catch (final WrappedException e) {
			final Throwable wrapped = e.getWrappedException();
			if (wrapped instanceof Exception)
				throw ((Exception) wrapped);

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

	public ScriptableObject getScope() {
		return mScope;
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
		final Object value = getScope().get(name, getScope());
		if (value instanceof NativeJavaObject)
			return ((NativeJavaObject) value).unwrap();

		return value;
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		final Map<String, Object> result = new HashMap<String, Object>();

		for (final Object key : getScope().getIds()) {
			final Object value = internalGetVariable(key.toString());
			if ((value == null) || (!value.getClass().getName().startsWith("org.mozilla.javascript.gen")))
				result.put(key.toString(), value);
		}

		return result;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		final Object value = getScope().get(name, getScope());
		return !Scriptable.NOT_FOUND.equals(value);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		final Scriptable scope = getScope();

		final Object jsOut = internaljavaToJS(content, scope);
		scope.put(name, scope, jsOut);
	}

	@Override
	protected Object internalRemoveVariable(final String name) {
		final Object result = getVariable(name);
		getScope().delete(name);

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
