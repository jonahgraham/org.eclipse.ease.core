/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.filetransfer.FileTransferJob;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.ease";

	public static final String PREFERENCES_NODE_SCRIPTS = "scripts";

	public static final String SCRIPTS_ALLOW_UI_ACCESS = "scriptUIAccess";
	public static final boolean DEFAULT_SCRIPTS_ALLOW_UI_ACCESS = false;

	private static Activator fInstance;

	public static Activator getDefault() {
		return fInstance;
	}

	private BundleContext fContext;

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		// we need to force loading of the org.eclipse.ecf.filetransfer plugin to correctly register extended URL protocols.
		// therefore load a class from that plugin
		new FileTransferJob("dummy").setFileTransfer(null);

		fContext = context;
		fInstance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		fInstance = null;
		fContext = null;

		super.stop(context);
	}

	public BundleContext getContext() {
		return fContext;
	}

	/**
	 * Get {@link ImageDescriptor} for a given bundle/path location.
	 *
	 * @param bundleID
	 * @param path
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(final String bundleID, final String path) {
		assert (bundleID != null) : "No bundle defined";
		assert (path != null) : "No path defined";

		// if the bundle is not ready then there is no image
		final Bundle bundle = Platform.getBundle(bundleID);
		final int bundleState = bundle.getState();
		if ((bundleState != Bundle.ACTIVE) && (bundleState != Bundle.STARTING) && (bundleState != Bundle.RESOLVED))
			return null;

		// look for the image (this will check both the plugin and fragment folders)
		final URL imagePath = FileLocator.find(bundle, new Path(path), null);

		if (imagePath != null)
			return ImageDescriptor.createFromURL(imagePath);

		return null;
	}
}
