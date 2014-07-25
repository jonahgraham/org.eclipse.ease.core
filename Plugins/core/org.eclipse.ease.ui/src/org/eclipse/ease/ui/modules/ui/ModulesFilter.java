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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ModulesFilter {

	@Deprecated
	private ModulesFilter() {

	}

	public static ViewerFilter visible(final ModulesComposite composite) {

		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {

				if (element instanceof Field)
					return ((ModuleDefinition) parentElement).isVisible();

				if (element instanceof Method)
					return ((ModuleDefinition) parentElement).isVisible();

				if (element instanceof ModuleDefinition)
					return ((ModuleDefinition) element).isVisible();

				if (element instanceof IPath) {

					boolean visible = false;

					Object[] children = ((ModulesContentProvider) (composite
							.getTreeViewer().getContentProvider()))
							.getChildren(element);

					for (Object node : children) {
						visible |= select(viewer, parentElement, node);

					}
					return visible;

				}

				return true;
			}

		};

	}

	public static ViewerFilter modulesOnly() {

		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {

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
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return true;
			}

		};

	}
}
