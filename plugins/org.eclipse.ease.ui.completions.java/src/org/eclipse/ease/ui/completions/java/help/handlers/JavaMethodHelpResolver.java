/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completions.java.help.handlers;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

public class JavaMethodHelpResolver extends JavaClassHelpResolver {

	private final Method fMethod;

	public JavaMethodHelpResolver(final Method method) {
		super(method.getDeclaringClass().getPackage().getName(), method.getDeclaringClass().getSimpleName());

		fMethod = method;
	}

	@Override
	protected IJavaElement resolveJavaElement(final ITypeRoot javaElement) {
		if (javaElement instanceof IClassFile) {

			// create parameter type information
			final Class<?>[] methodParameters = fMethod.getParameterTypes();
			final String[] parameterTypes = new String[methodParameters.length];
			for (int index = 0; index < parameterTypes.length; index++)
				parameterTypes[index] = getDescriptor(methodParameters[index]);

			return ((IClassFile) javaElement).getType().getMethod(fMethod.getName(), parameterTypes);
		}

		return null;
	}
}
