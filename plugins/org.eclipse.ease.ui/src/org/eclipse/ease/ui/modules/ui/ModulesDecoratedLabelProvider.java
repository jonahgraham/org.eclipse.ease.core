/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.help.hovers.ModuleHelp;
import org.eclipse.ease.ui.tools.DecoratedLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;

public class ModulesDecoratedLabelProvider extends DecoratedLabelProvider {

	private final Styler fStrikeThroughStyler = new Styler() {

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.strikeout = true;
		}
	};;

	public ModulesDecoratedLabelProvider(final ILabelProvider commonLabelProvider) {
		super(commonLabelProvider);
	}

	@Override
	public String getToolTipText(final Object element) {

		if (element instanceof IPath)
			return null;

		if (element instanceof ModuleDefinition)
			return ModuleHelp.getModuleHelpTip((ModuleDefinition) element);

		if (element instanceof Method)
			return ModuleHelp.getMethodHelpTip((Method) element);

		if (element instanceof Field)
			return ModuleHelp.getConstantHelpTip((Field) element);

		return null;
	}

	@Override
	protected StyledString getStyledText(Object element) {
		final StyledString base = super.getStyledText(element);

		if ((element instanceof AccessibleObject) && (isDeprecated((AccessibleObject) element)))
			base.setStyle(0, base.length(), fStrikeThroughStyler);

		else if (element instanceof ModuleDefinition) {
			if (((ModuleDefinition) element).getModuleClass().getAnnotation(Deprecated.class) != null)
				base.setStyle(0, base.length(), fStrikeThroughStyler);
		}

		return base;
	}

	private static boolean isDeprecated(AccessibleObject element) {
		return element.getAnnotation(Deprecated.class) != null;
	}
}
