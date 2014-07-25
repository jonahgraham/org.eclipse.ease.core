/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts.ui;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;

public class ScriptContentProvider extends AbstractVirtualTreeProvider {

	@Override
	protected void populateElements(Object inputElement) {
		if (inputElement instanceof IRepositoryService) {
			Collection<IScript> scripts = new HashSet<IScript>(((IRepositoryService) inputElement).getScripts());

			for (IScript script : scripts)
				registerElement(script.getPath().removeLastSegments(1), script);
		}
	}
}
