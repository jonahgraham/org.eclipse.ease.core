/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.ui;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.dnd.IShellDropHandler;
import org.eclipse.ease.ui.scripts.repository.IScript;

public class ScriptDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(IScriptEngine scriptEngine, Object element) {
		return element instanceof IScript;
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		if (element instanceof IScript)
			scriptEngine.executeAsync("include('script:/" + ((IScript) element).getPath() + "');");
	}
}
