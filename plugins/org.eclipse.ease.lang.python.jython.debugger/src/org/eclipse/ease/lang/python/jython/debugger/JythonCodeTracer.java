/*******************************************************************************
 * Copyright (c) 2017 Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.jython.debugger;

import org.eclipse.ease.Script;
import org.eclipse.ease.lang.python.debugger.ICodeTracer;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Wrapper class for calling {@link ICodeTracer} functionality on {@link PyObject}.
 */
public class JythonCodeTracer implements ICodeTracer {
	/**
	 * {@link ICodeTracer} in Python form.
	 * <p>
	 * All calls simply wrap to this.
	 */
	private final PyObject fPyTracer;

	/**
	 * Constructor only stores parameters to member.
	 *
	 * @param tracer
	 *            {@link ICodeTracer} in Python form.
	 */
	public JythonCodeTracer(PyObject tracer) {
		fPyTracer = tracer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.lang.python.debugger.ICodeTracer#run(org.eclipse.ease.Script, java.lang.String)
	 */
	@Override
	public void run(Script script, String filename) {
		fPyTracer.invoke("run", Py.java2py(script), Py.java2py(filename));
	}

}
