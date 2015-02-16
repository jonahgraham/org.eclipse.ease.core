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
package org.eclipse.ease.ui.scripts.repository;

import org.eclipse.ease.ui.scripts.repository.impl.ScriptEvent;

public interface IScriptListener {

	/**
	 * Notify of script change events.
	 *
	 * @param event
	 *            script event
	 */
	void notify(ScriptEvent event);
}
