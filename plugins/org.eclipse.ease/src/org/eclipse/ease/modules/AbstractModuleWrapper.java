/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractModuleWrapper implements IModuleWrapper {

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

	public AbstractModuleWrapper() {
	}

	/**
	 * Get the parameter name from a annotation. Use for engine which can have named variable
	 *
	 * @param parameterAnnotations
	 * @return
	 */
	protected String getParameterName(final Annotation[] parameterAnnotations) {
		for (final Annotation annot : parameterAnnotations) {
			if (annot instanceof ScriptParameter) {
				final ScriptParameter namedParameter = (ScriptParameter) annot;
				return namedParameter.name();
			}
		}
		return null;
	}

	public static List<Parameter> parseParameters(final Method method) {
		final ArrayList<Parameter> parameters = new ArrayList<Parameter>();

		for (int index = 0; index < method.getParameterTypes().length; index++) {
			final Parameter parameter = new Parameter();
			parameter.setClass(method.getParameterTypes()[index]);

			final ScriptParameter annotation = getParameterAnnotation(method.getParameterAnnotations()[index]);
			if (annotation != null) {
				parameter.setName(annotation.name());
				parameter.setOptional(ScriptParameter.Helper.isOptional(annotation));
				parameter.setDefault(annotation.defaultValue());
			}
			parameters.add(parameter);
		}

		// post process parameters: find unique names for unnamed parameters
		for (final Parameter parameter : parameters) {
			if (parameter.getName().isEmpty())
				parameter.setName(findName(parameters));
		}

		return parameters;
	}

	private static ScriptParameter getParameterAnnotation(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof ScriptParameter)
				return (ScriptParameter) annotation;
		}

		return null;
	}

	/**
	 * Find a unique name that is not used yet.
	 *
	 * @param parameters
	 *            list of available parameters
	 * @return unique unused parameter name
	 */
	private static String findName(final List<Parameter> parameters) {
		String name;
		int index = 1;
		boolean found;
		do {
			found = true;
			name = "param" + index;

			for (final Parameter parameter : parameters) {
				if (name.equals(parameter.getName())) {
					index++;
					found = false;
					break;
				}
			}

		} while (!found);

		return name;
	}

	protected String getDefaultValue(final Parameter parameter) {
		final String defaultStringValue = parameter.getDefaultValue().replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n");
		final Class<?> clazz = parameter.getClazz();

		// null as default value
		if (ScriptParameter.NULL.equals(defaultStringValue))
			return getNullString();

		// base datatypes
		if ((Integer.class.equals(clazz)) || (int.class.equals(clazz))) {
			try {
				return Integer.toString(Integer.parseInt(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Long.class.equals(clazz)) || (long.class.equals(clazz))) {
			try {
				return Long.toString(Long.parseLong(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Float.class.equals(clazz)) || (float.class.equals(clazz))) {
			try {
				return Float.toString(Float.parseFloat(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Double.class.equals(clazz)) || (double.class.equals(clazz))) {
			try {
				return Double.toString(Double.parseDouble(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Boolean.class.equals(clazz)) || (boolean.class.equals(clazz))) {
			return Boolean.parseBoolean(defaultStringValue) ? getTrueString() : getFalseString();
		}

		// undefined resolves to empty constructor
		if (ScriptParameter.UNDEFINED.equals(defaultStringValue)) {
			// look for empty constructor
			try {
				clazz.getConstructor();
				// empty constructor found, return class
				return classInstantiation(clazz, null);
			} catch (final SecurityException e) {
			} catch (final NoSuchMethodException e) {
			}
		}

		// look for string constructor
		try {
			clazz.getConstructor(String.class);
			// string constructor found, return class
			return classInstantiation(clazz, new String[] { "\"" + defaultStringValue + "\"" });
		} catch (final SecurityException e) {
		} catch (final NoSuchMethodException e) {
		}

		// special handling for string defaults passed to an Object.class
		if (clazz.isAssignableFrom(String.class))
			return classInstantiation(String.class, new String[] { "\"" + defaultStringValue + "\"" });

		return getNullString();
	}

	public static Collection<String> getMethodNames(final Method method) {
		final Set<String> methodNames = new HashSet<String>();
		methodNames.add(method.getName());

		final WrapToScript wrapAnnotation = method.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String name : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (!name.trim().isEmpty())
					methodNames.add(name.trim());
		}

		return methodNames;
	}

	public static String getPreExecutionCode(final IEnvironment environment, final Method method) {
		final StringBuffer code = new StringBuffer();

		for (final Object module : environment.getModules()) {
			if (module instanceof IScriptFunctionModifier)
				code.append(((IScriptFunctionModifier) module).getPreExecutionCode(method));
		}

		return code.toString();
	}

	public static String getPostExecutionCode(final IEnvironment environment, final Method method) {
		final StringBuffer code = new StringBuffer();

		for (final Object module : environment.getModules()) {
			if (module instanceof IScriptFunctionModifier)
				code.append(((IScriptFunctionModifier) module).getPostExecutionCode(method));
		}

		return code.toString();
	}

	/**
	 * Get string representation for <code>null</code> in target language.
	 *
	 * @return <code>null</code> in target language.
	 */
	protected abstract String getNullString();

	/**
	 * Get string representation for <code>true</code> in target language.
	 *
	 * @return <code>true</code> in target language.
	 */
	protected String getTrueString() {
		return Boolean.TRUE.toString();
	}

	/**
	 * Get string representation for <code>false</code> in target language.
	 *
	 * @return <code>false</code> in target language.
	 */
	protected String getFalseString() {
		return Boolean.FALSE.toString();
	}
}
