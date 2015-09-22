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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ui.help.hovers.internal.VirtualProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jface.text.Region;
import org.objectweb.asm.Type;

public class JavaClassHelpResolver implements IHelpResolver {

	private final String fPackageName;
	private final String fClassName;
	private Class<?> fClazz;

	public JavaClassHelpResolver(final String packageName, final String className) {
		fPackageName = packageName.replace('.', '/');
		fClassName = className;

		try {
			fClazz = JavaClassHelpResolver.class.getClassLoader().loadClass(packageName + "." + className.replace('.', '$'));
		} catch (final ClassNotFoundException e) {
			fClazz = null;
		}
	}

	@Override
	public String resolveHelp() {
		final IProject project = new VirtualProject();
		final IJavaProject javaProject = JavaCore.create(project);

		final ITypeRoot typeRoot = resolveTypeRoot(javaProject);
		final IJavaElement javaElement = resolveJavaElement(typeRoot);

		final JavadocBrowserInformationControlInput hoverInfo = JavadocHover.getHoverInfo(new IJavaElement[] { javaElement }, typeRoot, new Region(0, 1), null);
		if (hoverInfo != null)
			return hoverInfo.getHtml();

		return null;
	}

	@Override
	public String resolveHTMLHelp() {
		return resolveHelp();
	}

	protected ITypeRoot resolveTypeRoot(final IJavaProject javaProject) {
		IPath elementPath = new Path(fPackageName);
		elementPath = elementPath.append(fClassName.replace('.', '$') + ".java");

		try {
			final IJavaElement root = javaProject.findElement(elementPath);
			if (root instanceof ITypeRoot)
				return (ITypeRoot) root;
		} catch (final JavaModelException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		return null;
	}

	protected IJavaElement resolveJavaElement(final ITypeRoot javaElement) {

		if (javaElement instanceof JavaElement)
			return new ResolvedBinaryType((JavaElement) javaElement, fClazz.getSimpleName(), getDescriptor(fClazz));

		// if (javaElement instanceof JavaElement)
		// return new ResolvedBinaryType((JavaElement) javaElement, fClassName, "L" + fPackageName + "/" + fClassName);

		return null;
	}

	protected static String getDescriptor(Class<?> clazz) {
		return Type.getDescriptor(clazz);
	}
}
