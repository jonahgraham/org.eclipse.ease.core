/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.modules.ui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;

public class ModulesTools {

	@Deprecated
	private ModulesTools() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates the signature of the method. If a parameter is annotated as optional (Scriptparameter) it is enclosed with [].
	 *
	 * @param method
	 *            inspected method
	 * @return Signature of Method.
	 */
	public static String getSignature(final Method method) {
		return getSignature(method, false);
	}

	/**
	 * Generates the signature of the method. If a parameter is annotated as optional (Scriptparameter) it is enclose with []. If showDefault is true the given
	 * default value is added. E.g. "foo(type1 [,type2 = defaultValue])".
	 *
	 * @param method
	 *            inspected method
	 * @return Signature of Method.
	 */
	public static String getSignature(final Method method, final boolean showDefault) {

		final Class<?>[] parameters = method.getParameterTypes();
		final List<Boolean> optional = new ArrayList<Boolean>();
		final List<String> defaultValue = new ArrayList<String>();
		int i = 0;
		for (final Annotation[] list : method.getParameterAnnotations()) {
			boolean optionalFlag = false;
			String defaultValueFlag = "";
			for (final Annotation annotation : list) {
				if (annotation.annotationType().equals(ScriptParameter.class)) {
					optionalFlag = ScriptParameter.Helper.isOptional((ScriptParameter) annotation);
					defaultValueFlag = ((ScriptParameter) annotation).defaultValue();
					if (parameters[i].equals(String.class)) {
						defaultValueFlag = "\"" + defaultValueFlag + "\"";
					}
				} else {
					optionalFlag = false;
				}

			}
			optional.add(optionalFlag);
			defaultValue.add(defaultValueFlag);
			i++;

		}
		final StringBuilder signature = new StringBuilder(method.getName());

		if (parameters.length != 0) {
			signature.append("(");
			i = 0;
			for (final Class<?> parameter : parameters) {
				if (optional.get(i)) {
					signature.append("[");
				}
				if (i != 0) {
					signature.append(",");
				}

				signature.append(parameter.getSimpleName());

				if (optional.get(i) & showDefault) {
					signature.append(" = ").append(defaultValue.get(i));
				}

				if (optional.get(i)) {
					signature.append("]");
				}

				i++;
			}
			signature.append(")");
		} else {
			signature.append("()");
		}

		return signature.toString();
	}

	/**
	 * Get the module owning the method provided.
	 *
	 * @param method
	 *            inspected method
	 * @return module containing the method or null.
	 */
	public static ModuleDefinition getDeclaringModule(final Method method) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(scriptService.getAvailableModules().values());

		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(method.getDeclaringClass()))
				return module;
		}

		return null;
	}

	/**
	 * Get the module owning the field provided.
	 *
	 * @param field
	 *            inspected field
	 * @return module containing the field or null.
	 */
	public static ModuleDefinition getDeclaringModule(final Field field) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(scriptService.getAvailableModules().values());

		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(field.getDeclaringClass()))
				return module;
		}

		return null;
	}

	public static int getOptionalParameterCount(Method method) {
		int optional = 0;

		for (final Annotation[] list : method.getParameterAnnotations()) {
			for (final Annotation annotation : list) {
				if ((annotation.annotationType().equals(ScriptParameter.class)) && (ScriptParameter.Helper.isOptional((ScriptParameter) annotation))) {
					optional++;
					break;
				}
			}
		}

		return optional;
	}
}
