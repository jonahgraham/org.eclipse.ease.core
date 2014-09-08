/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.events;

import org.eclipse.ease.Script;

public class ScriptTerminatedEvent extends AbstractEvent implements IDebuggerEvent {

	private final Script fScript;

	private final Thread fThread;

	public ScriptTerminatedEvent(final Script script, final Thread thread) {
		fScript = script;
		fThread = thread;
	}

	public Script getScript() {
		return fScript;
	}

	public Thread getThread() {
		return fThread;
	}
}
