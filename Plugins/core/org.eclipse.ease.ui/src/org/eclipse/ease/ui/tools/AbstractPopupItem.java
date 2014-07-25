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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

public abstract class AbstractPopupItem {

	public IContributionItem getContribution(final IServiceLocator serviceLocator) {
		final CommandContributionItemParameter contributionParameter = getContributionParameter();
		contributionParameter.serviceLocator = serviceLocator;
		contributionParameter.label = getDisplayName();
		contributionParameter.visibleEnabled = true;
		contributionParameter.icon = getImageDescriptor();

		return new CommandContributionItem(contributionParameter);
	}

	public boolean isVisible() {
		return true;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	protected abstract CommandContributionItemParameter getContributionParameter();

	public abstract String getDisplayName();

	protected abstract ImageDescriptor getImageDescriptor();
}
