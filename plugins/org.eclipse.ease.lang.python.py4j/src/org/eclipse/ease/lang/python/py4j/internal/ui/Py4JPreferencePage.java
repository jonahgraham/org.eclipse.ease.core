/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j.internal.ui;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.ease.lang.python.py4j.internal.Activator;
import org.eclipse.ease.lang.python.py4j.internal.Py4JScriptEnginePrefConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Py4JPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Py4JPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(Py4JScriptEnginePrefConstants.INTERPRETER, "Python location:", false, FileFieldEditor.VALIDATE_ON_KEY_STROKE,
				getFieldEditorParent()) {
			private Button variablesButton;

			{
				setErrorMessage("Python location must be absolute path to python, or name of executable to launch from System PATH");
			}

			@Override
			public int getNumberOfControls() {
				return 4;
			}

			protected void variablesPressed() {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getTextControl().getShell());
				if (dialog.open() == Window.OK) {
					getTextControl().insert(dialog.getVariableExpression());
	                valueChanged();
				}
			}

			/**
			 * Get the change control. Create it in parent if required.
			 *
			 * @param parent
			 * @return Button
			 */
			protected Button getVariablesControl(Composite parent) {
				if (variablesButton == null) {
					variablesButton = new Button(parent, SWT.PUSH);
					variablesButton.setText("Variables...");
					variablesButton.setFont(parent.getFont());
					variablesButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent evt) {
							variablesPressed();
						}
					});
					variablesButton.addDisposeListener(event -> variablesButton = null);
				} else {
					checkParent(variablesButton, parent);
				}
				return variablesButton;
			}

			@Override
			protected void doFillIntoGrid(Composite parent, int numColumns) {
				super.doFillIntoGrid(parent, numColumns - 1);
				variablesButton = getVariablesControl(parent);
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				int widthHint = convertHorizontalDLUsToPixels(variablesButton, IDialogConstants.BUTTON_WIDTH);
				gd.widthHint = Math.max(widthHint, variablesButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
				variablesButton.setLayoutData(gd);
			}

			@Override
			protected boolean checkState() {

				String msg = null;
				String info = null;

				String path = getTextControl().getText();
				if (path != null) {
					path = path.trim();
				} else {
					path = "";//$NON-NLS-1$
				}

				IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
				try {
					path = variableManager.performStringSubstitution(path);
				} catch (CoreException e) {
					msg = e.getLocalizedMessage();
				}

				if (msg == null) {
					if (path.isEmpty()) {
						msg = getErrorMessage();
					} else {
						File file = new File(path);
						if (file.isFile() && file.isAbsolute()) {
							// all good
						} else if (file.isDirectory() || path.contains("/") || path.contains("\\")) {
							msg = getErrorMessage();
						} else {
							info = "Python will be launched from System PATH unless an absolute location for Python is provided.";
						}
					}
				}

				if (msg != null) { // error
					showErrorMessage(msg);
					return false;
				}

				if (doCheckState()) { // OK!
					clearErrorMessage();
					getPage().setMessage(null);
					if (info != null) {
						getPage().setMessage(info, IMessageProvider.INFORMATION);
					}
					return true;
				}
				msg = getErrorMessage(); // subclass might have changed it in the #doCheckState()
				if (msg != null) {
					showErrorMessage(msg);
				}
				return false;
			}
		});

	}

}
