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
package org.eclipse.ease.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;

public final class ModuleHelper {

	@Deprecated
	private ModuleHelper() {
	}

	/**
	 * Returns a list of exported methods. Any public method marked by a @WrapToScript annotation is exported. If no annotations are found all public methods
	 * are returned.
	 *
	 * @param clazz
	 *            class to be evaluated
	 * @return list of methods
	 */
	public static List<Method> getMethods(final Class<?> clazz) {

		if ((clazz == null) || (clazz.getMethods().length == 0))
			return Collections.emptyList();

		List<Method> methods = new ArrayList<Method>();
		boolean wrapping = ModuleHelper.hasWrapToScript(clazz);
		for (Method method : clazz.getMethods()) {
			if ((Modifier.isPublic(method.getModifiers()) && (!wrapping || method.isAnnotationPresent(WrapToScript.class))))
				methods.add(method);
		}

		return methods;
	}

	/**
	 * Returns a List of exported fields. Any public static field marked by a @WrapToScript annotation is exported. If no annotations are found all public
	 * static fields are returned.
	 *
	 * @param clazz
	 *            Class to be evaluated
	 * @return List of Fields
	 */
	public static List<Field> getFields(final Class<?> clazz) {

		if ((clazz == null) || (clazz.getDeclaredFields().length == 0))
			return Collections.emptyList();

		List<Field> fields = new ArrayList<Field>();
		boolean wrapping = ModuleHelper.hasWrapToScript(clazz);
		for (Field field : clazz.getDeclaredFields()) {
			if ((Modifier.isFinal(field.getModifiers()))
					&& (Modifier.isPublic(field.getModifiers()) && (!wrapping || field.isAnnotationPresent(WrapToScript.class))))
				fields.add(field);
		}

		return fields;
	}

	/**
	 * Returns if the Class to be evaluated contains {@link WrapToScript} annotations.
	 *
	 * @param clazz
	 *            class to be evaluated
	 * @return <code>true</code> when clazz contains {@link WrapToScript} annotations
	 */
	private static boolean hasWrapToScript(final Class<?> clazz) {

		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(WrapToScript.class))
				return true;
		}

		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(WrapToScript.class))
				return true;
		}

		return false;
	}

	/**
	 * Resolve a relative module name to its absolute name. When only the last part of a module name is provided (without path), this method tries to locate the
	 * module and returns its absolute path. If 2 modules with the same name are detected, a {@link RuntimeException} is thrown.
	 *
	 * @param identifier
	 *            module identifier
	 * @return absolute module name
	 */
	public static String resolveName(final String identifier) {
		final IScriptService scriptService = ScriptService.getService();
		Map<String, ModuleDefinition> availableModules = scriptService.getAvailableModules();

		// check for absolute path
		if (identifier.startsWith("/")) {

			// check for valid, absolute path
			if (availableModules.containsKey(identifier))
				return identifier;

			// path is already absolute, module does not exist
			return null;
		}

		IPath searchPath = new Path(identifier);
		if ((searchPath.segmentCount() == 1) && (!searchPath.isAbsolute())) {
			// only module name given
			for (Entry<String, ModuleDefinition> module : availableModules.entrySet()) {
				if (new Path(module.getKey()).lastSegment().equals(identifier)) {
					// candidate detected
					if (searchPath.isAbsolute())
						// we already had one candidate, name is ambiguous
						throw new RuntimeException("Module identifier \"" + identifier + "\" is ambiguous. Use full path name to load.");

					searchPath = module.getValue().getPath();
				}
			}
		}

		return searchPath.toString();
	}
}
