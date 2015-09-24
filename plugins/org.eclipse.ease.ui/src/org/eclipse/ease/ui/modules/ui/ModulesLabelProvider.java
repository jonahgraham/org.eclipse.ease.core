/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * code taken from
 * shttp://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet015CustomTooltipsForTree.java
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class ModulesLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		final StyledString text = new StyledString();

		if (element instanceof ModuleDefinition) {
			text.append(((ModuleDefinition) element).getName());

		} else if (element instanceof IPath) {
			text.append(((IPath) element).lastSegment());

		} else if (element instanceof Field) {
			text.append(((Field) element).getName());
			text.append(" : " + ((Field) element).getType().getSimpleName(), StyledString.DECORATIONS_STYLER);

		} else if (element instanceof Method) {
			text.append(ModulesTools.getSignature((Method) element, true));
		}

		return text;
	}

	@Override
	public Image getImage(final Object element) {

		if (element instanceof IPath)
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/folder.png", true);

		if (element instanceof ModuleDefinition) {
			final ImageDescriptor icon = ((ModuleDefinition) element).getImageDescriptor();
			if (icon == null)
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/module.png", true);

			return icon.createImage();
		}

		if (element instanceof Method)
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/function.png", true);

		if (element instanceof Field)
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/field.png", true);

		return null;
	}
}
