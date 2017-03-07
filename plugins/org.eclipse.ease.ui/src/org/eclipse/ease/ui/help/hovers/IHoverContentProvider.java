/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;

public interface IHoverContentProvider {

	/**
	 * @param origin
	 *            element that triggered the hover action
	 * @param detail
	 *            detail on the hover action. The content of this parameter depends on the type of the origin object. May be <code>null</code>.
	 * @return html content as string or a {@link BrowserInformationControlInput}
	 */
	Object getContent(Object origin, Object detail);

	/**
	 * Callback allowing to populate the popup toolbar
	 * @param control browser control
	 * @param toolBarManager toolbar being populated
	 */
	void populateToolbar(BrowserInformationControl control, ToolBarManager toolBarManager);
}
