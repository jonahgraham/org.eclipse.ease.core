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

import org.eclipse.ease.lang.python.py4j.internal.Activator;
import org.eclipse.ease.lang.python.py4j.internal.Py4JScriptEnginePrefConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
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
			{
				setErrorMessage("Python location must be absolute path to python, or name of executable to launch from System PATH");
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
				if (path.length() == 0) {
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
