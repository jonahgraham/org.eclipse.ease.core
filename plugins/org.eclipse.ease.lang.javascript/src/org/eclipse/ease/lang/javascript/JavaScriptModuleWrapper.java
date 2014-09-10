/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Arthur Daussy - Allow optional parameter
 *******************************************************************************/
package org.eclipse.ease.lang.javascript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractModuleWrapper;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;

public class JavaScriptModuleWrapper extends AbstractModuleWrapper {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<String>();

	static {
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("while");
		RESERVED_KEYWORDS.add("delete");
		// TODO Complete this list
	}

	private static boolean isValidMethodName(final String methodName) {
		return JavaScriptHelper.isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append("new Packages.").append(clazz.getName()).append("(");

		if (parameters != null) {
			for (final String parameter : parameters)
				code.append(parameter).append(", ");

			if (parameters.length > 0)
				code.delete(code.length() - 2, code.length());
		}

		code.append(")");

		return code.toString();
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		final StringBuilder javaScriptCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = parseParameters(method);

		// build parameter string
		final StringBuilder parameterList = new StringBuilder();
		for (final Parameter parameter : parameters)
			parameterList.append(", ").append(parameter.getName());

		if (parameterList.length() > 2)
			parameterList.delete(0, 2);

		final StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append(verifyParameters(parameters));

		// insert hooked pre execution code
		body.append(getPreExecutionCode(environment, method));

		// insert method call
		body.append("\tvar ").append(IScriptFunctionModifier.RESULT_NAME).append(" = ").append(moduleVariable).append('.').append(method.getName()).append('(');
		body.append(parameterList);
		body.append(");\n");

		// insert hooked post execution code
		body.append(getPostExecutionCode(environment, method));

		// insert return statement
		body.append("\treturn ").append(IScriptFunctionModifier.RESULT_NAME).append(";\n");

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.logError("The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved",
						Activator.PLUGIN_ID);

			} else if (!name.isEmpty()) {
				javaScriptCode.append("function ").append(name).append("(").append(parameterList).append(") {\n");
				javaScriptCode.append(body);
				javaScriptCode.append("}\n");
			}
		}

		return javaScriptCode.toString();
	}

	@Override
	public String createStaticFieldWrapper(final IEnvironment environment, final Field field) {
		return "const " + JavaScriptHelper.getSaveName(field.getName()) + " = Packages." + field.getDeclaringClass().getName() + "." + field.getName() + ";\n";
	}

	@Override
	protected String getNullString() {
		return "null";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return JavaScriptHelper.getSaveName(variableName);
	}

	private StringBuilder verifyParameters(final List<Parameter> parameters) {
		final StringBuilder data = new StringBuilder();

		if (!parameters.isEmpty()) {
			final Parameter parameter = parameters.get(parameters.size() - 1);
			data.append("\tif (typeof " + parameter.getName() + " === \"undefined\") {\n");
			if (parameter.isOptional()) {
				data.append("\t\t" + parameter.getName() + " = " + getDefaultValue(parameter) + ";\n");
			} else {
				data.append("\t\tthrow new java.lang.RuntimeException('Parameter " + parameter.getName() + " is not optional');\n");

			}
			data.append(verifyParameters(parameters.subList(0, parameters.size() - 1)));
			data.append("\t}\n");
		}

		return data;
	}
}
