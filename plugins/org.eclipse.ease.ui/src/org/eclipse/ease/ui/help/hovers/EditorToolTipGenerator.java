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

/**
 * @author VIDURA
 *
 */
public class EditorToolTipGenerator {

	protected static String getToolTipText(String text) {

		String toolTipText = "";
		Collection<ModuleDefinition> fLoadedModules = LoadedModuleCompletionProvider.getStaticLoadedModules();
		for (ModuleDefinition definition : fLoadedModules) {

			// check fields from modules
			for (Field field : definition.getFields()) {
				if (field.getName().equalsIgnoreCase(text)) {

					toolTipText = ModuleHelp.getConstantHelpTip(field);

					if (toolTipText == null) {
						return String.format("Public member of module %s with type %s.", definition.getName(), field.getType().getName());
					}
					break;
				}
			}

			// check methods from modules
			for (Method method : definition.getMethods()) {
				if (method.getName().equalsIgnoreCase(text)) {

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
		return toolTipText;
	}
}
