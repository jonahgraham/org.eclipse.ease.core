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
package org.eclipse.ease.ui.modules.ui;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.Logger;
import org.eclipse.ui.PlatformUI;

public class ModuleHelp {

	/**
	 * Generating help tool tips for modules
	 * 
	 * @param element
	 * @return module tool tip
	 */
	public static String getModuleHelpTip(final ModuleDefinition module) {

		String moduleToolTip = null;
		XMLMemento bodyNode = null;

		if (module != null) {
			moduleToolTip = "";
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);
			try {

				XMLMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));

				if (rootNode.getChild("body") != null) {
					bodyNode = (XMLMemento) rootNode.getChild("body");
				} else {
					return null;
				}

				IMemento[] bodyChildNodes = bodyNode.getChildren();
				int lengthBodyChildNodes = bodyChildNodes.length;
				for (int i = 0; i < lengthBodyChildNodes; i++) {
					if (bodyChildNodes[i].getTextData().equalsIgnoreCase("Method Overview") || bodyChildNodes[i].getTextData().equalsIgnoreCase("Constants")) {
						break;
					}
					moduleToolTip += bodyChildNodes[i].toString();
				}

				String cssUrl = url.toString().replace(helpLocation, "/org.eclipse.ease.help/help/css/tooltip.css");
				moduleToolTip = "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssUrl + "\" /></head><body>" + moduleToolTip + "</body>";

			} catch (Exception e) {
				Logger.logError("Cannot find the module help content ", e);
				return null;
			}
		}
		return moduleToolTip;
	}

	/**
	 * Generating help tool tips for module methods
	 * 
	 * @param element
	 * @return method tool tip
	 */
	public static String getMethodHelpTip(final Method method) {

		ModuleDefinition module = ModulesTools.getDeclaringModule(method);
		String methodToolTip = null;
		String methodName = method.getName();
		XMLMemento bodyNode = null;

		if (module != null) {
			methodToolTip = "";
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);

			try {

				XMLMemento theMethodNode = null;
				XMLMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));

				if (rootNode.getChild("body") != null) {
					bodyNode = (XMLMemento) rootNode.getChild("body");
				} else {
					return null;
				}

				boolean found = false;
				IMemento[] bodyChildNodes = bodyNode.getChildren();
				int lengthBodyChildNodes = bodyChildNodes.length;
				int indexOfFirstMethodNode = 0;

				for (int i = 0; i < lengthBodyChildNodes; i++) {
					if (bodyChildNodes[i].getTextData().equalsIgnoreCase("Methods")) {
						indexOfFirstMethodNode = i + 1;
						break;
					}
				}

				for (int i = indexOfFirstMethodNode; i < lengthBodyChildNodes; i++) {

					if (isNull(bodyChildNodes[i], bodyChildNodes[i].getChild("h3"), bodyChildNodes[i].getChild("h3").getChild("a"))) {
						return null;
					}

					if (bodyChildNodes[i].getChild("h3").getChild("a").getTextData().equalsIgnoreCase(methodName)) {
						methodToolTip = bodyChildNodes[i].getChild("h3").getChild("a").getTextData();
						theMethodNode = (XMLMemento) bodyChildNodes[i];
						found = true;
						break;
					}
				}

				if (found) {
					String cssUrl = url.toString().replace(helpLocation, "/org.eclipse.ease.help/help/css/tooltip.css");
					methodToolTip = "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssUrl + "\" /></head><body>" + theMethodNode.toString()
							+ "</body>";
				} else {
					return null;
				}

			} catch (Exception e) {
				Logger.logError("Cannot find the method help content ", e);
				return null;
			}
		}
		return methodToolTip;
	}

	/**
	 * Generating help tool tips for constants
	 * 
	 * @param element
	 * @return constant tool tip
	 */
	public static String getConstantHelpTip(final Field field) {

		ModuleDefinition module = ModulesTools.getDeclaringModule(field);
		String constantToolTip = null;
		String fieldName = field.getName();
		XMLMemento bodyNode = null;

		if (module != null) {
			constantToolTip = "";
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);

			try {

				XMLMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				if (rootNode.getChild("body") != null) {
					bodyNode = (XMLMemento) rootNode.getChild("body");
				} else {
					return null;
				}

				boolean constantFound = false;

				for (IMemento node : bodyNode.getChildren()) {
					if (constantFound) {
						XMLMemento tableNode = null;
						if (node != null) {
							tableNode = (XMLMemento) node;
						} else {
							return null;
						}

						boolean isFirst = true;
						for (IMemento rowNode : tableNode.getChildren()) {
							if (isFirst) {
								isFirst = false;
								continue;
							}
							if (isNull(rowNode, rowNode.getChild("td"), rowNode.getChild("td").getChild("a"))) {
								return null;
							}
							if (rowNode.getChild("td").getChild("a").getTextData().equalsIgnoreCase(fieldName)) {
								String theconstantToolTip = "<table class=\"constants\"><tr><th>Constant</th><th>Description</th></tr>" + rowNode.toString()
										+ "</table>";
								String cssUrl = url.toString().replace(helpLocation, "/org.eclipse.ease.help/help/css/tooltip.css");
								constantToolTip = "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssUrl + "\" /></head><body>"
										+ theconstantToolTip + "</body>";
								break;
							}
						}
						break;
					}

					if (node.getTextData() != null && !constantFound) {
						if (node.getTextData().equalsIgnoreCase("Constants")) {
							constantFound = true;
							continue;
						}
					}
				}

			} catch (Exception e) {
				Logger.logError("Cannot find the constant help content", e);
				return null;
			}
		}
		return constantToolTip;
	}

	/**
	 * Check whether HTML nodes are null
	 * 
	 * @return true if a null node is found, false otherwise
	 */
	private static boolean isNull(IMemento node1, IMemento node2, IMemento node3) {
		if (node1 == null || node2 == null || node3 == null) {
			return true;
		}
		return false;
	}

}
