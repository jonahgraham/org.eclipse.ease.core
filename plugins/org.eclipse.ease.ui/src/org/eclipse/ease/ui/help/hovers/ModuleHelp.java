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

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesTools;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

public class ModuleHelp {

	/**
	 * Retrieve help page for a given module definition.
	 *
	 * @param definition
	 *            module definition to fetch help for
	 * @return help content (HTML body node)
	 */
	private static IMemento getHelpContent(final ModuleDefinition definition) {

		if (definition != null) {
			String helpLocation = definition.getHelpLocation(null);
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);
			try {
				IMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				return rootNode.getChild("body");
			} catch (Exception e) {
				Logger.error(Activator.PLUGIN_ID, "Cannot find the module help content ", e);
			}
		}

		return null;
	}

	/**
	 * Retrieve the css location for tooltips.
	 *
	 * @param definition
	 *            module definition to fetch css location for
	 * @return css location
	 */
	private static String getCSSUrl(final ModuleDefinition definition) {
		String helpLocation = definition.getHelpLocation(null);
		URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);
		return url.toString().replace(helpLocation, "/org.eclipse.ease.help/help/css/tooltip.css");
	}

	/**
	 * Retrieve help content for module definition.
	 *
	 * @param definition
	 *            module definition to fetch help for
	 * @return help content
	 */
	public static String getModuleHelpTip(final ModuleDefinition definition) {

		StringBuilder helpContent = new StringBuilder();

		IMemento bodyNode = getHelpContent(definition);
		if (bodyNode != null) {
			helpContent.append("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"");
			helpContent.append(getCSSUrl(definition));
			helpContent.append("\" /></head><body>");

			for (IMemento node : bodyNode.getChildren()) {
				if (node.getTextData().equalsIgnoreCase("Method Overview") || node.getTextData().equalsIgnoreCase("Constants"))
					break;

				helpContent.append(node);
			}

			helpContent.append("</body>");
		}

		return helpContent.toString();
	}

	/**
	 * Retrieve help content for module method.
	 *
	 * @param method
	 *            module method to fetch help for
	 * @return help content
	 */
	public static String getMethodHelpTip(final Method method) {

		StringBuilder helpContent = new StringBuilder();

		IMemento bodyNode = getHelpContent(ModulesTools.getDeclaringModule(method));
		if (bodyNode != null) {
			for (IMemento node : bodyNode.getChildren("div")) {
				if ((method.getName().equals(node.getString("title"))) && ("command".equals(node.getString("class")))) {
					// method found
					helpContent.append("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"");
					helpContent.append(getCSSUrl(ModulesTools.getDeclaringModule(method)));
					helpContent.append("\" /></head><body>");
					helpContent.append(node);
					helpContent.append("</body>");

					break;
				}
			}
		}

		return helpContent.toString();
	}

	/**
	 * Retrieve help content for module constants.
	 *
	 * @param field
	 *            module field to fetch help for
	 * @return help content
	 */
	public static String getConstantHelpTip(final Field field) {

		StringBuilder helpContent = new StringBuilder();

		IMemento bodyNode = getHelpContent(ModulesTools.getDeclaringModule(field));
		if (bodyNode != null) {
			for (IMemento node : bodyNode.getChildren("table")) {
				if ("constants".equals(node.getString("class"))) {
					for (IMemento tableRow : node.getChildren("tr")) {
						boolean found = false;
						for (IMemento tableCell : tableRow.getChildren("td")) {
							if (found) {
								// constant found
								helpContent.append("<head><link rel=\"stylesheet\" type=\"text/css\" href=\"");
								helpContent.append(getCSSUrl(ModulesTools.getDeclaringModule(field)));
								helpContent.append("\" /></head><body>");
								helpContent.append(tableCell.getTextData());
								helpContent.append("</body>");

								return helpContent.toString();
							}

							IMemento anchorNode = tableCell.getChild("a");
							found = (anchorNode != null) && (field.getName().equals(anchorNode.getString("id")));
						}
					}

					break;
				}
			}
		}

		return helpContent.toString();
	}
}
