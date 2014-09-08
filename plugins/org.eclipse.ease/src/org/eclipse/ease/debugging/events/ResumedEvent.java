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

public class ResumedEvent extends AbstractEvent implements IDebuggerEvent {

	private final Thread fThread;

	private final int fType;

	public ResumedEvent(final Thread thread, final int type) {
		fThread = thread;
		fType = type;
	}

	public Thread getThread() {
		return fThread;
	}

	public int getType() {
		return fType;
	}
}
