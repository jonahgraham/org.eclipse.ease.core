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

package org.eclipse.ease.ui.scripts.preferences;

import org.eclipse.ease.sign.IPreferenceConstants;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SignaturePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// Way to access this preference

	// from same plugin
	// final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
	// System.out.println("pref " + prefs.getString(IPreferenceConstants.DEFAULT_KEYSTORE_PATH));

	// from different plugin
	// Platform.getPreferencesService().getBoolean("org.eclipse.ease.ui.scripts", IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL, false,
	// null);

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("EASE Security");
	}

	@Override
	protected void createFieldEditors() {

		addField(new BooleanFieldEditor(IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL, "Allow executing &LOCAL scripts that are not signed",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(IPreferenceConstants.RUN_WITHOUT_SIGN_REMOTE, "Allow executing &REMOTE scripts that are not signed(Not Recommended)",
				getFieldEditorParent()));
	}
}
