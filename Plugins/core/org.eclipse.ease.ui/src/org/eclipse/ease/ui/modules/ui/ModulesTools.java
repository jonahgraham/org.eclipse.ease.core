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
	 * Generates the signature of the method. If a parameter is annotated as
	 * optional (Scriptparameter) it is enclosed with [].
	 * 
	 * @param method
	 *            inspected method
	 * @return Signature of Method.
	 */
	public static String getSignature(Method method) {
		return getSignature(method, false);
	}

	/**
	 * Generates the signature of the method. If a parameter is annotated as
	 * optional (Scriptparameter) it is enclose with []. If showDefault is true
	 * the given default value is added. E.g.
	 * "foo(type1 [,type2 = defaultValue])".
	 * 
	 * @param method
	 *            inspected method
	 * @return Signature of Method.
	 */
	public static String getSignature(Method method, boolean showDefault) {

		Class<?>[] parameters = method.getParameterTypes();
		List<Boolean> optional = new ArrayList<Boolean>();
		List<String> defaultValue = new ArrayList<String>();
		int i = 0;
		for (Annotation[] list : method.getParameterAnnotations()) {
			boolean optionalFlag = false;
			String defaultValueFlag = "";
			for (Annotation annotation : list) {
				if (annotation.annotationType().equals(ScriptParameter.class)) {
					optionalFlag = ((ScriptParameter) annotation).optional();
					defaultValueFlag = ((ScriptParameter) annotation)
							.defaultValue();
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
		StringBuilder signature = new StringBuilder(method.getName());

		if (parameters.length != 0) {
			signature.append("(");
			i = 0;
			for (Class<?> parameter : parameters) {
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
	public static ModuleDefinition getDeclaringModule(Method method) {

		final IScriptService scriptService = (IScriptService) PlatformUI
				.getWorkbench().getService(IScriptService.class);
		List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(
				scriptService.getAvailableModules().values());

		for (ModuleDefinition module : modules) {
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
	public static ModuleDefinition getDeclaringModule(Field field) {

		final IScriptService scriptService = (IScriptService) PlatformUI
				.getWorkbench().getService(IScriptService.class);
		List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(
				scriptService.getAvailableModules().values());

		for (ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(field.getDeclaringClass()))
				return module;
		}

		return null;

	}
}
