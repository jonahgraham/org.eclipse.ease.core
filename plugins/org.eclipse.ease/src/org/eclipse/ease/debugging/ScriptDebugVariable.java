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
package org.eclipse.ease.debugging;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ease.Activator;

public class ScriptDebugVariable extends ScriptDebugElement implements IVariable {

	private final ScriptDebugStackFrame fStackFrame;

	private final String fName;

	private final Object fValue;

	public ScriptDebugVariable(final ScriptDebugStackFrame stackFrame, final String name, final Object value) {
		super(stackFrame.getDebugTarget());

		fStackFrame = stackFrame;
		fName = name;
		fValue = value;
	}

	@Override
	public void setValue(final String expression) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not implemented"));
	}

	@Override
	public void setValue(final IValue value) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not implemented"));
	}

	@Override
	public boolean supportsValueModification() {
		return false;
	}

	@Override
	public boolean verifyValue(final String expression) {
		return false;
	}

	@Override
	public boolean verifyValue(final IValue value) {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return fStackFrame.isTerminated();
	}

	@Override
	public boolean isSuspended() {
		return fStackFrame.isSuspended();
	}

	@Override
	public boolean isStepping() {
		return fStackFrame.isStepping();
	}

	@Override
	public ScriptDebugValue getValue() {
		return new ScriptDebugValue(fStackFrame, fValue);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getReferenceTypeName() {
		return (fValue != null) ? fValue.getClass().getSimpleName() : "";
	}

	@Override
	public boolean hasValueChanged() {
		return false;
	}

	@Override
	public String toString() {
		try {
			return getName() + ":" + getValue().getValueString();
		} catch (final DebugException e) {
		}

		return super.toString();
	}
}
