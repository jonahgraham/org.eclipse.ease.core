/*******************************************************************************
 * Copyright (c) 2015, 2016 Martin Kloesch and others.
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
import org.eclipse.ease.Logger;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.lang.javascript.ui.PluginConstants;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.CodeCompletionAggregator;
import org.eclipse.ease.ui.completion.ICompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

/**
 * {@link IJavaCompletionProposalComputer} for EASE JavaScript.
 *
 * Internally uses {@link CodeCompletionAggregator} and dynamic {@link ICompletionProvider} to create proposals. This CompletionComputer is created once for
 * JSDT JavaScript Editors. So all editors of this type share the same instance.
 */
public class JavaScriptEditorCompletionComputer implements IJavaCompletionProposalComputer {
	/**
	 * {@link CodeCompletionAggregator} to handle the actual creation of proposals.
	 */
	private final CodeCompletionAggregator fCompletionAggregator = new CodeCompletionAggregator();

	/**
	 * Constructor sets up {@link CodeCompletionAggregator} and loads registered {@link ICompletionProvider}.
	 */
	public JavaScriptEditorCompletionComputer() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final ScriptType scriptType = scriptService.getAvailableScriptTypes().get(JavaScriptHelper.SCRIPT_TYPE_JAVASCRIPT);

		fCompletionAggregator.setScriptType(scriptType);
		fCompletionAggregator.setCodeParser(scriptType.getCodeParser());
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
		if (context != null) {
			// get content of document
			final IDocument document = context.getDocument();
			if (document != null) {

				// extract resource
				Object resource = null;
				final IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				if (activePart instanceof IEditorPart) {
					final IEditorInput input = ((IEditorPart) activePart).getEditorInput();
					if (input instanceof FileEditorInput)
						resource = ((FileEditorInput) input).getFile();
				}

				try {
					final String relevantText = document.get(0, context.getInvocationOffset());

					final int cursorPosition = context.getInvocationOffset();
					final int selectionRange = context.getViewer().getSelectedRange().y;
					List<ScriptCompletionProposal> proposals = fCompletionAggregator.getCompletionProposals(resource, relevantText, cursorPosition,
							selectionRange, monitor);
					return new ArrayList<>(proposals);

				} catch (final BadLocationException e) {
					Logger.error(PluginConstants.PLUGIN_ID, "Failed to calculate proposals for JavaScript editor", e);
				}
			}
		}

		return Collections.emptyList();
	}

	// XXX: We are returning List because of a bug in JSDT's API that will hopefully be fixed before it is released
	// in Neon, but in case it isn't we need to protect against the API changing. Note the API in question is
	// provisional. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=493810
	@Override
	public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		// nothing to do
		return null;
	}

	@Override
	public void sessionEnded() {
		// nothing to do
	}

	@Override
	public void sessionStarted() {
		// nothing to do
	}
}
