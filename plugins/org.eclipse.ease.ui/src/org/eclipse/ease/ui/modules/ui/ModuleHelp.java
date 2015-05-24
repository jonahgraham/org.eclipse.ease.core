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
import org.eclipse.ui.PlatformUI;

public class ModuleHelp {

	/**
	 * Generating help tool tips for modules
	 * 
	 * @param element
	 * @return module tool tip
	 */
	public static String getModuleHelpTip(final Object element) {

		ModuleDefinition module = (ModuleDefinition) element;
		String moduleToolTip = "";

		if (module != null) {
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);

			try {
				XMLMemento rootNode = null;
				XMLMemento bodyNode = null;

				rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				bodyNode = (XMLMemento) rootNode.getChild("body");

				if (bodyNode.getChild("div").getChild("h1").getTextData() != null) {
					moduleToolTip = bodyNode.getChild("div").getChild("h1").getTextData();
				}
				if (bodyNode.getChild("div").getChild("p").getTextData() != null) {
					moduleToolTip += "\n\n" + bodyNode.getChild("div").getChild("p").getTextData();
				}
				IMemento[] bodyChildNodes = bodyNode.getChildren();
				int lengthBodyChildNodes = bodyChildNodes.length;
				int lengthModuleChildNodes = 0;
				for (int i = 0; i < lengthBodyChildNodes; i++) {
					if (bodyChildNodes[i].getTextData().equalsIgnoreCase("Methods")) {
						lengthModuleChildNodes = i;
						break;
					}
				}
				for (int i = 1; i < lengthModuleChildNodes; i++) {
					if (bodyChildNodes[i].getTextData() != null && !bodyChildNodes[i].getType().equalsIgnoreCase("table")) {
						moduleToolTip += "\n\n" + bodyChildNodes[i].getTextData();
					} else if (bodyChildNodes[i].getType().equalsIgnoreCase("table")) {
						IMemento[] tableChildNodes = bodyChildNodes[i].getChildren();
						moduleToolTip += "\n" + "--------------------";
						int lengthTableChildNodes = tableChildNodes.length;
						for (int j = 1; j < lengthTableChildNodes; j++) {
							IMemento[] tdNodes = tableChildNodes[j].getChildren("td");
							String theName = tdNodes[0].getChild("a").getTextData();
							String description = tdNodes[1].getTextData();
							moduleToolTip += "\n" + theName.trim() + "    -    " + description.trim();
						}
					}
				}

			} catch (Exception e) {
				moduleToolTip = "Cannot find the module help page";
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
	public static String getMethodHelpTip(final Object element) {

		ModuleDefinition module = ModulesTools.getDeclaringModule((Method) element);
		Method method = (Method) element;
		String methodToolTip = "";
		String methodName = method.getName();

		if (module != null) {
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);

			try {
				XMLMemento rootNode = null;
				XMLMemento bodyNode = null;
				XMLMemento theMethodNode = null;

				rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				bodyNode = (XMLMemento) rootNode.getChild("body");
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
					if (bodyChildNodes[i].getChild("h3").getChild("a") != null
							&& bodyChildNodes[i].getChild("h3").getChild("a").getTextData().equalsIgnoreCase(methodName)) {
						methodToolTip = bodyChildNodes[i].getChild("h3").getChild("a").getTextData();
						theMethodNode = (XMLMemento) bodyChildNodes[i];
						found = true;
						break;
					}
				}

				if (found) {
					IMemento[] methodChildNodes = theMethodNode.getChildren();
					if (methodChildNodes[1].getTextData() != null && methodChildNodes[1].getType().equalsIgnoreCase("p")) {
						methodToolTip += "\n\nSynopsis\n" + "--------------\n" + methodChildNodes[1].getTextData();
					}
					if (methodChildNodes[2].getTextData() != null && methodChildNodes[2].getType().equalsIgnoreCase("p")) {
						methodToolTip += "\n\nDescription\n" + "--------------\n" + methodChildNodes[2].getTextData();
					}
					if (methodChildNodes.length > 3 && methodChildNodes[3].getType().equalsIgnoreCase("table")) {
						IMemento[] parameterTableNodes = methodChildNodes[3].getChildren();
						methodToolTip += "\n\nParameters\n" + "--------------\n";
						int lengthParameterTableNodes = parameterTableNodes.length;
						for (int j = 1; j < lengthParameterTableNodes; j++) {
							IMemento[] tableTdNodes = parameterTableNodes[j].getChildren();
							methodToolTip += tableTdNodes[0].getTextData() + "\t" + tableTdNodes[1].getTextData() + "\t" + tableTdNodes[2].getTextData() + "\n";
						}
					}
					if (methodChildNodes.length > 4 && methodChildNodes[4].getType().equalsIgnoreCase("p")) {
						methodToolTip += "\nReturns\n" + "--------------\n" + methodChildNodes[4].getTextData();
						if (methodChildNodes[4].getChild("code") != null) {
							methodToolTip += methodChildNodes[4].getChild("code").getTextData();
						} else if (methodChildNodes[4].getChild("i") != null) {
							methodToolTip += methodChildNodes[4].getChild("i").getTextData();
						}
					}
				} else {
					methodToolTip = "Cannot find the method help content";
				}

			} catch (Exception e) {
				methodToolTip = "Cannot find the method help page";
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
	public static String getConstantHelpTip(final Object element) {

		ModuleDefinition module = ModulesTools.getDeclaringModule((Field) element);
		Field field = (Field) element;
		String constantToolTip = "";
		String fieldName = field.getName();

		if (module != null) {
			String helpLocation = module.getHelpLocation(module.getName());
			URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);

			try {
				XMLMemento rootNode = null;
				XMLMemento bodyNode = null;
				XMLMemento tableNode = null;

				rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				bodyNode = (XMLMemento) rootNode.getChild("body");
				boolean constantFound = false;

				for (IMemento node : bodyNode.getChildren()) {
					if (constantFound) {
						tableNode = (XMLMemento) node;
						for (IMemento rowNode : tableNode.getChildren()) {
							if (rowNode.getChild("td") != null && rowNode.getChild("td").getChild("a").getTextData().equalsIgnoreCase(fieldName)) {
								IMemento[] tdNodes = rowNode.getChildren("td");
								constantToolTip = tdNodes[0].getChild("a").getTextData() + "\n" + tdNodes[1].getTextData();
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
				constantToolTip = "Cannot find the field help page";
			}
		}
		return constantToolTip;
	}

}
