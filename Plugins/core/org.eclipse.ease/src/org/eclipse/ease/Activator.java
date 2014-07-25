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

import org.eclipse.ecf.filetransfer.FileTransferInfo;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.ease";

	public static String PREFERENCES_NODE_SCRIPTS = "scripts";

	public static String SCRIPTS_ALLOW_UI_ACCESS = "scriptUIAccess";
	public static boolean DEFAULT_SCRIPTS_ALLOW_UI_ACCESS = false;

	private static Activator fInstance;

	public static Activator getDefault() {
		return fInstance;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		// we need to force loading of the org.eclipse.ecf.filetransfer plugin to correctly register extended URL protocols.
		// therefore load a class from that plugin
		Class<FileTransferInfo> foo = FileTransferInfo.class;

		fInstance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		fInstance = null;

		super.stop(context);
	}
}
