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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ScriptParameter;

public interface ICodeFactory {

	/** Trace enablement for module wrappers. */
	boolean TRACE_MODULE_WRAPPER = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/moduleWrapper"));

	public static class Parameter {

		private Class<?> fClazz;
		private String fName = "";
		private boolean fOptional = false;
		private String fDefaultValue = ScriptParameter.NULL;

		public void setClass(final Class<?> clazz) {
			fClazz = clazz;
		}

		public void setName(final String name) {
			fName = name;
		}

		public void setOptional(final boolean optional) {
			fOptional = optional;
		}

		public void setDefault(final String defaultValue) {
			fDefaultValue = defaultValue;
		}

		public String getName() {
			return fName;
		}

		public Class<?> getClazz() {
			return fClazz;
		}

		public String getDefaultValue() {
			return fDefaultValue;
		}

		public boolean isOptional() {
			return fOptional;
		}
	}

	String getSaveVariableName(String variableName);

	String createFunctionWrapper(IEnvironment environment, String moduleVariable, Method method);

	String classInstantiation(Class<?> clazz, String[] parameters);

	String createStaticFieldWrapper(IEnvironment environment, String moduleVariable, Field field);

	String createFunctionCall(Method method, Object... parameters);

	String getDefaultValue(final Parameter parameter);
}
