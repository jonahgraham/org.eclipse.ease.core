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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ModuleToolTipDecorator extends ColumnViewerToolTipSupport {

	private ModuleToolTipDecorator(ColumnViewer viewer) {
		super(viewer, ToolTip.NO_RECREATE, false);
	}

	public static void enableFor(ColumnViewer viewer) {
		new ModuleToolTipDecorator(viewer);
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {

		final Browser browser = new Browser(parent, SWT.NONE);
		browser.setJavascriptEnabled(false);
		browser.setText("<html>" + getText(event) + "</html>");
		browser.setSize(600, 200);

		browser.setFocus();

		return browser;
	}

	@Override
	public boolean isHideOnMouseDown() {
		return false;
	}
}
