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
package org.eclipse.ease.ui.view;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.scripts.ui.ScriptComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

public class ScriptDropin implements IShellDropin, IScriptEngineProvider {

	private IScriptEngine fEngine;
	private ScriptComposite fComposite;

	@Override
	public void setScriptEngine(IScriptEngine engine) {
		fEngine = engine;

		if (fComposite != null)
			fComposite.setEngine(fEngine.getDescription().getID());
	}

	@Override
	public Composite createPartControl(final IWorkbenchPartSite site, final Composite parent) {
		fComposite = new ScriptComposite(this, site, parent, SWT.NONE);

		if (fEngine != null)
			fComposite.setEngine(fEngine.getDescription().getID());

		return fComposite;
	}

	@Override
	public String getTitle() {
		return "Scripts";
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fEngine;
	}
}
