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

/**
 * An ICodeFactory is capable of generating code fragments for a dedicated target language.
 */
public interface ICodeFactory {

	/** Trace enablement for module wrappers. */
	boolean TRACE_MODULE_WRAPPER = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/moduleWrapper"));

	/**
	 * Parameter definition class. Holds data to describe a script parameter used in a function call.
	 */
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

	/**
	 * Converts a given string to a save variable name for the target language. Typically filters invalid characters and verifies that the returned string does
	 * not match any reserved keyword. Does not verify if the returned name is already in use.
	 *
	 * @param variableName
	 *            variable name candidate
	 * @return converted variable name
	 */
	String getSaveVariableName(String variableName);

	/**
	 * Create code to wrap a java method call on a given script variable to the target language. Creates script code that invokes the given <i>method</i> on the
	 * given <i>moduleVariable</i>.
	 *
	 * @param environment
	 *            script environment
	 * @param moduleVariable
	 *            name of variable holding the module instance
	 * @param method
	 *            method to be wrapped
	 * @return wrapped script code
	 */
	String createFunctionWrapper(IEnvironment environment, String moduleVariable, Method method);

	/**
	 * Create code to instantiate a java class.
	 *
	 * @param clazz
	 *            class to instantiate
	 * @param parameters
	 *            parameters used for class instantiation
	 * @return wrapped script code
	 */
	String classInstantiation(Class<?> clazz, String[] parameters);

	/**
	 * Create code to bind a final java field to a script variable.
	 *
	 * @param environment
	 *            script environment
	 * @param moduleVariable
	 *            name of variable holding the module instance
	 * @param field
	 *            field to be accessed
	 * @return wrapped script code
	 */
	String createFinalFieldWrapper(IEnvironment environment, String moduleVariable, Field field);

	/**
	 * Create code to call a wrapped function. Create code to call a script function that was wrapped using
	 * {@link #createFunctionWrapper(IEnvironment, String, Method)} before.
	 *
	 * @param method
	 *            method to be called
	 * @param parameters
	 *            call parameters
	 * @return script code to call function
	 */
	String createFunctionCall(Method method, Object... parameters);

	/**
	 * Get the default value for a given parameter
	 *
	 * @param parameter
	 *            parameter to get default value for
	 * @return String representation of default value
	 */
	String getDefaultValue(final Parameter parameter);
}
