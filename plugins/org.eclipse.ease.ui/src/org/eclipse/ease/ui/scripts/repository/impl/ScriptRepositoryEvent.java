/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts.repository.impl;

import org.eclipse.ease.ui.repository.IScript;

public class ScriptRepositoryEvent {

	public static final int PARAMETER_CHANGE = 2;

	public static final int DELETE = 4;

	public static final int ADD = 1;

	private final IScript fScript;
	private final int fType;
	private final Object fEventData;

	public ScriptRepositoryEvent(IScript script, int type, Object eventData) {
		fScript = script;
		fType = type;
		fEventData = eventData;
	}

	public IScript getScript() {
		return fScript;
	}

	public int getType() {
		return fType;
	}

	public Object getEventData() {
		return fEventData;
	}
}
