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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PerformAdvancedSignPage extends WizardPage {

	private Combo fAliasProviderCombo, fDigestCombo;
	private String fAliasProvider = null, fDigestAlgo = null;

	/**
	 * @param pageName
	 */
	protected PerformAdvancedSignPage() {
		super("Advanced Settings");
		setTitle("Enter Keystore Info");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		final Composite container = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;

		GridData gridData;

		Label aliasProviderLabel = new Label(container, SWT.NONE);
		aliasProviderLabel.setText("Provider to perform Signature:");
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		aliasProviderLabel.setLayoutData(gridData);

		fAliasProviderCombo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		fAliasProviderCombo.add("Preferred");
		for (String provider : GetInfo.getProvider()) {
			fAliasProviderCombo.add(provider);
		}
		fAliasProviderCombo.setLayoutData(gridData);
		fAliasProviderCombo.select(0);

		Label digestLabel = new Label(container, SWT.NONE);
		digestLabel.setText("Provide Message Digest Algorithm to use:");
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		digestLabel.setLayoutData(gridData);

		fDigestCombo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		for (String algo : GetInfo.getMessageDigestAlgo())
			fDigestCombo.add(algo);

		fDigestCombo.setLayoutData(gridData);
		fDigestCombo.select(0);

		fAliasProviderCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fAliasProvider = fAliasProviderCombo.getText();
			}
		});

		fDigestCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fDigestAlgo = fDigestCombo.getText();
			}
		});

		container.setSize(container.computeSize(600, SWT.DEFAULT));
		setControl(scrolledComposite);
	}

	public String getMessageDigestAlgo() {
		if (fDigestCombo.isDisposed())
			return null;

		return fDigestAlgo;
	}

	public String getAliasProvider() {
		if (fAliasProviderCombo.isDisposed())
			return null;

		return fAliasProvider;
	}

}
