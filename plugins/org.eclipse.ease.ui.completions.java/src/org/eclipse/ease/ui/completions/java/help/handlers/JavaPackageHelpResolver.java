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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

public class JavaPackageHelpResolver extends JavaClassHelpResolver {

	public JavaPackageHelpResolver(final String packageName) {
		super(packageName, null);
	}

	@Override
	protected ITypeRoot resolveTypeRoot(final IJavaProject javaProject) {
		return null;
	}

	@Override
	protected IJavaElement resolveJavaElement(final ITypeRoot javaElement) {
		try {
			IPath elementPath = new Path(getPackageName());
			IJavaElement element = getJavaProject().findElement(elementPath);
			if (element instanceof IJavaElement)
				return element;

		} catch (final JavaModelException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		return null;
	}
}
