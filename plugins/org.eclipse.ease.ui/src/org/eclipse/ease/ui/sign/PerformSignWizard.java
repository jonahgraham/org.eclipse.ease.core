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

package org.eclipse.ease.ui.sign;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.UnrecoverableKeyException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.sign.PerformSignature;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;

public class PerformSignWizard extends Wizard {

	protected PerformBasicSignPage fPerformBasicSignPage;
	protected PerformAdvancedSignPage fPerformAdvancedSignPage;
	final protected GetInfo fGetInfo;
	final IFile fFile;
	final ScriptType fScriptType;

	/**
	 * All the information related to signature and contents of file excluding signature will be received to the caller using instance of {@link GetInfo}.
	 *
	 * @param iFile
	 *            provide file to find signature of
	 * @param scriptType
	 *            provide {@link ScriptType} of corresponding file
	 * @param getInfo
	 *            instance of {@link GetInfo} to receive signature info and information regarding file
	 */
	public PerformSignWizard(IFile iFile, ScriptType scriptType, GetInfo getInfo) {
		super();
		setNeedsProgressMonitor(true);

		// fGetInfo will be used by paged to store signature related information
		fGetInfo = getInfo;
		fFile = iFile;
		fScriptType = scriptType;
	}

	@Override
	public String getWindowTitle() {
		return "Perform Signature";
	}

	@Override
	public void addPages() {
		super.addPages();

		fPerformBasicSignPage = new PerformBasicSignPage(fGetInfo);
		fPerformAdvancedSignPage = new PerformAdvancedSignPage();

		addPage(fPerformBasicSignPage);
		addPage(fPerformAdvancedSignPage);
	}

	@Override
	public boolean canFinish() {
		return fPerformBasicSignPage.canComplete();
	}

	@Override
	public boolean performFinish() {
		try {

			fGetInfo.setCertificates(PerformSignature.getCertificate(fGetInfo.getKeyStore(), fGetInfo.getAlias(), true));

			String signature = null;
			final ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
			final ISecurePreferences node = preferences.node(GetInfo.KEYTORE_ALIAS_NODE);
			String pass = null;
			final String aliasNodeKey = fGetInfo.getKeyStoreFile() + "/" + fGetInfo.getAlias();
			while (true) {
				try {
					pass = node.get(aliasNodeKey, null);
					if (pass == null) {
						final PasswordDialog passwordDialog = new PasswordDialog(getShell(), aliasNodeKey, "Alias Password");
						if (passwordDialog.open() == Window.CANCEL) {
							return false;
						}

						pass = passwordDialog.getPassword();
					}

					// get input stream depending on whether file already contains signature
					InputStream inputStream;
					final SignatureInfo signatureInfo = fScriptType.getCodeParser().getSignatureInfo(fFile.getContents());
					inputStream = signatureInfo == null ? ResourceTools.getInputStream(fFile)
							: new ByteArrayInputStream(signatureInfo.getContentOnly().getBytes());

					signature = PerformSignature.createSignature(fGetInfo.getKeyStore(), inputStream, fGetInfo.getAlias(), pass,
							fPerformAdvancedSignPage.getAliasProvider(), fPerformAdvancedSignPage.getMessageDigestAlgo());

					// tell handler signature is present or not
					if (signatureInfo != null) {
						fGetInfo.setSignaturePresence(true);
						fGetInfo.setContentOnly(signatureInfo.getContentOnly());
					}

					break;

				} catch (final UnrecoverableKeyException e) {
					pass = null;
					node.remove(aliasNodeKey);
					fPerformBasicSignPage.setErrorMessage("Invalid Private Key Password");
					fPerformAdvancedSignPage.setErrorMessage("Invalid Private Key Password");

				} catch (final Exception e) {
					// StorageException CoreException
					Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);

				}
			}

			if (signature == null) {
				fPerformBasicSignPage.reset();
				fPerformBasicSignPage.setErrorMessage("Unable to access keystore. Try again");
				fPerformAdvancedSignPage.setErrorMessage("Unable to access keystore. Try again");
				return false;
			}

			FileWriter fw = null;
			try {
				final IPath iPath = Activator.getDefault().getStateLocation();
				fw = new FileWriter(iPath.append(GetInfo.KEYSTORE_SETTING_FILE).toString());
				for (final String file : fGetInfo.getKeyStoreFiles())
					fw.write(file + "\n");

			} catch (final IOException ex) {
				Logger.error(Activator.PLUGIN_ID, ex.getMessage(), ex);

			} finally {
				try {
					if (fw != null)
						fw.close();

				} catch (final IOException ex) {
					Logger.error(Activator.PLUGIN_ID, ex.getMessage(), ex);

				}
			}

			fGetInfo.setSignMessageDigestAlgo(fPerformAdvancedSignPage.getMessageDigestAlgo());
			final String temp[] = signature.split(":");
			fGetInfo.setSignature(temp[1]);
			fGetInfo.setSignProvider(temp[0]);

			return true;

		} catch (final ScriptSignatureException e) {
			fPerformBasicSignPage.setErrorMessage(e.getMessage());
			fPerformAdvancedSignPage.setErrorMessage(e.getMessage());
			Logger.error(Activator.PLUGIN_ID, e.getMessage(), e.getCause() != null ? e.getCause() : e);

		}

		return false;
	}
}
