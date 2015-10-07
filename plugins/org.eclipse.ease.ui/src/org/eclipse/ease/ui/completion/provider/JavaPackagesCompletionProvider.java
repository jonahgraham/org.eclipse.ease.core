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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.help.hovers.IHelpResolver;
import org.eclipse.ease.ui.help.hovers.JavaPackageHelpResolver;
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
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		String parentPackage = (context.getType() == Type.NONE) ? "" : context.getPackage();

		if (getPackages().get(parentPackage) != null) {
			for (String packageName : getPackages().get(parentPackage)) {

				final IHelpResolver helpResolver = new JavaPackageHelpResolver(parentPackage + "." + packageName);

				if (parentPackage.isEmpty())
					// add root package
					addProposal(proposals, context, packageName, packageName + ".", JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_PACKAGE),
							ScriptCompletionProposal.ORDER_PACKAGE, helpResolver);
				else
					// add sub package
					addProposal(proposals, context, parentPackage + "." + packageName, packageName + ".",
							JavaMethodCompletionProvider.getSharedImage(ISharedImages.IMG_OBJS_PACKAGE), ScriptCompletionProposal.ORDER_PACKAGE, helpResolver);
			}
		}

		return proposals;
	}

	public static Map<String, Collection<String>> getPackages() {
		if (PACKAGES == null) {
			PACKAGES = new HashMap<String, Collection<String>>();

			// read java packages
			try {
				URL url = new URL(
						"platform:/plugin/org.eclipse.ease.ui/resources/java" + System.getProperty("java.runtime.version").charAt(2) + " packages.txt");
				InputStream inputStream = url.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String packageName;
				while ((packageName = reader.readLine()) != null)
					registerPackage(packageName);

				reader.close();

			} catch (IOException e) {
				Logger.error(Activator.PLUGIN_ID, "Cannot read package list for code completion", e);
			}

			// read eclipse packages
			BundleContext context = FrameworkUtil.getBundle(JavaPackagesCompletionProvider.class).getBundleContext();
			for (Bundle bundle : context.getBundles()) {
				for (String packageName : getExportedPackages(bundle))
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
		Collection<String> exportedPackages = new HashSet<String>();

		String exportPackage = bundle.getHeaders().get("Export-Package");
		if (exportPackage != null) {
			String[] packages = exportPackage.split(",");
			for (String packageEntry : packages) {
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
		int lastIndex = packageName.lastIndexOf('.');
		String key = (lastIndex == -1) ? "" : packageName.substring(0, lastIndex);
		String value = (lastIndex == -1) ? packageName : packageName.substring(lastIndex + 1);

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
		if (getPackages().containsKey(candidate))
			return true;

		int lastDot = candidate.lastIndexOf('.');
		if (lastDot > 0) {
			Collection<String> packageList = getPackages().get(candidate.substring(0, lastDot));
			if (packageList != null)
				return packageList.contains(candidate.substring(lastDot + 1));
		}

		return false;
	}
}