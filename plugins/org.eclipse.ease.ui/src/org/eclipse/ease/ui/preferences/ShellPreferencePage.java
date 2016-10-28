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
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	private ComboViewer comboViewer;

	public ShellPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));

		final Label lblNewLabel = new Label(container, SWT.WRAP);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel.setText("Defines look and feel as well as behavior of the script shells.");

		final Group grpAppearance = new Group(container, SWT.NONE);
		grpAppearance.setLayout(new GridLayout(3, false));
		final GridData gd_grpAppearance = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_grpAppearance.verticalIndent = 15;
		grpAppearance.setLayoutData(gd_grpAppearance);
		grpAppearance.setText("Appearance");

		final Label lblSeeColorsAnd = new Label(grpAppearance, SWT.NONE);
		lblSeeColorsAnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblSeeColorsAnd.setText("See Colors and fonts to change the font settings for shell output.");

		final Label lblHistoryLength = new Label(grpAppearance, SWT.NONE);
		lblHistoryLength.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHistoryLength.setText("History Length:");

		fTxtHistoryLength = new Text(grpAppearance, SWT.BORDER);

		final Label lblEntries = new Label(grpAppearance, SWT.NONE);
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

		final Label lblPreferredEngine = new Label(container, SWT.NONE);
		lblPreferredEngine.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPreferredEngine.setText("Preferred Engine:");

		comboViewer = new ComboViewer(container, SWT.NONE);
		final Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((EngineDescription) e1).getName().compareTo(((EngineDescription) e2).getName());
			}
		});

		final Label lblShellStartupCommands = new Label(container, SWT.NONE);
		lblShellStartupCommands.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblShellStartupCommands.setText("Shell startup commands");

		fTabFolder = new TabFolder(container, SWT.NONE);
		fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final Map<String, ScriptType> scriptTypes = scriptService.getAvailableScriptTypes();
		for (final String type : scriptTypes.keySet()) {
			final TabItem tbtmNewItem = new TabItem(fTabFolder, SWT.NONE);
			tbtmNewItem.setText(type);

			final ScrolledComposite scrolledComposite = new ScrolledComposite(fTabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
			tbtmNewItem.setControl(scrolledComposite);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);

			final Text input = new Text(scrolledComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			scrolledComposite.setContent(input);
		}

		performDefaults();

		return container;
	}

	@Override
	protected void performDefaults() {
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		final int defaultLength = prefs.getInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, IPreferenceConstants.DEFAULT_SHELL_HISTORY_LENGTH);
		fTxtHistoryLength.setText(Integer.toString(defaultLength));

		final boolean flatList = prefs.getBoolean(IPreferenceConstants.SHELL_MODULES_AS_LIST, IPreferenceConstants.DEFAULT_SHELL_MODULES_AS_LIST);
		fChkAutoFocusText.setSelection(flatList);

		final boolean autofocus = prefs.getBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, IPreferenceConstants.DEFAULT_SHELL_AUTOFOCUS);
		fChkAutoFocusText.setSelection(autofocus);

		final boolean keepCommand = prefs.getBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, IPreferenceConstants.DEFAULT_SHELL_KEEP_COMMAND);
		fChkKeepLastCommand.setSelection(keepCommand);

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		comboViewer.setInput(scriptService.getEngines());
		final String engineID = prefs.get(IPreferenceConstants.SHELL_DEFAULT_ENGINE, IPreferenceConstants.DEFAULT_SHELL_DEFAULT_ENGINE);
		final EngineDescription defaultEngine = scriptService.getEngineByID(engineID);
		if (defaultEngine != null)
			comboViewer.setSelection(new StructuredSelection(scriptService.getEngineByID(engineID)), true);

		for (final TabItem item : fTabFolder.getItems()) {
			final String title = item.getText();
			final String shellCommands = prefs.get(IPreferenceConstants.SHELL_STARTUP + title, "");

			((Text) ((ScrolledComposite) item.getControl()).getContent()).setText(shellCommands);
		}

		super.performDefaults();
	}

	@Override
	public boolean performOk() {

		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		// FIXME add checks to have valid integers in the input box
		prefs.putInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, Integer.parseInt(fTxtHistoryLength.getText()));

		prefs.putBoolean(IPreferenceConstants.SHELL_MODULES_AS_LIST, fChkModulesAsFlatList.getSelection());
		prefs.putBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, fChkAutoFocusText.getSelection());
		prefs.putBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, fChkKeepLastCommand.getSelection());

		final String engineId = ((EngineDescription) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement()).getID();
		prefs.put(IPreferenceConstants.SHELL_DEFAULT_ENGINE, engineId);

		for (final TabItem item : fTabFolder.getItems()) {
			final String title = item.getText();
			final String commands = ((Text) ((ScrolledComposite) item.getControl()).getContent()).getText();

			prefs.put(IPreferenceConstants.SHELL_STARTUP + title, commands);
		}

		return super.performOk();
	}
}
