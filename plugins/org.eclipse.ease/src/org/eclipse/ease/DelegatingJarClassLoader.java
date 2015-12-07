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
package org.eclipse.ease;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * A class loader for script interpreters. This class loader allows to add arbitrary {@link URL}s that will be queried for class loading. When the requested
 * class cannot be found, it delegates loading to its parent classloader.
 */
public class DelegatingJarClassLoader extends ClassLoader {

	/** Internal URL classloader. */
	private URLClassLoader fURLClassLoader = null;

	public DelegatingJarClassLoader(final ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		// try to load from jars
		if (fURLClassLoader != null) {
			try {
				return fURLClassLoader.loadClass(name);
			} catch (ClassNotFoundException e) {
				// ignore, class not found in registered JARs
			}
		}

		// not found in jars, delegate to parent classloader
		return super.loadClass(name);
	}

	/**
	 * Add a URL to the search path of the classloader. Currently detects classes only, not resources.
	 *
	 * @param url
	 *            url to add to classpath
	 */
	public void registerURL(final URL url) {
		if (fURLClassLoader == null)
			fURLClassLoader = URLClassLoader.newInstance(new URL[] { url });

		else {
			URL[] registeredURLs = fURLClassLoader.getURLs();
			List<URL> urlList = Arrays.asList(registeredURLs);

			if (!urlList.contains(url)) {
				// new URL, add to list
				URL[] updatedURLs = Arrays.copyOf(registeredURLs, registeredURLs.length + 1);
				updatedURLs[updatedURLs.length - 1] = url;
				fURLClassLoader = URLClassLoader.newInstance(updatedURLs);
			}
		}
	}
}
