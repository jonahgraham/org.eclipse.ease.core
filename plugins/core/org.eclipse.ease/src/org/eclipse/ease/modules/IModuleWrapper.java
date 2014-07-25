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
package org.eclipse.ease.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface IModuleWrapper {

	String getSaveVariableName(String variableName);

	String createFunctionWrapper(IEnvironment environment, String moduleVariable, Method method);

	String classInstantiation(Class<?> clazz, String[] parameters);

	String createStaticFieldWrapper(IEnvironment environment, Field field);
}
