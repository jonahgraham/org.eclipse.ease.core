/*******************************************************************************
 * Copyright (c) 2017 christian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     christian - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.groovy.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class MultiClassLoader extends ClassLoader {

	private final ClassLoader[] fParents;

	public MultiClassLoader(ClassLoader... parents) {
		super(parents[0]);

		fParents = parents;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		ClassNotFoundException exception = null;

		for (final ClassLoader classloader : fParents) {
			try {
				return classloader.loadClass(name);
			} catch (final ClassNotFoundException e) {
				exception = e;
			}
		}

		throw exception;
	}

	@Override
	public URL getResource(String name) {
		for (final ClassLoader classloader : fParents) {
			final URL candidate = classloader.getResource(name);
			if (candidate != null)
				return candidate;
		}

		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		IOException exception = null;

		for (final ClassLoader classloader : fParents) {
			try {
				final Enumeration<URL> candidate = classloader.getResources(name);
				if (candidate != null)
					return candidate;
			} catch (final IOException e) {
				exception = e;
			}
		}

		if (exception != null)
			throw exception;

		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		for (final ClassLoader classloader : fParents) {
			final InputStream candidate = classloader.getResourceAsStream(name);
			if (candidate != null)
				return candidate;
		}

		return null;
	}

	@Override
	public void setDefaultAssertionStatus(boolean enabled) {
		for (final ClassLoader classloader : fParents)
			classloader.setDefaultAssertionStatus(enabled);
	}

	@Override
	public void setPackageAssertionStatus(String packageName, boolean enabled) {
		for (final ClassLoader classloader : fParents)
			classloader.setPackageAssertionStatus(packageName, enabled);
	}

	@Override
	public void setClassAssertionStatus(String className, boolean enabled) {
		for (final ClassLoader classloader : fParents)
			classloader.setPackageAssertionStatus(className, enabled);
	}

	@Override
	public void clearAssertionStatus() {
		for (final ClassLoader classloader : fParents)
			classloader.clearAssertionStatus();
	}
}