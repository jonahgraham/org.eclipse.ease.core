/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.tools.AbstractPopupItem;
import org.eclipse.ease.ui.tools.AbstractPopupMenu;
import org.eclipse.jface.resource.ImageDescriptor;

public class ScriptPopupMenu extends AbstractPopupMenu {

	private final List<AbstractPopupItem> mItems = new ArrayList<AbstractPopupItem>();

	public ScriptPopupMenu(final String name) {
		super(name);
	}

	public void addItem(final AbstractPopupItem item) {
		mItems.add(item);
	}

	@Override
	protected void populate() {
		for (final AbstractPopupItem item : mItems)
			addPopup(item);
	}

	/**
	 * @param segment
	 * @return
	 */
	public boolean hasSubMenu(final String name) {
		for (final AbstractPopupItem item : mItems) {
			if (item.getDisplayName().equals(name))
				return true;
		}

		return false;
	}

	/**
	 * @param segment
	 * @return
	 */
	public ScriptPopupMenu getSubMenu(final String name) {
		for (final AbstractPopupItem item : mItems) {
			if ((item.getDisplayName().equals(name)) && (item instanceof ScriptPopupMenu))
				return (ScriptPopupMenu) item;
		}

		return null;
	}

	@Override
	protected ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor("/images/macro_folder.gif");
	}
}
