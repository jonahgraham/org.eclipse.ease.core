/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.Preferences;

public class ScriptingPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button btnAllowUIAccess;

	public ScriptingPage() {
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Group grpSecurisecurty = new Group(container, SWT.NONE);
		grpSecurisecurty.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpSecurisecurty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSecurisecurty.setText("Security");

		btnAllowUIAccess = new Button(grpSecurisecurty, SWT.CHECK);
		btnAllowUIAccess.setText("Allow scripts to run code in UI thread");

		performDefaults();

		return container;
	}

	@Override
	protected void performDefaults() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);

		boolean allowUIAccess = prefs.getBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, Activator.DEFAULT_SCRIPTS_ALLOW_UI_ACCESS);
		btnAllowUIAccess.setSelection(allowUIAccess);

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);

		prefs.putBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, btnAllowUIAccess.getSelection());

		return super.performOk();
	}
}
