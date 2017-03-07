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

package org.eclipse.ease.ui.help.hovers.internal;

import org.eclipse.ease.ui.help.hovers.IHoverContentProvider;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;

public class StaticHoverContentProvider implements IHoverContentProvider {
	private final String fContent;

	public StaticHoverContentProvider(String content) {
		fContent = content;
	}

	@Override
	public String getContent(Object origin, Object detail) {
		return fContent;
	}

	@Override
	public void populateToolbar(BrowserInformationControl control, ToolBarManager toolBarManager) {
		// nothing to do
	}
}
