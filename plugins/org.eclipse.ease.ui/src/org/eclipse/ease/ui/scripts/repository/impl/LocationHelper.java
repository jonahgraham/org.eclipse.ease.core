/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.repository.impl;

 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;

 public class LocationHelper {

	 public static String toLocation(final String value) {
		 if ("main".equalsIgnoreCase(value)) {
			 // main toolbar
			 return null;
		 }

		 final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.views");
		 for (final IConfigurationElement e : config) {
			 if ("view".equals(e.getName())) {
				 String id = e.getAttribute("id");
				 if (id.equals(value))
					 return value;

				 String name = e.getAttribute("name");
				 if (name.equals(value))
					 return id;
			 }
		 }

		 return null;
	 }
 }
