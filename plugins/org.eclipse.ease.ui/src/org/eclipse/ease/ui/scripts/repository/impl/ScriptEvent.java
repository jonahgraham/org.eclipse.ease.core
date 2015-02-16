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
package org.eclipse.ease.ui.scripts.repository.impl;

import org.eclipse.ease.ui.repository.IScript;

public class ScriptEvent {

	/** Parameter changed. */
	public static final int PARAMETER_CHANGE = 2;

	/** Script deleted. */
	public static final int DELETE = 4;

	/** Script added. */
	public static final int ADD = 1;

	private final IScript fScript;
	private final int fType;
	private final Object fEventData;

	/**
	 * Script repository event constructor.
	 *
	 * @param script
	 *            affected script
	 * @param type
	 *            event type
	 * @param eventData
	 *            specific event data
	 */
	public ScriptEvent(final IScript script, final int type, final Object eventData) {
		fScript = script;
		fType = type;
		fEventData = eventData;
	}

	/**
	 * Get affected script.
	 *
	 * @return affected script
	 */
	public IScript getScript() {
		return fScript;
	}

	/**
	 * Get event type
	 *
	 * @return event type
	 */
	public int getType() {
		return fType;
	}

	/**
	 * Get specific event data. On PARAMETER_CHANGE contains a Map<String, String> of changed parameters and their old value. If value is <code>null</code> the
	 * parameter was added to the script. New parameter values can be queried from the script instance.
	 *
	 * @return specific event data
	 */
	public Object getEventData() {
		return fEventData;
	}
}
