/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.preferences;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class EmptyNodesFilter extends ViewerFilter {

	private final StringTreeContentProvider mContentProvider;

	public EmptyNodesFilter(StringTreeContentProvider contentProvider) {
		mContentProvider = contentProvider;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof StringBuilder) {
			for (Object child : mContentProvider.getChildCollection(element)) {
				if (select(viewer, parentElement, child))
					return true;
			}

			return false;
		}

		return true;
	}
}
