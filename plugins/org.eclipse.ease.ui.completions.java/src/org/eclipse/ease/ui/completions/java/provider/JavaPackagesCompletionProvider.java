/*******************************************************************************
 * Copyright (c) 2015, 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.completions.java.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.IHelpResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completions.java.EaseUICompletionsJavaFragment;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaPackageHelpResolver;
import org.eclipse.jdt.ui.ISharedImages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class JavaPackagesCompletionProvider extends AbstractCompletionProvider {

	private static Map<String, Collection<String>> PACKAGES = null;

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && ((context.getType() == Type.NONE) || (context.getType() == Type.PACKAGE));
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {

		final String parentPackage = (context.getType() == Type.NONE) ? "" : context.getPackage();

		if (getPackages().get(parentPackage) != null) {
			for (final String packageName : getPackages().get(parentPackage)) {

				if (matchesFilter(packageName)) {
					final IHelpResolver helpResolver = new JavaPackageHelpResolver(parentPackage + "." + packageName);

					if (parentPackage.isEmpty())
						// add root package
						addProposal(packageName, packageName + ".", JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_PACKAGE),
								ScriptCompletionProposal.ORDER_PACKAGE, helpResolver);
					else
						// add sub package
						addProposal(parentPackage + "." + packageName, packageName + ".",
								JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_PACKAGE), ScriptCompletionProposal.ORDER_PACKAGE,
								helpResolver);
				}
			}
		}
	}

	public static Map<String, Collection<String>> getPackages() {
		if (PACKAGES == null) {
			PACKAGES = new HashMap<>();

			// read java packages
			try {
				final URL url = new URL(
						"platform:/plugin/org.eclipse.ease.ui/resources/java" + System.getProperty("java.runtime.version").charAt(2) + " packages.txt");
				final InputStream inputStream = url.openConnection().getInputStream();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String packageName;
				while ((packageName = reader.readLine()) != null)
					registerPackage(packageName);

				reader.close();

			} catch (final IOException e) {
				Logger.error(EaseUICompletionsJavaFragment.FRAGMENT_ID, "Cannot read package list for code completion", e);
			}

			// read eclipse packages
			final BundleContext context = FrameworkUtil.getBundle(JavaPackagesCompletionProvider.class).getBundleContext();
			for (final Bundle bundle : context.getBundles()) {

				for (final String packageName : getExportedPackages(bundle))
					registerPackage(packageName);
			}
		}

		return PACKAGES;
	}

	/**
	 * Get a list of all exported packages of a bundle.
	 *
	 * @param bundle
	 *            bundle instance
	 * @return collection of exported packages
	 */
	public static Collection<String> getExportedPackages(final Bundle bundle) {
		final Collection<String> exportedPackages = new HashSet<>();

		final String exportPackage = bundle.getHeaders().get("Export-Package");
		if (exportPackage != null) {
			final String[] packages = exportPackage.split(",");
			for (final String packageEntry : packages) {
				String candidate = packageEntry.trim().split(";")[0];
				if (candidate.endsWith("\""))
					candidate = candidate.substring(0, candidate.length() - 1);

				if ((candidate.contains(".internal")) || (packageEntry.contains(";x-internal:=true")))
					// ignore internal packages
					continue;

				if ((candidate.startsWith("Lib")) || (candidate.startsWith("about_files")) || (candidate.startsWith("META")))
					// ignore some dedicated packages
					continue;

				exportedPackages.add(candidate);
			}
		}

		return exportedPackages;
	}

	private static void registerPackage(final String packageName) {
		final int lastIndex = packageName.lastIndexOf('.');
		final String key = (lastIndex == -1) ? "" : packageName.substring(0, lastIndex);
		final String value = (lastIndex == -1) ? packageName : packageName.substring(lastIndex + 1);

		if (!PACKAGES.containsKey(key))
			PACKAGES.put(key, new HashSet<String>());

		PACKAGES.get(key).add(value);

		if (!key.isEmpty())
			registerPackage(key);
	}

	/**
	 * Try to find a package in the registered package list
	 *
	 * @param candidate
	 *            package name to look up
	 * @return <code>true</code> when package is registered
	 */
	public static boolean containsPackage(final String candidate) {
		final int lastDot = candidate.lastIndexOf('.');
		final Collection<String> packageList = (lastDot > 0) ? getPackages().get(candidate.substring(0, lastDot)) : getPackages().get("");

		if (packageList != null)
			return packageList.contains(candidate.substring(lastDot + 1));

		return false;
	}
}
