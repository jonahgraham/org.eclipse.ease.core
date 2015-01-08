/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.debugging.IScriptDebugFrame;

/**
 * Interface to be implemented by a script debug engine.
 */
public interface IDebugEngine extends IScriptEngine {

	/**
	 * Get the current stack trace. A trace is a stack starting with the root file executed by the engine. Function calls and files (called via include command)
	 * will be put on top of that stack. Each entry may contain a pointer to the current line number executed. Traces might be created dynamically on demand or
	 * accumulated during execution depending on the underlying engine.
	 *
	 * @return current stack trace
	 */
	List<IScriptDebugFrame> getStackTrace();

	void setupDebugger(ILaunch launch, boolean suspendOnStartup, boolean suspendOnScriptLoad, boolean showDynamicCode);
}
