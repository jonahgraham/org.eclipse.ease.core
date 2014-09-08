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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
		if(isSimpleType(fValue))
			return fValue + " (" + fValue.getClass().getSimpleName().toLowerCase() + ")";

		return (fValue != null) ? fValue.getClass().getSimpleName() : "null";
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return fValue != null;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		List<IVariable> variables = new ArrayList<IVariable>();

		if((fValue != null) && (!isSimpleType(fValue))) {
			if(fValue.getClass().isArray()) {
				// handle arrays

				if(fValue instanceof Object[]) {
					for(int index = 0; index < ((Object[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((Object[])fValue)[index]));

				} else if(fValue instanceof char[]) {
					for(int index = 0; index < ((char[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((char[])fValue)[index]));

				} else if(fValue instanceof byte[]) {
					for(int index = 0; index < ((byte[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((byte[])fValue)[index]));

				} else if(fValue instanceof boolean[]) {
					for(int index = 0; index < ((boolean[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((boolean[])fValue)[index]));

				} else if(fValue instanceof short[]) {
					for(int index = 0; index < ((short[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((short[])fValue)[index]));

				} else if(fValue instanceof int[]) {
					for(int index = 0; index < ((int[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((int[])fValue)[index]));

				} else if(fValue instanceof long[]) {
					for(int index = 0; index < ((long[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((long[])fValue)[index]));

				} else if(fValue instanceof double[]) {
					for(int index = 0; index < ((double[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((double[])fValue)[index]));

				} else if(fValue instanceof float[]) {
					for(int index = 0; index < ((float[])fValue).length; index++)
						variables.add(new ScriptDebugVariable(fStackFrame, "[" + index + "]", ((float[])fValue)[index]));
				}

			} else {
				// handle java objects
				for(Field field : fValue.getClass().getDeclaredFields()) {
					try {
						if(!Modifier.isStatic(field.getModifiers())) {
							if(!field.isAccessible())
								field.setAccessible(true);

							variables.add(new ScriptDebugVariable(fStackFrame, field.getName(), field.get(fValue)));
						}
					} catch (Exception e) {
					}
				}
			}
		}

		return variables.toArray(new IVariable[variables.size()]);
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return getVariables().length > 0;
	}

	@Override
	public Object getAdapter(final Class adapter) {
		if(String.class.equals(adapter))
			return (fValue != null) ? fValue.toString() : "";

		return super.getAdapter(adapter);
	}

	public Object getValue() {
		return fValue;
	}

	private static boolean isSimpleType(final Object value) {
		return (value instanceof Integer) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Boolean) || (value instanceof Character) || (value instanceof Long) || (value instanceof Double) || (value instanceof Float);
	}
}
