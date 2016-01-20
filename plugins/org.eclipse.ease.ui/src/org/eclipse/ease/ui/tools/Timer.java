/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.tools;

public class Timer {

	private final long fStart;

	public Timer() {
		fStart = System.nanoTime();
	}

	public long getTime() {
		return System.nanoTime() - fStart;
	}

	public long getMilliSeconds() {
		return (getTime() + 500000) / 1000000;
	}
}
