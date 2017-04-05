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

package org.eclipse.ease.lang.python.debugger;

/**
 * Abstraction interface for frame information in Python.
 */
public interface IPyFrame {
	/**
	 * Returns the filename of the current frame.
	 * <p>
	 * Must <b>NOT</b> return <code>null</code>.
	 *
	 * @return filename for the current frame.
	 */
	public String getFilename();

	/**
	 * Returns the linenumber of the current frame.
	 *
	 * @return line number of the current frame.
	 */
	public int getLineNumber();

	/**
	 * Returns the parent frame in the call stack.
	 * <p>
	 * If the current frame is the root, <code>null</code> should be returned.
	 *
	 * @return Parent in the call stack or <code>null</code>
	 */
	public IPyFrame getParent();
}
