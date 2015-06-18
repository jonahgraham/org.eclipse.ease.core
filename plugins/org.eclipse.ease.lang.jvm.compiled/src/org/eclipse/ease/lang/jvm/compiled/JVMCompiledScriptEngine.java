/*******************************************************************************
 * Copyright (c) 2014 Nicolas Rouquette, JPL, Caltech and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Rouquette, JPL, Caltech - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.jvm.compiled;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class JVMCompiledScriptEngine extends AbstractScriptEngine implements IScriptEngine {

	private final Map<String, Object> fVariables = new HashMap<String, Object>();

	public JVMCompiledScriptEngine() {
		super("JVMCompiled");
	}

	@Override
	public void terminateCurrent() {
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return fVariables.get(name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return fVariables;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return fVariables.containsKey(name);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		fVariables.put(name, content);
	}

	@Override
	protected Object internalRemoveVariable(final String name) {
		return fVariables.remove(name);
	}

	@Override
	public String getSaveVariableName(final String name) {
		throw new RuntimeException("Functionality not supported by this engine");
	}

	@Override
	public void registerJar(final URL url) {
		throw new RuntimeException("Functionality not supported by this engine");
	}

	@Override
	protected boolean setupEngine() {
		return true;
	}

	@Override
	protected boolean teardownEngine() {
		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {

		Class<?> clazz = loadClass(reference);
		if (clazz != null) {

			Method mainMethod = clazz.getMethod("main", String[].class);
			if (mainMethod != null) {

				ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(clazz.getClassLoader());

				try {
					// before we run main, we try to initialize the class
					try {
						Method initialize = clazz.getMethod("initialize", InputStream.class, PrintStream.class, PrintStream.class);
						initialize.invoke(null, getInputStream(), getOutputStream(), getErrorStream());
					} catch (NoSuchMethodException e) {
						// initialize method not available, to be ignored
					}

					Object result = mainMethod.invoke(null, internalGetVariable("argv"));
					return result;

				} finally {
					Thread.currentThread().setContextClassLoader(localClassLoader);
				}
			}
		}

		throw new ClassNotFoundException();
	}

	/**
	 * Loads a class definition for a given source file.
	 *
	 * @param reference
	 *            file name or {@link IFile} instance of the source file.
	 * @return class definition
	 * @throws ClassNotFoundException
	 *             If the class was not found
	 */
	public static Class<?> loadClass(final Object reference) throws JavaModelException, MalformedURLException, ClassNotFoundException {
		Object file = ResourceTools.resolveFile(reference, null, true);

		// find source project and resolve dependencies
		SimpleEntry<IFile, IBundleProjectDescription> pair = getBundleProjectDescription(file);
		if (pair != null) {
			IFile sourceFile = pair.getKey();
			IBundleProjectDescription scriptBundleProject = pair.getValue();
			List<URL> urls = new ArrayList<URL>();
			IProject project = scriptBundleProject.getProject();
			IJavaProject javaProject = JavaCore.create(project);

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IClasspathEntry[] cpEntries = javaProject.getRawClasspath();
			if (cpEntries != null) {
				for (IClasspathEntry cpEntry : cpEntries) {
					// The script bundle project may have a ".classpath"
					// dependency on another
					// Eclipse "source" project (could be Java or Scala or
					// anything else based on the Eclipse Java nature)
					if ((cpEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) && (cpEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE)) {
						IPath cpPath = cpEntry.getPath();
						IProject cpProject = root.getProject(cpPath.toString());
						if (cpProject != null) {
							IJavaProject jcpProject = JavaCore.create(cpProject);
							IPath output = jcpProject.getOutputLocation();
							IResource bin = root.findMember(output);
							IPath binPath = bin.getRawLocation();
							URL url = binPath.toFile().toURI().toURL();
							urls.add(url);
						}
					}
				}
			}

			IPath output = javaProject.getOutputLocation();
			IResource bin = root.findMember(output);
			IPath binPath = bin.getRawLocation();
			URL url = binPath.toFile().toURI().toURL();
			urls.add(url);

			IPath wsPath = sourceFile.getProjectRelativePath();
			IPath wsSource = wsPath.removeFirstSegments(1);

			IRequiredBundleDescription[] requiredBundles = scriptBundleProject.getRequiredBundles();
			List<Bundle> bundles = new ArrayList<Bundle>();
			if (requiredBundles != null) {
				for (IRequiredBundleDescription requiredBundle : requiredBundles) {
					String id = requiredBundle.getName();
					Bundle b = Platform.getBundle(id);
					if (b != null)
						// The script bundle project (in the Eclipse workspace)
						// has a MANIFEST dependency
						// on an Eclipse bundle (in the Eclipse installation)
						bundles.add(b);
					else {
						// The script bundle project (in the Eclipse workspace)
						// has a MANIFEST dependency
						// on an Eclipse plugin that is not in the Eclipse
						// installation -- so it must be in the Eclipse
						// workspace...
						IProject bProject = root.getProject("/" + id);
						if (bProject != null) {
							IJavaProject bjProject = JavaCore.create(bProject);
							IPath bOutput = bjProject.getOutputLocation();
							IResource bBin = root.findMember(bOutput);
							IPath bBinPath = bBin.getRawLocation();
							URL bUrl = bBinPath.toFile().toURI().toURL();
							urls.add(bUrl);
						}
					}
				}
			}

			URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), JVMCompiledScriptEngine.class.getClassLoader());
			try {
				IJavaElement wsElement = javaProject.findElement(wsSource);
				if (wsElement instanceof ICompilationUnit) {
					ICompilationUnit u = (ICompilationUnit) wsElement;
					String uName = u.getElementName();
					int dot = uName.indexOf('.');

					String qName = uName.substring(0, dot);

					IJavaElement uParent = u.getParent();
					while (uParent instanceof IPackageFragment) {
						IPackageFragment uPkg = (IPackageFragment) uParent;
						String pkgName = uPkg.getElementName();
						qName = pkgName + "." + qName;
						uParent = uParent.getParent();
					}

					return cl.loadClass(qName);
				}
			} finally {
				// TODO needs Java 1.7
				// cl.close();
			}
		}
		return null;
	}

	private static AbstractMap.SimpleEntry<IFile, IBundleProjectDescription> getBundleProjectDescription(final Object reference) {

		IFile sourceFile = null;

		if (reference instanceof IFile)
			sourceFile = (IFile) reference;

		else if (reference instanceof File) {
			URI scriptURI = ((File) reference).toURI();
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] files = workspaceRoot.findFilesForLocationURI(scriptURI);
			if ((files != null) && (files.length == 1))
				sourceFile = files[0];
		}

		if (sourceFile != null) {
			Bundle bundle = FrameworkUtil.getBundle(IBundleProjectService.class);
			BundleContext context = bundle.getBundleContext();
			ServiceReference<IBundleProjectService> ref = context.getServiceReference(IBundleProjectService.class);
			IBundleProjectService service = context.getService(ref);

			try {
				IBundleProjectDescription projectDescription = service.getDescription(sourceFile.getProject());
				if (projectDescription != null)
					return new AbstractMap.SimpleEntry<IFile, IBundleProjectDescription>(sourceFile, projectDescription);

			} catch (IllegalArgumentException ex) {
				// ignore
			} catch (CoreException e) {
				// ignore
			}
		}

		return null;
	}
}
