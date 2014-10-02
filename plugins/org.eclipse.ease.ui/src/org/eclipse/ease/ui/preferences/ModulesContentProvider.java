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
package org.eclipse.ease.ui.preferences;

import java.util.Collection;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;

public class ModulesContentProvider extends AbstractVirtualTreeProvider {

	@Override
	protected void populateElements(final Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			for (Object definition : (Collection<?>) inputElement) {
				if (definition instanceof ModuleDefinition)
					registerElement(((ModuleDefinition) definition).getPath().removeLastSegments(1), definition);
			}
		}
	}
}
