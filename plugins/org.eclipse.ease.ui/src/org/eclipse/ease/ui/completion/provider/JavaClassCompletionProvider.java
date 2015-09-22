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
package org.eclipse.ease.ui.completion.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.help.hovers.IHelpResolver;
import org.eclipse.ease.ui.help.hovers.JavaClassHelpResolver;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class JavaClassCompletionProvider extends AbstractCompletionProvider {

	/** Maps Package name -> contained classes. */
	private static Map<String, Collection<String>> CLASSES = null;

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && (context.getType() == Type.PACKAGE);
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		if (getClasses().get(context.getPackage()) != null) {
			for (final String className : getClasses().get(context.getPackage())) {
				// add class name
				final IHelpResolver helpResolver = new JavaClassHelpResolver(context.getPackage(), className);

				// retrieve image
				ImageDescriptor descriptor = null;

				try {
					final Class<?> clazz = getClass().getClassLoader().loadClass(context.getPackage() + "." + className.replace('.', '$'));
					if (clazz.isEnum())
						descriptor = JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_ENUM);
					else if (clazz.isInterface())
						descriptor = JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_INTERFACE);
				} catch (final ClassNotFoundException e) {
					// if we cannot find the class, use the default image
				}

				if (descriptor == null)
					descriptor = JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_CLASS);

				addProposal(proposals, context, className, className, descriptor, ScriptCompletionProposal.ORDER_CLASS, helpResolver);
			}
		}

		return proposals;
	}

	private static Map<String, Collection<String>> getClasses() {
		if (CLASSES == null) {
			CLASSES = new HashMap<String, Collection<String>>();

			// read java classes
			try {
				final URL url = new URL(
						"platform:/plugin/org.eclipse.ease.ui/resources/java" + System.getProperty("java.runtime.version").charAt(2) + " classes.txt");
				final InputStream inputStream = url.openConnection().getInputStream();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String fullQualifiedName;
				while ((fullQualifiedName = reader.readLine()) != null) {
					addClass(fullQualifiedName);
				}

				reader.close();

			} catch (final IOException e) {
				Logger.logError("Cannot read class list for code completion", e);
			}

			// read eclipse classes
			final BundleContext context = FrameworkUtil.getBundle(JavaClassCompletionProvider.class).getBundleContext();
			for (final Bundle bundle : context.getBundles()) {

				final Collection<String> exportedPackages = JavaPackagesCompletionProvider.getExportedPackages(bundle);

				// first look for class signatures in manifest, so we do not need to parse the whole bundle
				boolean signedContent = false;
				try {
					final URL manifest = bundle.getEntry("/META-INF/MANIFEST.MF");
					final InputStream inputStream = manifest.openConnection().getInputStream();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					while ((line = reader.readLine()) != null) {
						if ((line.startsWith("Name:")) && (line.endsWith(".class")) && (!line.contains("$")) && (!line.contains("package-info"))) {
							final String fullQualifiedName = line.substring(5, line.length() - 6).trim().replace('/', '.');
							final String packageName = fullQualifiedName.contains(".") ? fullQualifiedName.substring(0, fullQualifiedName.lastIndexOf('.'))
									: "";
							if (exportedPackages.contains(packageName))
								addClass(fullQualifiedName);

							signedContent = true;
						}
					}
				} catch (final IOException e) {
					Logger.logError("Could not parse manifest of bundle \"" + bundle.getBundleId() + "\"", e);
				}

				if (!signedContent) {
					// we did not find a signed bundle, try to parse the bundle

					try {
						final File bundleFile = FileLocator.getBundleFile(bundle);

						if (bundleFile.isDirectory()) {
							// bundle stored as folder
							for (final String packageName : exportedPackages) {
								final String packagePath = bundleFile.getAbsolutePath() + File.separatorChar + packageName.replace('.', File.separatorChar);
								final File packageFile = new File(packagePath);
								if (packageFile.isDirectory()) {
									for (final String candidate : packageFile.list()) {
										if ((candidate.endsWith(".class")) && (!candidate.contains("$")))
											addClass(packageName + "." + candidate.substring(0, candidate.length() - 6));
									}
								}
							}

						} else if (bundleFile.isFile()) {
							// bundle stored as jar
							final JarFile jarFile = new JarFile(bundleFile);
							final Enumeration<JarEntry> entries = jarFile.entries();
							while (entries.hasMoreElements()) {
								final String candidate = entries.nextElement().getName();
								if ((candidate.endsWith(".class")) && (!candidate.contains("$"))) {
									final String fullQualifiedName = candidate.substring(0, candidate.length() - 6).replace('/', '.');
									final String packageName = fullQualifiedName.contains(".")
											? fullQualifiedName.substring(0, fullQualifiedName.lastIndexOf('.')) : "";
									if (exportedPackages.contains(packageName))
										addClass(fullQualifiedName);
								}
							}

							jarFile.close();

						}
					} catch (final IOException e) {
						Logger.logError("Cannot resolve location for bundle \"" + bundle.getBundleId() + "\"", e);
					}
				}
			}
		}

		return CLASSES;
	}

	/**
	 * @param packageName
	 * @param substring
	 */
	private static void addClass(final String fullQualifiedName) {

		final String packageName = getPackage(fullQualifiedName);

		if (!CLASSES.containsKey(packageName))
			CLASSES.put(packageName, new HashSet<String>());

		CLASSES.get(packageName).add(fullQualifiedName.substring(packageName.length() + 1));
	}

	/**
	 * @param className
	 * @return
	 */
	private static String getPackage(final String className) {
		final int lastDot = className.lastIndexOf('.');
		if (lastDot == -1)
			return null;

		final String candidate = className.substring(0, lastDot);
		final Map<String, Collection<String>> packages = JavaPackagesCompletionProvider.getPackages();
		if (JavaPackagesCompletionProvider.containsPackage(candidate))
			return candidate;

		return getPackage(candidate);
	}

	/**
	 * Helper method to extract class names from public javadoc. Not needed for productive use. May be helpful when new java version gets released.
	 */
	// public static void main(final String[] args) {
	// // read java packages
	// try {
	// URL url = new URL("https://docs.oracle.com/javase/6/docs/api/allclasses-frame.html");
	// System.out.print("fetching data ... ");
	// InputStream inputStream = url.openConnection().getInputStream();
	// String htmlContent = StringTools.toString(inputStream);
	// inputStream.close();
	// System.out.println("done");
	//
	// int start = htmlContent.indexOf("<body>");
	// int end = htmlContent.indexOf("</body>");
	//
	// // preparing data
	// htmlContent = htmlContent.substring(start, end + 7);
	// htmlContent = htmlContent.replaceAll("&nbsp;", " ");
	//
	// System.out.print("parsing data ... ");
	// XMLMemento root = XMLMemento.createReadRoot(new StringReader(htmlContent));
	//
	// File file = new File("/tmp/Java 6 classes.txt");
	// if (!file.exists())
	// file.createNewFile();
	//
	// FileOutputStream outputStream = new FileOutputStream(file);
	// for (IMemento listNode : root.getChild("div").getChild("ul").getChildren("li")) {
	// String className = listNode.getChild("a").getString("href").replace('/', '.').replace(".html", "");
	// outputStream.write((className + "\n").getBytes());
	// }
	// outputStream.close();
	//
	// System.out.println("done");
	//
	// } catch (IOException e) {
	// Logger.logError("Cannot read package list for code completion", e);
	// } catch (WorkbenchException e) {
	// // TODO handle this exception (but for now, at least know it happened)
	// throw new RuntimeException(e);
	//
	// }
	// }
}