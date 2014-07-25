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

package org.eclipse.ease.ui.modules.decorators;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class ModulesExplorerMethodDecorator implements
		ILightweightLabelDecorator {

	public static String ID = "org.eclipse.ease.ui.modules.decorators.ModulesExplorerMethodDecorator";

	@Override
	public void addListener(ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// nothing to do
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof Method) {

			decoration.addSuffix(" : "
					+ ((Method) element).getReturnType().getSimpleName());

		}

	}
}
