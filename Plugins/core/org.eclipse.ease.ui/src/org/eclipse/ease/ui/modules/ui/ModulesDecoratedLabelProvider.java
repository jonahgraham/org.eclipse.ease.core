/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.jface.viewers.ILabelProvider;

public class ModulesDecoratedLabelProvider extends DecoratedLabelProvider {

	public ModulesDecoratedLabelProvider(ILabelProvider commonLabelProvider) {
		super(commonLabelProvider);

	}

	@Override
	public String getToolTipText(Object element) {

		if (element instanceof IPath)
			return null;

		if (element instanceof ModuleDefinition)
			return ((ModuleDefinition) element).getBundleID();

		if (element instanceof Method)
			return ModulesTools.getSignature((Method) element, true);

		return null;
	}

}
