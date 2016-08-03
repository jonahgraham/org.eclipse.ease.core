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
package org.eclipse.ease.ui.scripts.preferences;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.IInputValidator;

public class URIValidator implements IInputValidator {

	@Override
	public String isValid(String text) {
		try {
			if (URI.createURI(text).isRelative())
				return "relative URI detected";
		} catch (Exception e) {
			return "Invalid URI detected";
		}

		return null;
	}
}
