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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class ScriptDebugValue extends ScriptDebugElement implements IValue {

	private final ScriptDebugStackFrame fStackFrame;

	private final Object fValue;

	public ScriptDebugValue(final ScriptDebugStackFrame stackFrame, final Object value) {
		super(stackFrame.getDebugTarget());

		fStackFrame = stackFrame;
		fValue = value;
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
	public String getReferenceTypeName() throws DebugException {
		return (fValue != null) ? fValue.getClass().getSimpleName() : "";
	}

	@Override
	public String getValueString() throws DebugException {
		if (isSimpleType(fValue))
			return fValue + " (" + fValue.getClass().getSimpleName().toLowerCase() + ")";

		if (fValue instanceof String)
			return "\"" + fValue + "\"";

		return (fValue != null) ? fValue.getClass().getSimpleName() : "null";
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return fValue != null;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		IVariable[] children = fStackFrame.getVariables(fValue);
		if (children != null)
			return children;

		return new IVariable[0];
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables().length > 0;
	}

	@Override
	public Object getAdapter(final Class adapter) {
		if (String.class.equals(adapter))
			return (fValue != null) ? fValue.toString() : "";

		return super.getAdapter(adapter);
	}

	public Object getValue() {
		return fValue;
	}

	public static boolean isSimpleType(final Object value) {
		return (value instanceof Integer) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Boolean) || (value instanceof Character)
				|| (value instanceof Long) || (value instanceof Double) || (value instanceof Float);
	}
}
