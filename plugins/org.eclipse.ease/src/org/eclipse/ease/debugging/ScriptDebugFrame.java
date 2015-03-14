/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging;

import java.util.Collections;
import java.util.Map;

import org.eclipse.ease.Script;

/**
 * Frame containing debug location information for a dedicated script source.
 */
public class ScriptDebugFrame implements IScriptDebugFrame {

	private final Script fScript;
	private int fLineNumber;
	private final int fType;

	public ScriptDebugFrame(final Script script, final int lineNumber, final int type) {
		// deep copy script to get rid of references to the script engine (due to the stored result)
		fScript = script.clone();
		fLineNumber = lineNumber;
		fType = type;
	}

	public ScriptDebugFrame(final IScriptDebugFrame frame) {
		this(frame.getScript(), frame.getLineNumber(), frame.getType());
	}

	@Override
	public int getLineNumber() {
		return fLineNumber;
	}

	@Override
	public Script getScript() {
		return fScript;
	}

	@Override
	public int getType() {
		return fType;
	}

	@Override
	public String getName() {
		return getScript().getTitle();
	}

	@Override
	public Map<String, Object> getVariables() {
		return Collections.emptyMap();
	}

	@Override
	public void setLineNumber(final int lineNumber) {
		fLineNumber = lineNumber;
	}
}
