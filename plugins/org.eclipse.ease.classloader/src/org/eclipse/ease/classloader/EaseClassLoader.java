/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.IScriptEngine;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

/**
 * A classloader using 'Eclipse-BuddyPolicy: global' as class loading strategy. It further allows to register additional jar files to be looked up.
 */
public class EaseClassLoader extends ClassLoader {

	private final Map<IScriptEngine, URLClassLoader> fRegisteredJars = new HashMap<>();

	/** Marker that we are currently looking within a specific URLClassLoader. */
	private final Collection<URLClassLoader> fTraversingURLClassLoader = new HashSet<>();

	/**
	 * Constructor for the class loader.
	 */
	public EaseClassLoader() {
		super(FrameworkUtil.getBundle(EaseClassLoader.class).adapt(BundleWiring.class).getClassLoader());
	}

	/**
	 * Constructor using a given parent classloader. When using this classloader the Eclipse-BuddyPolicy from the parent classloader bundle will be used.
	 *
	 * @param parent
	 *            parent classloader
	 */
	public EaseClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException {
		// try to load from jars
		final Job currentJob = Job.getJobManager().currentJob();
		final URLClassLoader classLoader = fRegisteredJars.get(currentJob);
		if (classLoader != null) {
			// the EaseClassLoader will query its parent (this) if it cannot find the requested class, keep a marker to break the cycle
			if (!fTraversingURLClassLoader.contains(classLoader)) {
				fTraversingURLClassLoader.add(classLoader);
				try {
					final Class<?> clazz = classLoader.loadClass(name);
					if (clazz != null)
						return clazz;

				} catch (final ClassNotFoundException e) {
					// ignore, class not found in registered JARs
				} finally {
					// clear marker
					fTraversingURLClassLoader.remove(classLoader);
				}
			}
		}

		// not found in jars, delegate to eclipse loader
		return super.findClass(name);
	}

	/**
	 * Add a URL to the search path of the classloader. Currently detects classes only, not resources.
	 *
	 * @param engine
	 *            script engine used
	 * @param url
	 *            url to add to classpath
	 */
	public void registerURL(final IScriptEngine engine, final URL url) {
		// engine needs to be registered as we use a single classloader for multiple script engines.
		if (!fRegisteredJars.containsKey(engine))
			fRegisteredJars.put(engine, URLClassLoader.newInstance(new URL[] { url }, this));

		else {
			final URL[] registeredURLs = fRegisteredJars.get(engine).getURLs();
			final List<URL> urlList = Arrays.asList(registeredURLs);
			if (!urlList.contains(url)) {
				// new URL, add to list
				final URL[] updatedURLs = Arrays.copyOf(registeredURLs, registeredURLs.length + 1);
				updatedURLs[updatedURLs.length - 1] = url;
				fRegisteredJars.put(engine, URLClassLoader.newInstance(updatedURLs, this));
			}
		}
	}

	public void unregisterEngine(final IScriptEngine engine) {
		fRegisteredJars.remove(engine);
	}
}
