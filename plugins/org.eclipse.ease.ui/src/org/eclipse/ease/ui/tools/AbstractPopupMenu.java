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
package org.eclipse.ease.ui.tools;

import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

public abstract class AbstractPopupMenu extends AbstractPopupItem implements IMenuListener {

	private final MenuManager fMenuManager;
	private IServiceLocator fServiceLocator;
	private boolean fSeparatorRequested = false;

	public AbstractPopupMenu(final String name) {
		super();

		fMenuManager = new MenuManager(name, getImageDescriptor(), null);
		fMenuManager.setRemoveAllWhenShown(true);
		fMenuManager.addMenuListener(this);
	}

	@Override
	public final IContributionItem getContribution(final IServiceLocator serviceLocator) {
		fServiceLocator = serviceLocator;
		return fMenuManager;
	}

	@Override
	public final void menuAboutToShow(final IMenuManager manager) {
		fSeparatorRequested = false;
		populate();
	}

	protected final void addPopup(final AbstractPopupItem item) {
		if (item == null)
			fSeparatorRequested = true;

		else if (item.isVisible()) {
			if (fSeparatorRequested) {
				fMenuManager.add(new Separator());
				fSeparatorRequested = false;
			}

			fMenuManager.add(item.getContribution(fServiceLocator));
		}
	}

	protected final void addSeparator() {
		addPopup(null);
	}

	@Override
	public CommandContributionItemParameter getContributionParameter() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return fMenuManager.getMenuText();
	}

	@Override
	protected ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor("/icons/eobj16/folder.png");
	}

	protected abstract void populate();
}
