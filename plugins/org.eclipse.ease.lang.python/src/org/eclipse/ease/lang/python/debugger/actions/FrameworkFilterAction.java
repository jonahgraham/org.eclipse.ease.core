/*******************************************************************************
 * Copyright (c) 2017 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.debugger.actions;

import org.eclipse.ease.debugging.ScriptDebugVariable;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link ViewFilterAction} for hiding debug framewok specific variables.
 */
public class FrameworkFilterAction extends ViewFilterAction {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.jsdt.debug.internal.ui.actions.ViewFilterAction#getPreferenceKey()
	 */
	@Override
	protected String getPreferenceKey() {
		return "org.eclipse.ease.ui.show_frameworkvariables";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof ScriptDebugVariable) {
			final ScriptDebugVariable variable = (ScriptDebugVariable) element;
			return !variable.getName().startsWith("_pyease_");
		}

		return true;
	}

}
