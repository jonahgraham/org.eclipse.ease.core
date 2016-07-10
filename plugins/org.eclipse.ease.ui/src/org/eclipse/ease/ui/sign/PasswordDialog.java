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

import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PasswordDialog extends Dialog {

	protected Text fPasswordText;
	protected String fNodeName, fPassword, fTitle;
	protected Button fSavePassCheckButton;

	protected PasswordDialog(Shell parentShell, String nodeName, String title) {
		super(parentShell);

		fTitle = title;
		fNodeName = nodeName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;

		GridData gridData;

		Label aliasLabel = new Label(container, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		aliasLabel.setText("Enter Password");
		aliasLabel.setLayoutData(gridData);

		fPasswordText = new Text(container, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		fPasswordText.setLayoutData(gridData);

		Group group = new Group(container, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		fSavePassCheckButton = new Button(group, SWT.CHECK);

		Label checkBoxLabel = new Label(group, SWT.NONE);
		checkBoxLabel.setText("Save Password");

		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		group.setLayoutData(gridData);

		initialize();

		return container;
	}

	protected void initialize() {

		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences node = preferences.node(GetInfo.KEYTORE_ALIAS_NODE);

		try {
			fPasswordText.setText(node.get(fNodeName, ""));

		} catch (StorageException e) {
			Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fTitle);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fPassword = fPasswordText.getText();
		if (fSavePassCheckButton.getSelection()) {
			ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = preferences.node(GetInfo.KEYTORE_ALIAS_NODE);
			try {
				node.put(fNodeName, fPasswordText.getText(), true);

			} catch (StorageException e) {
				Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
			}
		}
		super.okPressed();
	}

	String getPassword() {
		return fPassword;
	}
}