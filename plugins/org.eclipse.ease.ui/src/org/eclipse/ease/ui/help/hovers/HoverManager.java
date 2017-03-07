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

import org.eclipse.ease.ui.help.hovers.internal.ControlHoverManager;
import org.eclipse.ease.ui.help.hovers.internal.StaticHoverContentProvider;
import org.eclipse.ease.ui.help.hovers.internal.TableViewerHoverManager;
import org.eclipse.ease.ui.help.hovers.internal.TreeViewerHoverManager;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Manager for HTML hovers. Typically a hover manager is responsible for a part instance, eg a view or editor. The manager takes care that at max 1 hover will
 * be visible at a time for all registered controls.
 */
public class HoverManager implements IWidgetTokenOwner, IWidgetTokenOwnerExtension {

	/** Token to guarantee to just open 1 hover at a time. */
	private IWidgetTokenKeeper fTokenKeeper;

	/** Token priority. */
	private int fPriority = 0;

	/** Root container for registered hovers. */
	private final Composite fHoverContainer;

	/**
	 * Creates a new HoverManager for a given container.
	 *
	 * @param hoverContainer
	 *            the container is typically the root composite of a part.
	 */
	public HoverManager(Composite hoverContainer) {
		fHoverContainer = hoverContainer;
	}

	/**
	 * Add a hover for a control element.
	 *
	 * @param control
	 *            control to bind hover to
	 * @param hoverContent
	 *            hover content provider
	 */
	public ControlHoverManager addHover(Control control, IHoverContentProvider hoverContent) {
		return new ControlHoverManager(control, fHoverContainer, this, hoverContent);
	}

	/**
	 * Add a hover for a control element.
	 *
	 * @param control
	 *            control to bind hover to
	 * @param hoverContent
	 *            constant hover content
	 */
	public ControlHoverManager addHover(Control control, String hoverContent) {
		return new ControlHoverManager(control, fHoverContainer, this, new StaticHoverContentProvider(hoverContent));
	}

	/**
	 * Add a hover for a table viewer.
	 *
	 * @param tableViewer
	 *            tableviewer to bind hover to
	 * @param hoverContent
	 *            hover content provider
	 */
	public ControlHoverManager addHover(TableViewer tableViewer, IHoverContentProvider hoverContent) {
		return new TableViewerHoverManager(tableViewer, fHoverContainer, this, hoverContent);
	}

	/**
	 * Add a hover for a tree viewer.
	 *
	 * @param treeViewer
	 *            treeviewer to bind hover to
	 * @param hoverContent
	 *            hover content provider
	 */
	public ControlHoverManager addHover(TreeViewer treeViewer, IHoverContentProvider hoverContent) {
		return new TreeViewerHoverManager(treeViewer, fHoverContainer, this, hoverContent);
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
		if (fTokenKeeper == null) {
			fTokenKeeper = requester;
			fPriority = priority;
			return true;
		}

		if (fTokenKeeper.equals(requester)) {
			fPriority = priority;
			return true;
		}

		if (priority > fPriority) {
			if (fTokenKeeper instanceof IWidgetTokenKeeperExtension) {
				if (((IWidgetTokenKeeperExtension) fTokenKeeper).requestWidgetToken(this, priority)) {
					fTokenKeeper = requester;
					fPriority = priority;
					return true;
				}

			} else {
				if (fTokenKeeper.requestWidgetToken(this)) {
					fTokenKeeper = requester;
					fPriority = priority;
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
		if ((fTokenKeeper == null) || (fTokenKeeper.equals(requester))) {
			fTokenKeeper = requester;
			return true;
		}

		return false;
	}

	@Override
	public void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper) {
		if (tokenKeeper.equals(fTokenKeeper)) {
			fTokenKeeper = null;
			fPriority = 0;
		}
	}
}
