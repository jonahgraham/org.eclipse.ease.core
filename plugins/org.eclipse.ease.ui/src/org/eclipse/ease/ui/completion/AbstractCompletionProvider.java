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

import java.util.List;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.completion.CompletionContext;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;

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

	/**
	 * Creates a refined context based on the given original and a newly calculated root node.
	 * 
	 * @param orig
	 *            Original context to create refined copy of. Must not be <code>null</code>.
	 * @param root
	 *            New root element in source stack. Must not be <code>null</code>
	 * @return Refined context if successful, <code>null</code> in case context could not be refined.
	 */
	protected ICompletionContext createRefinedContext(ICompletionContext orig, ICompletionSource root) {
		List<ICompletionSource> newStack = orig.getSourceStack();
		newStack.set(0, root);

		newStack = CompletionContext.refineSourceStack(newStack);
		if (newStack != null) {
			return new CompletionContext(orig.getInput(), orig.getFilter(), newStack);
		}

		return null;
	}
}
