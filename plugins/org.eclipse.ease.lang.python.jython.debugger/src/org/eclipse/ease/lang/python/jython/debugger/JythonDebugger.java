/*******************************************************************************
 * Copyright (c) 2014 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.jython.debugger;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.lang.python.debugger.IPyFrame;
import org.eclipse.ease.lang.python.debugger.PythonDebugger;
import org.python.core.PyObject;

/**
 * Extension of {@link PythonDebugger} for overloading methods to have {@link PyObject} parameter wrappers.
 */
public class JythonDebugger extends PythonDebugger {
	/**
	 * @see PythonDebugger#PythonDebugger(IScriptEngine, boolean)
	 */
	public JythonDebugger(IScriptEngine engine, boolean showDynamicCode) {
		super(engine, showDynamicCode);
	}

	/**
	 * Overload of {@link #setCodeTracer(org.eclipse.ease.lang.python.debugger.ICodeTracer)} wrapping {@link PyObject} to {@link JythonCodeTracer}.
	 *
	 * @param tracer
	 *            {@link PyObject} representation of code tracer.
	 */
	public void setCodeTracer(PyObject tracer) {
		setCodeTracer(new JythonCodeTracer(tracer));
	}

	/**
	 * Overload of {@link #traceDispatch(org.eclipse.ease.lang.python.debugger.IPyFrame, String)} wrapping {@link PyObject} to {@link IPyFrame}.
	 */
	public void traceDispatch(PyObject frame, String type) {
		traceDispatch(new JythonFrame(frame), type);
	}
}