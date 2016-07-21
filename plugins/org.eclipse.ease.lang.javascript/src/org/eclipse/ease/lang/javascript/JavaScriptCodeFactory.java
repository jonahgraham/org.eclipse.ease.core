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

import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractCodeFactory;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;
import org.eclipse.ease.modules.ModuleHelper;

public class JavaScriptCodeFactory extends AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<String>();

	static {
		RESERVED_KEYWORDS.add("abstract");
		RESERVED_KEYWORDS.add("arguments");
		RESERVED_KEYWORDS.add("boolean");
		RESERVED_KEYWORDS.add("break");
		RESERVED_KEYWORDS.add("byte");
		RESERVED_KEYWORDS.add("case");
		RESERVED_KEYWORDS.add("catch");
		RESERVED_KEYWORDS.add("char");
		RESERVED_KEYWORDS.add("class");
		RESERVED_KEYWORDS.add("const");
		RESERVED_KEYWORDS.add("continue");
		RESERVED_KEYWORDS.add("debugger");
		RESERVED_KEYWORDS.add("default");
		RESERVED_KEYWORDS.add("delete");
		RESERVED_KEYWORDS.add("do");
		RESERVED_KEYWORDS.add("double");
		RESERVED_KEYWORDS.add("else");
		RESERVED_KEYWORDS.add("enum");
		RESERVED_KEYWORDS.add("eval");
		RESERVED_KEYWORDS.add("export");
		RESERVED_KEYWORDS.add("extends");
		RESERVED_KEYWORDS.add("false");
		RESERVED_KEYWORDS.add("final");
		RESERVED_KEYWORDS.add("finally");
		RESERVED_KEYWORDS.add("float");
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("function");
		RESERVED_KEYWORDS.add("goto");
		RESERVED_KEYWORDS.add("if");
		RESERVED_KEYWORDS.add("implements");
		RESERVED_KEYWORDS.add("import");
		RESERVED_KEYWORDS.add("in");
		RESERVED_KEYWORDS.add("instanceof");
		RESERVED_KEYWORDS.add("int");
		RESERVED_KEYWORDS.add("interface");
		RESERVED_KEYWORDS.add("let");
		RESERVED_KEYWORDS.add("long");
		RESERVED_KEYWORDS.add("native");
		RESERVED_KEYWORDS.add("new");
		RESERVED_KEYWORDS.add("null");
		RESERVED_KEYWORDS.add("package");
		RESERVED_KEYWORDS.add("private");
		RESERVED_KEYWORDS.add("protected");
		RESERVED_KEYWORDS.add("public");
		RESERVED_KEYWORDS.add("return");
		RESERVED_KEYWORDS.add("short");
		RESERVED_KEYWORDS.add("static");
		RESERVED_KEYWORDS.add("super");
		RESERVED_KEYWORDS.add("switch");
		RESERVED_KEYWORDS.add("synchronized");
		RESERVED_KEYWORDS.add("this");
		RESERVED_KEYWORDS.add("throw");
		RESERVED_KEYWORDS.add("throws");
		RESERVED_KEYWORDS.add("transient");
		RESERVED_KEYWORDS.add("true");
		RESERVED_KEYWORDS.add("try");
		RESERVED_KEYWORDS.add("typeof");
		RESERVED_KEYWORDS.add("var");
		RESERVED_KEYWORDS.add("void");
		RESERVED_KEYWORDS.add("volatile");
		RESERVED_KEYWORDS.add("while");
		RESERVED_KEYWORDS.add("with");
		RESERVED_KEYWORDS.add("yield");
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
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

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

		// insert deprecation warnings

		if (ModuleHelper.isDeprecated(method))
			body.append("\tprintError('" + method.getName() + "() is deprecated. Consider updating your code.');\n");

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
				Logger.error(PluginConstants.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				javaScriptCode.append("function ").append(name).append("(").append(parameterList).append(") {\n");
				javaScriptCode.append(body);
				javaScriptCode.append("}\n");
			}
		}

		return javaScriptCode.toString();
	}

	@Override
	public String createFinalFieldWrapper(final IEnvironment environment, final String moduleVariable, final Field field) {
		return "var " + JavaScriptHelper.getSaveName(field.getName()) + " = " + moduleVariable + "." + field.getName() + ";\n";
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

	@Override
	protected String getMultiLineCommentStartToken() {
		return "/*";
	}

	@Override
	protected String getMultiLineCommentEndToken() {
		return "*/";
	}

}
