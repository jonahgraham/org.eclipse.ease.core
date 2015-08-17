/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import org.eclipse.ease.IScriptEngine;

/**
 * Abstract base class for all {@link ICompletionProvider}.
 * 
 * @author Martin Kloesch
 *
 */
public abstract class AbstractCompletionProvider implements ICompletionProvider {

	/**
	 * {@link IScriptEngine} currently in use. Can also be used to distinguish between live-shell and script mode.
	 */
	protected IScriptEngine fScriptEngine;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#setScriptEngine(org.eclipse.ease.IScriptEngine)
	 */
	@Override
	public void setScriptEngine(IScriptEngine engine) {
		fScriptEngine = engine;
	}
}
