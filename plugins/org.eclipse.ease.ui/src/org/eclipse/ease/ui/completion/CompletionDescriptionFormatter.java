/*******************************************************************************
 * Copyright (c) 2015 kloeschmartin and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     kloeschmartin - initial API and implementation
 *     Vidura Mudalige - link with ModuleHelp to get improved tooltips
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.help.hovers.ModuleHelp;

/**
 * Static string formatting class to describe code completion suggestions.
 * 
 * @author kloeschmartin
 *
 */
@SuppressWarnings("rawtypes")
public class CompletionDescriptionFormatter {

	public static String format(Field field, ModuleDefinition module) {
		if (ModuleHelp.getConstantHelpTip(field) == null) {
			return String.format("Public member of module %s with type %s.", module.getName(), field.getType().getName());
		} else
			return ModuleHelp.getConstantHelpTip(field);
	}

	public static String format(Field field, Class clazz) {
		return String.format("Public member of class %s with type %s.", clazz.getName(), field.getType().getName());
	}

	public static String format(Class clazz) {
		return String.format("Local variable with type %s.", clazz.getName());
	}

	public static String format(Object obj) {
		return format(obj.getClass());
	}

	public static String format(Method method, ModuleDefinition module) {

		if (ModuleHelp.getMethodHelpTip(method) == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Public method of module %s.\n", module.getName()));
			sb.append("Signature and overloads:\n");
			for (Method overload : module.getMethods()) {
				if (overload.getName().equals(method.getName())) {
					sb.append(overload.toGenericString());
					sb.append("\n");
				}
			}
			return sb.toString();
		} else
			return ModuleHelp.getMethodHelpTip(method);
	}

	public static String format(Method method, Class clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Public method of class %s.\n", clazz.getName()));
		sb.append("Signature and overloads:\n");
		for (Method overload : clazz.getMethods()) {
			if (overload.getName().equals(method.getName())) {
				sb.append(overload.toGenericString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static String format(Method method, Collection<Method> localMethods) {
		StringBuilder sb = new StringBuilder();
		sb.append("Public function.\n");
		sb.append("Signature and overloads:\n");
		for (Method overload : localMethods) {
			if (overload.getName().equals(method.getName())) {
				sb.append(overload.toGenericString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
