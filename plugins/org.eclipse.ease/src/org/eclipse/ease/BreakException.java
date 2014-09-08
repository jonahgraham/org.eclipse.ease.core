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
package org.eclipse.ease;

public class BreakException extends RuntimeException {

	private static final long serialVersionUID = -4157933914171239048L;

	private Object fCondition = null;

	public BreakException() {
	}

	public BreakException(final Object condition) {
		super();
		fCondition = condition;
	}

	public Object getCondition() {
		return fCondition;
	}
}
