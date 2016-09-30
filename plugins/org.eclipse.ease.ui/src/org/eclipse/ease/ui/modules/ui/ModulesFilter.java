/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ModulesFilter {

	@Deprecated
	private ModulesFilter() {
	}

	public static ViewerFilter visible(final ModulesContentProvider contentProvider) {

		return new ViewerFilter() {
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, Object element) {

				if (element instanceof ModuleDefinition)
					return ((ModuleDefinition) element).isVisible();

				if (element instanceof IPath) {
					for (final Object node : contentProvider.getChildren(element)) {
						if (select(viewer, parentElement, node))
							return true;
					}

					return false;
				}

				return ((ModuleDefinition) parentElement).isVisible();
			}
		};
	}

	public static ViewerFilter modulesOnly() {

		return new ViewerFilter() {
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {

				if (element instanceof ModuleDefinition)
					return true;

				if (element instanceof IPath)
					return true;

				return false;
			}
		};
	}

	public static ViewerFilter all() {

		return new ViewerFilter() {
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				return true;
			}
		};
	}
}
