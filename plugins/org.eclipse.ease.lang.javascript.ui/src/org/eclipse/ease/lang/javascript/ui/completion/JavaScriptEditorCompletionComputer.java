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

package org.eclipse.ease.lang.javascript.ui.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.lang.javascript.JavaScriptCompletionAnalyzer;
import org.eclipse.ease.ui.completion.CompletionProviderDispatcher;
import org.eclipse.ease.ui.completion.ICompletionProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

/**
 * {@link IJavaCompletionProposalComputer} for EASE JavaScript.
 * 
 * Internally uses {@link CompletionProviderDispatcher} and dynamic {@link ICompletionProvider} to create proposals.
 * 
 * @author Martin Kloesch
 *
 */
public class JavaScriptEditorCompletionComputer implements IJavaCompletionProposalComputer {
	/**
	 * {@link CompletionProviderDispatcher} to handle the actual creation of proposals.
	 */
	private final CompletionProviderDispatcher fDispatcher = new CompletionProviderDispatcher();

	/**
	 * Constructor sets up {@link CompletionProviderDispatcher} and loads registered {@link ICompletionProvider}.
	 */
	public JavaScriptEditorCompletionComputer() {
		fDispatcher.setAnalyzer(new JavaScriptCompletionAnalyzer());
		for (ICompletionProvider provider : CompletionProviderDispatcher.getProviders(null)) {
			fDispatcher.registerCompletionProvider(provider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#computeCompletionProposals(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext
	 * , org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<?> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor arg1) {
		List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
		if (context != null) {
			// Get content of document
			IDocument document = context.getDocument();
			if (document != null) {
				String content = document.get();
				
				// Add code, ICompletionProviders must check internally if anything changed.
				fDispatcher.addCode(content);

				// Calculate context for input
				ICompletionContext ctx = fDispatcher.getContext(content);
				if (ctx != null) {
					// Actually add proposals
					for (ICompletionSource src : fDispatcher.calculateProposals(ctx)) {
						int offset = ctx.getFilter().length();
						proposals.add(new CompletionProposal(src.getName(), context.getInvocationOffset() - offset, offset, src.getName().length()));
					}
				}
			}
		}

		return proposals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#computeContextInformation(org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext
	 * , org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<?> computeContextInformation(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {
		// TODO Auto-generated method stub
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
	 */
	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
	 */
	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub

	}

}
