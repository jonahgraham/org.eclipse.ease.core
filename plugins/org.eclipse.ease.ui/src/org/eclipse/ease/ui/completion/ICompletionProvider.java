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

import java.util.Collection;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;

/**
 * Interface to provide context for auto-completion and actual matches..
 * 
 * Used by {@link CompletionProviderDispatcher} to gather information about the context of a given line of code. Then used to calculate the actual proposals.
 * 
 * @author Martin Kloesch
 *
 */
public interface ICompletionProvider {
	/**
	 * Called when piece of code is added. Code can be parsed to store information about e.g. loaded modules.
	 * 
	 * @param code
	 *            Code that has been added.
	 */
	void addCode(String code);

	/**
	 * Sets the given script engine. If implementations need access to script engine, they should keep a reference to this.
	 * 
	 * @param engine	{@link IScriptEngine} currently in use.
	 */
	void setScriptEngine(IScriptEngine engine);

	/**
	 * Tries to refine a given context. The given context will probably have been created by {@link ICompletionAnalyzer#getContext(String,int)} and needs further
	 * refinement.
	 * 
	 * @param context
	 *            Base context to be refined.
	 * @return Refined context if match found, <code>null</code> if context could not be refined.
	 */
	ICompletionContext refineContext(ICompletionContext context);

	/**
	 * Calculate all matching proposals for given {@link ICompletionContext}.
	 * 
	 * @param context
	 *            {@link ICompletionContext} with necessary information to calculate proposals.
	 * @return Collection of matching proposals.
	 */
	Collection<ICompletionSource> getProposals(ICompletionContext context);
}