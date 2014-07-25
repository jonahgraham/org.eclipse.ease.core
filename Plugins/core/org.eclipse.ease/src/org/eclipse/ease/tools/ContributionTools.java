/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     original code template from org.eclipse.ui.internal.menus.MemuHelper
 *     Christian Pontesegger - adaptions to get ImageDescriptors and to remove some complexity
 *******************************************************************************/
package org.eclipse.ease.tools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public final class ContributionTools {

	/**
	 * Static utility class. Instances should not be created.
	 */
	@Deprecated
	private ContributionTools() {
	}

	public static ImageDescriptor getImageDescriptor(final IConfigurationElement element, final String attr) {
		String iconPath = element.getAttribute(attr);
		if (iconPath == null) {
			return null;
		}

		// If iconPath doesn't specify a scheme, then try to transform to a URL
		// RFC 3986: scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
		// This allows using data:, http:, or other custom URL schemes
		if (!iconPath.matches("\\p{Alpha}[\\p{Alnum}+.-]*:.*")) { //$NON-NLS-1$
			// First attempt to resolve in ISharedImages (e.g. "IMG_OBJ_FOLDER")
			// as per bug 391232 & AbstractUIPlugin.imageDescriptorFromPlugin().
			ImageDescriptor d = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(iconPath);
			if (d != null)
				return d;

			String extendingPluginId = element.getDeclaringExtension().getContributor().getName();
			iconPath = "platform:/plugin/" + extendingPluginId + "/" + iconPath; //$NON-NLS-1$//$NON-NLS-2$
		}
		URL url = null;
		try {
			url = FileLocator.find(new URL(iconPath));
			if (url != null) {
				url = rewriteDurableURL(url);

				return ImageDescriptor.createFromURL(url);
			}
		} catch (MalformedURLException e) {
			/* IGNORE */
		}

		return null;
	}

	/**
	 * Rewrite certain types of URLs to more durable forms, as these URLs may may be persisted in the model.
	 * 
	 * @param url
	 *            the url
	 * @return the rewritten URL
	 */
	private static URL rewriteDurableURL(final URL url) {
		// Rewrite bundleentry and bundleresource entries as they are
		// invalidated on -clean or a bundle remove, . These Platform URIs are
		// of the form:
		// bundleentry://<bundle-id>.XXX/path/to/file
		// bundleresource://<bundle-id>.XXX/path/to/file
		if (!url.getProtocol().equals("bundleentry") && !url.getProtocol().equals("bundleresource")) { //$NON-NLS-1$ //$NON-NLS-2$
			return url;
		}

		BundleContext ctxt = FrameworkUtil.getBundle(IViewRegistry.class).getBundleContext();
		try {
			URI uri = url.toURI();
			String host = uri.getHost();
			String bundleId = host.substring(0, host.indexOf('.'));
			Bundle bundle = ctxt.getBundle(Long.parseLong(bundleId));
			StringBuilder builder = new StringBuilder("platform:/plugin/"); //$NON-NLS-1$
			builder.append(bundle.getSymbolicName());
			builder.append(uri.getPath());
			return new URL(builder.toString());

		} catch (URISyntaxException e) {
			/* IGNORE */
		} catch (MalformedURLException e) {
			/* IGNORE */
		}

		return url;
	}
}
