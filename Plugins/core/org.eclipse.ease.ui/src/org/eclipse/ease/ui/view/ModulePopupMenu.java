/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.ease.ui.tools.AbstractPopupItem;
import org.eclipse.ease.ui.tools.AbstractPopupMenu;

public class ModulePopupMenu extends AbstractPopupMenu {

	private final List<AbstractPopupItem> fItems = new ArrayList<AbstractPopupItem>();

	public ModulePopupMenu(final String name) {
		super(name);
	}

	@Override
	protected void populate() {
		for (AbstractPopupItem item : fItems)
			addPopup(item);
	}

	public void addEntry(final AbstractPopupItem item) {
		fItems.add(item);
	}

	public void sortEntries() {
		Collections.sort(fItems, new Comparator<AbstractPopupItem>() {

			@Override
			public int compare(final AbstractPopupItem o1, final AbstractPopupItem o2) {
				if ((o1 instanceof AbstractPopupMenu) && (!(o2 instanceof AbstractPopupMenu)))
					return -1;

				if ((o2 instanceof AbstractPopupMenu) && (!(o1 instanceof AbstractPopupMenu)))
					return 1;

				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
	}

	public List<AbstractPopupItem> getEntries() {
		return fItems;
	}
}
