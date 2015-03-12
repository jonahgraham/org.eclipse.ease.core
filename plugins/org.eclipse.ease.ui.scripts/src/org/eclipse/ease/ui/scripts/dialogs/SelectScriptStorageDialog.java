/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.dialogs;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.ui.scripts.preferences.PreferencesHelper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SelectScriptStorageDialog extends Dialog {
	private Text txtWorkspace;
	private Text txtFileSystem;
	private Button btnStoreInSettings;
	private Button btnStoreInWorkspace;
	private Button btnStoreOnFileSystem;
	private String fLocation = null;

	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public SelectScriptStorageDialog(final Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label lblWhereDoYou = new Label(container, SWT.NONE);
		lblWhereDoYou.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblWhereDoYou.setText("where do you want to store your recorded scripts to?");

		btnStoreInSettings = new Button(container, SWT.RADIO);
		btnStoreInSettings.setSelection(true);
		btnStoreInSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		GridData gd_btnStoreInSettings = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnStoreInSettings.verticalIndent = 20;
		btnStoreInSettings.setLayoutData(gd_btnStoreInSettings);
		btnStoreInSettings.setText("Store in my workspace settings");
		new Label(container, SWT.NONE);

		btnStoreInWorkspace = new Button(container, SWT.RADIO);
		btnStoreInWorkspace.setText("Use a workspace project");
		new Label(container, SWT.NONE);

		txtWorkspace = new Text(container, SWT.BORDER);
		GridData gd_txtWorkspace = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtWorkspace.horizontalIndent = 17;
		txtWorkspace.setLayoutData(gd_txtWorkspace);

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.setText("Browse...");
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin
						.getWorkspace().getRoot(), true, "Select script storage folder");
				if (dialog.open() == Window.OK) {
					Object[] result = dialog.getResult();
					if ((result.length > 0) && (result[0] instanceof IPath))
						txtWorkspace.setText(result[0].toString());
				}
			}
		});

		btnStoreOnFileSystem = new Button(container, SWT.RADIO);
		btnStoreOnFileSystem.setText("On the file system");
		new Label(container, SWT.NONE);

		txtFileSystem = new Text(container, SWT.BORDER);
		GridData gd_txtFileSystem = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtFileSystem.horizontalIndent = 17;
		txtFileSystem.setLayoutData(gd_txtFileSystem);

		Button btnNewButton_1 = new Button(container, SWT.NONE);
		btnNewButton_1.setText("Browse...");
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String path = dialog.open();
				if (path != null)
					txtFileSystem.setText(new File(path).toURI().toString());
			}
		});
		return container;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		if (btnStoreInSettings.getSelection())
			fLocation = PreferencesHelper.getDefaultScriptStorageLocation();

		else if (btnStoreInWorkspace.getSelection()) {
			if (txtWorkspace.getText().startsWith("/"))
				fLocation = "workspace:/" + txtWorkspace.getText();
			else
				fLocation = "workspace://" + txtWorkspace.getText();
		}

		else if (btnStoreOnFileSystem.getSelection())
			// FIXME change user input to URI syntax
			fLocation = txtFileSystem.getText();

		super.okPressed();
	}

	public String getLocation() {
		return fLocation;
	}
}
