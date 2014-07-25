/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.preferences;

import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.Preferences;

public class ShellPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Text fTxtHistoryLength;
	private Button fChkAutoFocusText;
	private Button fChkKeepLastCommand;
	private TabFolder fTabFolder;
	private Button fChkModulesAsFlatList;

	public ShellPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Label lblNewLabel = new Label(container, SWT.WRAP);
		lblNewLabel.setText("Defines look and feel as well as behavior of the script shells.");

		Group grpAppearance = new Group(container, SWT.NONE);
		grpAppearance.setLayout(new GridLayout(3, false));
		GridData gd_grpAppearance = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_grpAppearance.verticalIndent = 15;
		grpAppearance.setLayoutData(gd_grpAppearance);
		grpAppearance.setText("Appearance");

		Label lblSeeColorsAnd = new Label(grpAppearance, SWT.NONE);
		lblSeeColorsAnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblSeeColorsAnd.setText("See Colors and fonts to change the font settings for shell output.");

		Label lblHistoryLength = new Label(grpAppearance, SWT.NONE);
		lblHistoryLength.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHistoryLength.setText("History Length:");

		fTxtHistoryLength = new Text(grpAppearance, SWT.BORDER);

		Label lblEntries = new Label(grpAppearance, SWT.NONE);
		lblEntries.setText("entries");

		fChkModulesAsFlatList = new Button(grpAppearance, SWT.CHECK);
		fChkModulesAsFlatList.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		fChkModulesAsFlatList.setText("Show modules as flat list");

		fChkAutoFocusText = new Button(grpAppearance, SWT.CHECK);
		fChkAutoFocusText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		fChkAutoFocusText.setText("Auto focus text input field");

		fChkKeepLastCommand = new Button(grpAppearance, SWT.CHECK);
		fChkKeepLastCommand.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		fChkKeepLastCommand.setText("Keep last command in input field");

		Label lblShellStartupCommands = new Label(container, SWT.NONE);
		lblShellStartupCommands.setText("Shell startup commands");

		fTabFolder = new TabFolder(container, SWT.NONE);
		fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		Map<String, ScriptType> scriptTypes = scriptService.getAvailableScriptTypes();
		for (String type : scriptTypes.keySet()) {
			TabItem tbtmNewItem = new TabItem(fTabFolder, SWT.NONE);
			tbtmNewItem.setText(type);

			ScrolledComposite scrolledComposite = new ScrolledComposite(fTabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
			tbtmNewItem.setControl(scrolledComposite);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);

			Text input = new Text(scrolledComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			scrolledComposite.setContent(input);
		}

		performDefaults();

		return container;
	}

	@Override
	protected void performDefaults() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		int defaultLength = prefs.getInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, IPreferenceConstants.DEFAULT_SHELL_HISTORY_LENGTH);
		fTxtHistoryLength.setText(Integer.toString(defaultLength));

		boolean flatList = prefs.getBoolean(IPreferenceConstants.SHELL_MODULES_AS_LIST, IPreferenceConstants.DEFAULT_SHELL_MODULES_AS_LIST);
		fChkAutoFocusText.setSelection(flatList);

		boolean autofocus = prefs.getBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, IPreferenceConstants.DEFAULT_SHELL_AUTOFOCUS);
		fChkAutoFocusText.setSelection(autofocus);

		boolean keepCommand = prefs.getBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, IPreferenceConstants.DEFAULT_SHELL_KEEP_COMMAND);
		fChkKeepLastCommand.setSelection(keepCommand);

		for (TabItem item : fTabFolder.getItems()) {
			String title = item.getText();
			String shellCommands = prefs.get(IPreferenceConstants.SHELL_STARTUP + title, "");

			((Text) ((ScrolledComposite) item.getControl()).getContent()).setText(shellCommands);
		}

		super.performDefaults();
	}

	@Override
	public boolean performOk() {

		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		// FIXME add checks to have valid integers in the input box
		prefs.putInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, Integer.parseInt(fTxtHistoryLength.getText()));

		prefs.putBoolean(IPreferenceConstants.SHELL_MODULES_AS_LIST, fChkModulesAsFlatList.getSelection());
		prefs.putBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, fChkAutoFocusText.getSelection());
		prefs.putBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, fChkKeepLastCommand.getSelection());

		for (TabItem item : fTabFolder.getItems()) {
			String title = item.getText();
			String commands = ((Text) ((ScrolledComposite) item.getControl()).getContent()).getText();

			prefs.put(IPreferenceConstants.SHELL_STARTUP + title, commands);
		}

		return super.performOk();
	}
}
