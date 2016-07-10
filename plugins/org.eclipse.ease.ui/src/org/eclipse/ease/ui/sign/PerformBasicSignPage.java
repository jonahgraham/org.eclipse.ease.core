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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.Logger;
import org.eclipse.ease.sign.PerformSignature;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class PerformBasicSignPage extends WizardPage {

	private final GetInfo fGetInfo;
	private Combo fKeyStoreFileCombo;
	private List fAliasList;
	private boolean fIsComplete = false;
	protected ArrayList<String> fKeyStoreFileList;

	/**
	 * @param getInfo
	 *            provide instance of {@link GetInfo}
	 */
	protected PerformBasicSignPage(GetInfo getInfo) {
		super("Enter Properties");
		setTitle("Enter Properties");
		setPageComplete(false);

		fGetInfo = getInfo;

		fKeyStoreFileList = new ArrayList<>();
	}

	@Override
	public void createControl(Composite parent) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite container = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(container);

		final GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;

		GridData gridData;

		final Label keyStoreLabel = new Label(container, SWT.NONE);
		keyStoreLabel.setText("KeyStore Location");
		keyStoreLabel.setToolTipText("Enter location of keystore. Location can be URL or a file or even location on workspace.");
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		keyStoreLabel.setLayoutData(gridData);

		fKeyStoreFileCombo = new Combo(container, SWT.BORDER | SWT.SINGLE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1);
		fKeyStoreFileCombo.setLayoutData(gridData);

		final Button browseButton = new Button(container, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(gridData);

		final Label aliasLabel = new Label(container, SWT.NONE);
		aliasLabel.setText("Choose Alias");
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 4, 1);
		aliasLabel.setLayoutData(gridData);

		fAliasList = new List(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1);
		fAliasList.setLayoutData(gridData);

		initialize();

		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText("Chose Keystore");
				final String selectedFile = fd.open();
				if (selectedFile != null)
					fKeyStoreFileCombo.setText(selectedFile);

			}
		});

		fKeyStoreFileCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					final String selectedFile = fKeyStoreFileCombo.getText();
					loadKeyStore(selectedFile);

				} catch (final ScriptSignatureException ex) {
					Logger.error(Activator.PLUGIN_ID, ex.getMessage(), ex.getCause() != null ? ex.getCause() : ex);
					setErrorMessage(ex.getMessage());
					reset();
				}
			}
		});

		fAliasList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fGetInfo.setAlias(fAliasList.getItem(fAliasList.getSelectionIndex()));
				fIsComplete = true;
				setPageComplete(true);
			}
		});

		container.setSize(container.computeSize(600, SWT.DEFAULT));
		setControl(scrolledComposite);
	}

	protected void initialize() {
		final IPath iPath = Activator.getDefault().getStateLocation();

		BufferedReader bReader = null;
		try {
			fKeyStoreFileCombo.removeAll();
			bReader = new BufferedReader(new FileReader(iPath.append(GetInfo.KEYSTORE_SETTING_FILE).toString()));
			while (bReader.ready()) {
				final String line = bReader.readLine();
				fKeyStoreFileCombo.add(line);
				fKeyStoreFileList.add(line);
			}

		} catch (final FileNotFoundException e) {
			Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);

		} finally {
			if (bReader != null) {
				try {
					bReader.close();
				} catch (final IOException e) {
					Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Load keystore.
	 *
	 * @param selectedFile
	 *            provide path of file in string format
	 * @throws ScriptSignatureException
	 *             when keystore cannot be loaded
	 */
	protected void loadKeyStore(String selectedFile) throws ScriptSignatureException {
		final ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = preferences.node(GetInfo.KEYTORE_ALIAS_NODE);
		String pass = null;
		final String provider = null;
		InputStream inputStream = null;
		try {
			while (true) {
				inputStream = ResourceTools.getInputStream(selectedFile);
				if (inputStream != null) {
					pass = node.get(selectedFile, null);
					if (pass == null) {
						final PasswordDialog passwordDialog = new PasswordDialog(getShell(), selectedFile, "Keystore Password");
						if (passwordDialog.open() == Window.CANCEL) {
							reset();
							throw new ScriptSignatureException("Cannot load Keystore");
						}
						pass = passwordDialog.getPassword();
					}
					try {
						final Iterator<String> typeList = Security.getAlgorithms("keystore").iterator();
						while (typeList.hasNext()) {
							try {
								fGetInfo.setKeyStore(PerformSignature.loadKeyStore(inputStream, typeList.next(), provider, pass));
								break;

							} catch (final IOException e) {
								Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
								// last type is also not applicable
								if (!typeList.hasNext())
									throw new ScriptSignatureException("Cannot load keystore");

							}
						}

						fGetInfo.setKeyStoreFile(selectedFile);
						fGetInfo.setKeyStorePassword(pass);

						setErrorMessage(null);
						setMessage("KeyStore Loaded", SWT.OK);

						if (fKeyStoreFileList.contains(selectedFile))
							fKeyStoreFileList.remove(selectedFile);

						fKeyStoreFileList.add(0, selectedFile);

						fGetInfo.setKeyStoreFiles(fKeyStoreFileList);

						// initialize alias list
						fAliasList.removeAll();
						for (final String alias : PerformSignature.getAliases(fGetInfo.getKeyStore()))
							fAliasList.add(alias);

						fAliasList.setFocus();
						fAliasList.setSelection(0);

						return;

					} catch (final UnrecoverableKeyException e) {
						// password is incorrect. Remove key from node and try again
						pass = null;
						node.remove(selectedFile);
						setErrorMessage("Invalid Keystore Password");
					}
				} else
					throw new ScriptSignatureException("Cannot open Keystore from provided file");

			}

		} catch (final StorageException e) {
			Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
			reset();

		} finally {
			try {
				if (inputStream != null)
					inputStream.close();

			} catch (final IOException e) {
				Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);

			}
		}
	}

	/**
	 * Resets the values of page.
	 */
	protected void reset() {
		fKeyStoreFileCombo.clearSelection();
		fKeyStoreFileCombo.deselectAll();
		fAliasList.removeAll();
	}

	/**
	 * Used to know whether page is complete so that navigations buttons can reset.
	 *
	 * @return <code>true</code> if page is complete and <code>false</code> if page is not complete
	 */
	boolean canComplete() {
		return fIsComplete;
	}
}
