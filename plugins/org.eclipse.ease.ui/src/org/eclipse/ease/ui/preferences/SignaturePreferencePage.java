package org.eclipse.ease.ui.preferences;

import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SignaturePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// Way to access this preference
	// final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
	// System.out.println("pref " + prefs.getString(IPreferenceConstants.DEFAULT_KEYSTORE_PATH));

	public SignaturePreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("EASE Security");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {

		addField(new BooleanFieldEditor(IPreferenceConstants.RUN_WITHOUT_SIGN_LOCAL, "Allow executing &LOCAL scripts that are not signed",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(IPreferenceConstants.RUN_WITHOUT_SIGN_REMOTE, "Allow executing &REMOTE scripts that are not signed(Not Recommended)",
				getFieldEditorParent()));
	}
}
