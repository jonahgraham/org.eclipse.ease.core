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
package org.eclipse.ease.debugging.events;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.Script;

public class BreakpointRequest extends AbstractEvent implements IModelRequest {

	public enum Mode {
		ADD, REMOVE
	}

	private final Script fScript;

	private final IBreakpoint fBreakpoint;

	private final Mode fMode;

	public BreakpointRequest(final Script script, final IBreakpoint breakpoint, final Mode mode) {
		fScript = script;
		fBreakpoint = breakpoint;
		fMode = mode;
	}

	public Script getScript() {
		return fScript;
	}

	public IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	public Mode getMode() {
		return fMode;
	}
}
