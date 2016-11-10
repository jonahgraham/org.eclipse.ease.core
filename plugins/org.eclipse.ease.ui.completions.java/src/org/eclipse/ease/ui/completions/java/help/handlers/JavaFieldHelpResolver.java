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

import java.lang.reflect.Field;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryField;

public class JavaFieldHelpResolver extends JavaClassHelpResolver {

	private final Field fField;

	public JavaFieldHelpResolver(final Field field) {
		super(field.getDeclaringClass().getPackage().getName(), field.getDeclaringClass().getSimpleName());

		fField = field;
	}

	@Override
	protected IJavaElement resolveJavaElement(final ITypeRoot javaElement) {
		if (javaElement instanceof ClassFile) {

			final IType type = ((ClassFile) javaElement).getType();
			if (type instanceof JavaElement)
				return new ResolvedBinaryField((JavaElement) type, fField.getName(), getDescriptor(fField));
		}

		return null;
	}

	private static String getDescriptor(Field field) {
		final StringBuilder buffer = new StringBuilder();

		buffer.append(getDescriptor(field.getDeclaringClass()));
		buffer.append('.');
		buffer.append(field.getName());
		buffer.append(')');
		buffer.append(getDescriptor(field.getType()));

		return buffer.toString();
	}
}
