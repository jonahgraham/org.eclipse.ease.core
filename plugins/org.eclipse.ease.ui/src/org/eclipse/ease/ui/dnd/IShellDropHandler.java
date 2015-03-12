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

package org.eclipse.ease.ui.dnd;

import org.eclipse.ease.IScriptEngine;

/**
 * Handler interface for custom drop events for the script shell view.
 */
public interface IShellDropHandler {

	/**
	 * Verify that object can be handled.
	 *
	 * @param element
	 *            element to be dropped
	 * @return <code>true</code> when element can be handled
	 */
	boolean accepts(Object element);

	/**
	 * Perform the drop action.
	 *
	 * @param scriptEngine
	 *            script engine to execute drop action
	 * @param element
	 *            element to be dropped
	 */
	void performDrop(IScriptEngine scriptEngine, Object element);
}
