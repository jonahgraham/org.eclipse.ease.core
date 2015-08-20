/*******************************************************************************
 * Copyright (c) 2015 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.completion.LoadedModuleCompletionProvider;
import org.eclipse.swt.widgets.Combo;

/**
 * @author VIDURA
 *
 */
public class EditorToolTipGenerator {

	/*
	 * Calculate the selected token using the input text in the combo
	 */
	protected static String getSelectedToken(Combo fInputCombo) {
		final String input = fInputCombo.getText() + ' ';

		int inputLength = input.length();
		int textStartPosition = 0;
		int textEndPosition = inputLength - 1;
		int caretPosition = fInputCombo.getCaretPosition();

		for (int i = caretPosition; i >= 0; i--) {
			if (input.charAt(i) == ' ') {
				textStartPosition = i;
				break;
			}
		}
		for (int j = caretPosition; j < inputLength; j++) {
			if (input.charAt(j) == ' ' || input.charAt(j) == '(') {
				textEndPosition = j;
				break;
			}
		}
		final String selectedText = input.substring(textStartPosition, textEndPosition);
		final String selectedToken = selectedText.trim();

		return selectedToken;
	}

	/*
	 * Calculate the toolTipText using the selected token
	 */
	protected static String getToolTipText(String text) {

		String toolTipText = "";
		Collection<ModuleDefinition> fLoadedModules = LoadedModuleCompletionProvider.getStaticLoadedModules();
		for (ModuleDefinition definition : fLoadedModules) {

			// check fields from modules
			for (Field field : definition.getFields()) {
				if (field.getName().equals(text)) {

					toolTipText = ModuleHelp.getConstantHelpTip(field);

					if (toolTipText == null) {
						return String.format("Public member of module %s with type %s.", definition.getName(), field.getType().getName());
					}
					break;
				}
			}

			// check methods from modules
			for (Method method : definition.getMethods()) {
				if (method.getName().equals(text)) {

					toolTipText = ModuleHelp.getMethodHelpTip(method);

					if (toolTipText == null) {
						StringBuilder sb = new StringBuilder();
						sb.append(String.format("Public method of module %s.\n", definition.getName()));
						sb.append("Signature and overloads:\n");
						for (Method overload : definition.getMethods()) {
							if (overload.getName().equals(method.getName())) {
								sb.append(overload.toGenericString());
								sb.append("\n");
							}
						}
						return sb.toString();
					}
					break;
				}
			}
		}
		if (toolTipText == "") {
			return null;
		}
		return toolTipText;
	}
}
