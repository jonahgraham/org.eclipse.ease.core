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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Interface for shell drop-ins. A shell drop-in is a composite adding
 * additional features to the script shell view. It resides in a sidebar of the
 * shell and is connected to the current script engine.
 */
public interface IShellDropin {

	/**
	 * Sets the script engine for this drop-in. If the engine is changed during
	 * runtime, this method gets called another time. For all other scripting
	 * events this drop-in should register a listener on the script engine.
	 * 
	 * @param engine
	 *            script engine used in shell view
	 */
	public void setScriptEngine(IScriptEngine engine);

	/**
	 * Create the drop-in visual components.
	 * 
	 * @param site
	 *            workbench part site this drop-in is registered to
	 * @param parent
	 *            parent container to render in
	 * @return composite created within parent container (may not be
	 *         <code>null</code>)
	 */
	public Composite createPartControl(final IWorkbenchPartSite site, final Composite parent);

	/**
	 * Get this drop-in title. The title is used to populate a tabitem.
	 * 
	 * @return drop-in title
	 */
	public String getTitle();
}
