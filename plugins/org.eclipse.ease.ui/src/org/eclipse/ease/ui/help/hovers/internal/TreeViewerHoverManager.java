/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers.internal;

import org.eclipse.ease.ui.help.hovers.IHoverContentProvider;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This manager controls the layout, content, and visibility of an information control in reaction to mouse hover events issued by a tree. It overrides
 * <code>computeInformation</code>, so that the computation is performed in a dedicated background thread. This implies that the used
 * <code>IHoverContentProvider</code> objects must be capable of operating in a non-UI thread.
 */
public class TreeViewerHoverManager extends ControlHoverManager {

	private final TreeViewer fTreeViewer;

	public TreeViewerHoverManager(TreeViewer treeViewer, Control hoverArea, IWidgetTokenOwner owner, IHoverContentProvider hoverContent) {
		super(treeViewer.getTree(), hoverArea, owner, hoverContent);

		fTreeViewer = treeViewer;
	}

	@Override
	protected Rectangle getHoverInterestArea() {
		final TreeItem item = fTreeViewer.getTree().getItem(getHoverEventLocation());

		// default: use the whole table row
		final Rectangle area = item.getBounds();
		area.x = 0;
		area.width = item.getParent().getBounds().width;

		int offset = 0;
		for (final int index : fTreeViewer.getTree().getColumnOrder()) {
			if ((offset + fTreeViewer.getTree().getColumn(index).getWidth()) < getHoverEventLocation().x) {
				offset += fTreeViewer.getTree().getColumn(index).getWidth();
			} else {
				area.x = offset;
				area.width = fTreeViewer.getTree().getColumn(index).getWidth();
				break;
			}
		}

		return area;
	}

	@Override
	protected Object getHoverOrigin() {
		final TreeItem item = fTreeViewer.getTree().getItem(getHoverEventLocation());
		return (item != null) ? fTreeViewer : null;
	}

	@Override
	protected Object getHoverDetails() {
		final TreeItem item = fTreeViewer.getTree().getItem(getHoverEventLocation());
		return (item != null) ? item.getData() : null;
	}
}
