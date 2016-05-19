/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.py4j.internal;

import java.util.Map;

import org.eclipse.ease.AbstractScriptEngine;

/**
 * Main entry point for Py4J on Python side.
 */
public interface IPythonSideEngine {
	/**
	 * Execute a block of code on the target as a script.
	 */
	IInteractiveReturn executeScript(String codeText, String filename) throws Throwable;

	/**
	 * Execute a block of code on the target as an interactive line.
	 */
	IInteractiveReturn executeInteractive(String codeText) throws Throwable;

	/**
	 * @see AbstractScriptEngine#internalGetVariable(String)
	 */
	Object internalGetVariable(String name);

	/**
	 * @see AbstractScriptEngine#internalGetVariables()
	 */
	Map<String, Object> internalGetVariables();

	/**
	 * @see AbstractScriptEngine#internalHasVariable(String)
	 */
	boolean internalHasVariable(String name);

	/**
	 * @see AbstractScriptEngine#internalSetVariable(String, Object)
	 */
	void internalSetVariable(String name, Object content);

	/**
	 * Tear down the Python side of the engine
	 */
	void teardownEngine();
}
