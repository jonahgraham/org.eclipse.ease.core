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

import org.eclipse.ease.IDebugEngine;

/**
 * Extension of {@link IDebugEngine} for Python script engines.
 */
public interface IPythonDebugEngine extends IDebugEngine {
	/**
	 * Sets the PythonDebugger for the debug engine.
	 *
	 * @param debugger
	 *            {@link PythonDebugger} to be used.
	 */
	public void setDebugger(PythonDebugger debugger);
}
