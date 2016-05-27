package org.eclipse.ease.lang.python.py4j.internal.ui;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.ease.lang.python.py4j.internal.Activator;
import org.eclipse.ease.lang.python.py4j.internal.Py4JScriptEnginePrefConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.plugin.PydevPlugin;

public class Py4JPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Py4JPreferencePage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		List<String[]> interps = new ArrayList<>();
		interps.add(
				new String[] { "Run \"python\" from the system PATH", Py4JScriptEnginePrefConstants.INTERPRETER_CHOICE_PATH });
		interps.add(
				new String[] { "Run custom Python command (below)", Py4JScriptEnginePrefConstants.INTERPRETER_CHOICE_CUSTOM });
		interps.addAll(getOptionalPyDevOptions());

		String[][] labelAndValues = interps.toArray(new String[interps.size()][]);
		RadioGroupFieldEditor groupFieldEditor = new RadioGroupFieldEditor(Py4JScriptEnginePrefConstants.INTERPRETER,
				"Select which Python interpreter to use:", 1, labelAndValues, getFieldEditorParent());
		addField(groupFieldEditor);

		addField(new FileFieldEditor(Py4JScriptEnginePrefConstants.INTERPRETER_CUSTOM, "Custom Python command name", false,
				FileFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));

	}

	private Collection<? extends String[]> getOptionalPyDevOptions() {
		try {
			return getPyDevOptions();
		} catch (NoClassDefFoundError e) {
			return Collections.emptyList();
		}
	}

	/**
	 * @throws NoClassDefFoundError
	 *             if optional dependency PyDev is not installed
	 */
	private Collection<? extends String[]> getPyDevOptions() throws NoClassDefFoundError {
		List<String[]> interps = new ArrayList<>();

		IInterpreterManager pythonInterpreterManager = PydevPlugin.getPythonInterpreterManager();
		IInterpreterInfo[] interpreterInfos = pythonInterpreterManager.getInterpreterInfos();
		if (interpreterInfos.length > 0) {
			interps.add(new String[] { "Default PyDev interpreter",
					Py4JScriptEnginePrefConstants.INTERPRETER_CHOICE_PYDEV_PREFIX });
		}
		for (IInterpreterInfo info : interpreterInfos) {
			String nameForUI = info.getNameForUI();
			String name = info.getName();
			interps.add(new String[] { "PyDev interpreter: " + nameForUI,
					Py4JScriptEnginePrefConstants.INTERPRETER_CHOICE_PYDEV_PREFIX + name });
		}

		return interps;
	}

}
