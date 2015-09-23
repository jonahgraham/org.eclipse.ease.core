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

package org.eclipse.ease.ui.help.hovers;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.objectweb.asm.Type;

public class JavaMethodHelpResolver extends JavaClassHelpResolver {

	private final Method fMethod;

	public JavaMethodHelpResolver(final Method method) {
		super(method.getDeclaringClass().getPackage().getName(), method.getDeclaringClass().getSimpleName());

		fMethod = method;
	}

	@Override
	protected IJavaElement resolveJavaElement(final ITypeRoot javaElement) {
		if (javaElement instanceof ClassFile) {

			// create parameter type information

			final Class<?>[] methodParameters = fMethod.getParameterTypes();
			final String[] parameterTypes = new String[methodParameters.length];
			for (int index = 0; index < parameterTypes.length; index++)
				parameterTypes[index] = getDescriptor(methodParameters[index]);

			final IType type = ((ClassFile) javaElement).getType();
			if (type instanceof JavaElement)
				return new ResolvedBinaryMethod((JavaElement) type, fMethod.getName(), parameterTypes, getDescriptor(fMethod));
		}

		return null;
	}

	private static String getDescriptor(Method method) {
		final StringBuilder buffer = new StringBuilder();

		buffer.append(getDescriptor(method.getDeclaringClass()));
		buffer.append('.');
		buffer.append(method.getName());

		buffer.append(Type.getMethodDescriptor(method));

		final Class<?>[] exceptionTypes = method.getExceptionTypes();
		if (exceptionTypes.length > 0) {
			buffer.append('|');
			for (final Class<?> exception : exceptionTypes)
				buffer.append(getDescriptor(exception));
		}

		return buffer.toString();
	}
}
