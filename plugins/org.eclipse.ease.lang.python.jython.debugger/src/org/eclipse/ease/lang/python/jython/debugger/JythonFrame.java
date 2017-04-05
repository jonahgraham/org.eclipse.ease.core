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

import org.eclipse.ease.lang.python.debugger.IPyFrame;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Wrapper class for calling {@link IPyFrame} functionality on {@link PyObject}.
 */
public class JythonFrame implements IPyFrame {
	/**
	 * {@link IPyFrame} in Python form.
	 * <p>
	 * All calls simply wrap to this.
	 */
	private final PyObject fJythonFrame;

	/**
	 * Constructor only stores parameters to member.
	 *
	 * @param frame
	 *            {@link IPyFrame} in Python form.
	 */
	public JythonFrame(PyObject frame) {
		fJythonFrame = frame;
	}

	/**
	 * Utility method to check if a {@link PyObject} is a null object (either in Java or in Python).
	 *
	 * @param object
	 *            {@link PyObject} to check if it is null.
	 * @return <code>true</code> if object has null value.
	 */
	private static boolean isNull(PyObject object) {
		return (object != null) && !Py.None.equals(object);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.lang.python.debugger.IPyFrame#getFilename()
	 */
	@Override
	public String getFilename() {
		if (isNull(fJythonFrame)) {
			final PyObject filename = fJythonFrame.invoke("getFilename");
			return filename.asString();
		}
		return "<No Filename>";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.lang.python.debugger.IPyFrame#getLineNumber()
	 */
	@Override
	public int getLineNumber() {
		if (isNull(fJythonFrame)) {
			final PyObject lineNumber = fJythonFrame.invoke("getLineNumber");
			return Py.py2int(lineNumber);
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.lang.python.debugger.IPyFrame#getParent()
	 */
	@Override
	public IPyFrame getParent() {
		if (isNull(fJythonFrame)) {
			return new JythonFrame(fJythonFrame.invoke("getParent"));
		}
		return null;
	}

}
