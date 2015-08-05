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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.ease.completion.ICompletionAnalyzer;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

/**
 * Dispatcher class create code completion proposals.
 * 
 * First checks all registered {@link ICompletionProvider} objects to get the {@link ICompletionContext} for the desired line.
 * 
 * Then uses all registered {@link ICompletionProvider} objects to calculate the {@link IContentProposal} array for {@link #getProposals(String, int)}.
 * 
 * TODO: Refactor to use multi-threading.
 * 
 * @author Martin Kloesch
 *
 */
public class CompletionProviderDispatcher implements IContentProposalProvider {
	/**
	 * Static code analyzer to split given line of code to base {@link ICompletionContext} for {@link ICompletionProvider#refineContext(ICompletionContext)}.
	 */
	private ICompletionAnalyzer fAnalyzer;

	/**
	 * Setter method for ICompletionAnalyzer.
	 * 
	 * @param analyzer
	 *            {@link ICompletionAnalyzer} for completion calculation.
	 */
	public void setAnalyzer(ICompletionAnalyzer analyzer) {
		fAnalyzer = analyzer;
	}

	/**
	 * Set of all registered {@link ICompletionProvider} objects.
	 * 
	 * First they will be called in order to refine {@link ICompletionContext}. Then called to get proposals.
	 */
	private final Set<ICompletionProvider> fCompletionProviders = new HashSet<ICompletionProvider>();

	/**
	 * Registers a (new) {@link ICompletionProvider} to the internal set.
	 * 
	 * These providers are called in order to refine {@link ICompletionContext} and then to create completion proposals.
	 * 
	 * @param provider
	 *            {@link ICompletionProvider} to be registered.
	 */
	public void registerContextProvider(ICompletionProvider provider) {
		fCompletionProviders.add(provider);
	}

	/**
	 * Unregistered a (previously registered) {@link ICompletionProvider} from the internal set.
	 * 
	 * @param provider
	 *            {@link ICompletionProvider} to be unregistered.
	 */
	public void unregisterContextProvider(ICompletionProvider provider) {
		fCompletionProviders.remove(provider);
	}

	public char[] getActivationChars() {
		return null;
	}

	/**
	 * Dispatches the given piece of code to all registered {@link ICompletionProvider} objects for them to parse relevant information from.
	 * 
	 * @param code
	 *            Code to be added and parsed by {@link ICompletionProvider} objects.
	 */
	public void addCode(String code) {
	}
	
	/**
	 * Overload for {@link #getContext(String, int)} with default parameter for position.
	 * @see #getContext(String, int)
	 */
	public ICompletionContext getContext(String contents) {
		if (contents != null) {
			return getContext(contents, contents.length());
		}
		return null;
	}

	/**
	 * Parses a given String to an {@link ICompletionContext} using {@link ICompletionAnalyzer} and {@link ICompletionProvider} interfaces.
	 * 
	 * @param contents
	 *            String with contents to be parsed to completion context.
	 * @param position
	 *            End position of contents. (default: contents.length())
	 * @return {@link ICompletionContext} for given input, <code>null</code> in case of error.
	 */
	public ICompletionContext getContext(String contents, int position) {
		ICompletionContext context = null;
		if (fAnalyzer != null) {
			context = fAnalyzer.getContext(contents, position);
			// TODO: Refine context
		}
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
	 */
	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		return new IContentProposal[0];
	}
}