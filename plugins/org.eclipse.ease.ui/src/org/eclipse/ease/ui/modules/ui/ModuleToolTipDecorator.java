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

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Toolkit;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tracker;

public class ModuleToolTipDecorator extends ColumnViewerToolTipSupport {
	Browser browser;

	/**
	 * @param viewer
	 * @param style
	 * @param manualActivation
	 */
	protected ModuleToolTipDecorator(ColumnViewer viewer, int style, boolean manualActivation) {
		super(viewer, style, manualActivation);
	}

	public static void enableFor(ColumnViewer viewer) {
		new ModuleToolTipDecorator(viewer, ToolTip.NO_RECREATE, false);
	}

	protected Composite createViewerToolTipContentArea(Event event, ViewerCell cell, Composite parent) {

		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginWidth = 8;
		gridLayout.marginHeight = 8;

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout);

		browser = new Browser(composite, SWT.NONE);
		String theText = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"../../org.eclipse.ease.help/help/css/modules_reference.css\" /></head><body>"
				+ getText(event) + "</body></html>";
		browser.setText(theText);

		java.awt.Point location = MouseInfo.getPointerInfo().getLocation();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point relativeLocation = composite.toDisplay((int) location.getX(), (int) location.getY());
		int x = location.x;
		int y = location.y;
		if (relativeLocation.x + 616 > screenSize.getWidth()) {
			x -= (int) (relativeLocation.x + 616 - screenSize.getWidth());
		}
		if (relativeLocation.y + 216 > screenSize.getHeight()) {
			y -= (int) (relativeLocation.y + 216 - screenSize.getHeight());
		}

		composite.getShell().setBounds(x, y, 616, 216);
		browser.setSize(600, 170);

		composite.getShell().open();

		composite.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event e) {

				Tracker tracker = new Tracker(composite.getParent(), SWT.RESIZE);
				tracker.setStippled(true);
				Rectangle rect = composite.getBounds();
				tracker.setRectangles(new Rectangle[] { rect });
				if (tracker.open()) {
					Rectangle after = tracker.getRectangles()[0];
					composite.getShell().setSize(after.width + 20, after.height + 20);
					browser.setSize(after.width + 20 - 16, after.height + 20 - 46);
				}
				tracker.dispose();
			}
		});

		while (!composite.getShell().isDisposed()) {
			if (!composite.getShell().getDisplay().readAndDispatch()) {
				composite.getShell().getDisplay().sleep();
			}
		}
		composite.getShell().getDisplay().dispose();

		composite.pack();
		return composite;
	}

}
