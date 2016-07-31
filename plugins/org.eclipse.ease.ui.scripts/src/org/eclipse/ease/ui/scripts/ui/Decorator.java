/*******************************************************************************
 * Copyright (c) 2016 Varun Raval and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Varun Raval - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.scripts.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.ease.sign.IPreferenceConstants;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Decorator implements ILightweightLabelDecorator, IPreferenceChangeListener {

	public static final String SIGN_DECORATOR_ID = "org.eclipse.ease.ui.scripts.decorator.signatureState";

	private final ImageDescriptor SIGNED_SCRIPT = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/ovr16/signed_script.png");
	private final ImageDescriptor INVALID_SIGNED_SCRIPT = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
			"icons/ovr16/invalid_signed_script.png");
	private final ImageDescriptor BLOCKED_SCRIPT = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/ovr16/blocked_script.png");

	public Decorator() {
		Activator.getPrefsNode().addPreferenceChangeListener(this);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
		Activator.getPrefsNode().removePreferenceChangeListener(this);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IScript) {
			final IScript iScript = (IScript) element;

			final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();

			// considering local scripts as scripts on file system
			final boolean run_without_sign_local = prefs.getBoolean(IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL);

			final boolean run_without_sign_remote = prefs.getBoolean(IPreferenceConstants.RUN_WITHOUT_SIGN_REMOTE);

			final boolean isIFile = ResourceTools.resolveFile(iScript.getLocation(), null, true) instanceof IFile;
			final boolean isRemote = iScript.isRemote();

			// check if signature is present. null signifies signature is not there.
			if (iScript.getSignatureState() != null)
				decoration.addOverlay(iScript.getSignatureState() ? SIGNED_SCRIPT
						: isRemote ? (run_without_sign_remote ? INVALID_SIGNED_SCRIPT : BLOCKED_SCRIPT)
								: (run_without_sign_local ? INVALID_SIGNED_SCRIPT : BLOCKED_SCRIPT));

			// FIXME if local or remote files does not contain signature, add appropriate decorator
			// TODO after signature state is added to script model
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL) || event.getKey().equals(IPreferenceConstants.RUN_WITHOUT_SIGN_REMOTE)) {
			// update decorators
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					PlatformUI.getWorkbench().getDecoratorManager().update(SIGN_DECORATOR_ID);
				}
			});
		}

	}
}
