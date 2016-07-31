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

package org.eclipse.ease.sign;

import java.io.InputStream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class SignatureCheck {

	public static final String PLUGIN_EASE_UI_SCRIPTS_ID = "org.eclipse.ease.ui.scripts";

	/**
	 * Use result of this method directly to know whether to run remote/local scripts.
	 *
	 * @param scriptType
	 *            instance of {@link ScriptType}
	 * @param inputStream
	 *            InputStream of data file which contain appended signature
	 * @param remote
	 *            <code>true</code> if file to be executed is a remote file or <code>false</code> if file is on local system
	 * @return <code>true</code> if script file can be executed or <code>false</code> otherwise
	 */
	public static boolean canExecute(ScriptType scriptType, InputStream inputStream, boolean remote) {
		try {
			final VerifySignature verifySignature = VerifySignature.getInstance(scriptType, inputStream);

			final IPreferencesService prefs = Platform.getPreferencesService();

			if (remote) {
				final boolean executeRemote = prefs.getBoolean(PLUGIN_EASE_UI_SCRIPTS_ID, IPreferenceConstants.RUN_WITHOUT_SIGN_REMOTE, false, null);

				if (executeRemote) {
					// can execute remote scripts without signature

					if (verifySignature == null)
						// signature is not present
						return true;
					else {
						// signature is present
						return checkSignature(verifySignature);
					}
				} else {
					// cannot execute remote scripts without signature
					return checkSignature(verifySignature);
				}

			} else {
				final boolean executeLocal = prefs.getBoolean(PLUGIN_EASE_UI_SCRIPTS_ID, IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL, false, null);

				if (executeLocal) {
					// can execute local scripts without signature

					if (verifySignature == null)
						// signature is not present
						return true;
					else {
						// signature is present
						return checkSignature(verifySignature);
					}
				} else {
					// cannot execute local scripts without signature
					return checkSignature(verifySignature);
				}
			}

		} catch (final ScriptSignatureException e) {
			return showErrorMessage(e.getMessage());
		}
	}

	/**
	 * Method using various methods of {@link VerifySignature} for validation.
	 *
	 * @param verifySignature
	 *            instance of {@link VerifySignature} to perform various checks like signature check, certifiacte check, etc.
	 * @return <code>true</code> if signature and/or certificates are proper or user tells so explicitly or <code>false</code> if signature and/or certificates
	 *         are improper
	 */
	private static boolean checkSignature(VerifySignature verifySignature) {

		try {
			if (verifySignature.verify()) {
				if (!verifySignature.isSelfSignedCertificate()) {
					if (!verifySignature.isCertChainValid()) {
						return showErrorMessage("Certificate chain is invalid. Either not signed by trusted certificate or there are incorrect certificates.");
					} else
						return true;
				} else {
					return showErrorMessage("Signature is valid. Attached certificate is Self-Signed. Do you want to execute?");
				}
			} else {
				return showErrorMessage("File modified after applying signature");
			}

		} catch (final ScriptSignatureException e) {
			return showErrorMessage("Signature can't be verified. " + e.getMessage());

		}
	}

	/**
	 * Method to show error messages.
	 *
	 * @param msg
	 *            message to show to user
	 * @return <code>true</code> if can run script i.e. user click on "Execute button" or <code>false</code> if user clicks on "Don't Execute button" or
	 *         disposes message dialog
	 */
	private static boolean showErrorMessage(String msg) {
		final MessageDialog messageDialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error checking signature", null,
				msg, MessageDialog.QUESTION, new String[] { "Don't Execute", "Execute" }, 0);

		switch (messageDialog.open()) {
		case 0:
			// Don't execute
			return false;
		case 1:
			// Execute
			return true;
		default:
			// No answer - Don't execute
			return false;
		}
	}
}
