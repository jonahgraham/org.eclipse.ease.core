/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.CodeCompletionAggregator;
import org.eclipse.ease.ui.completion.ICompletionProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.ILocalScope;
import org.python.pydev.core.IToken;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.structure.CompletionRecursionException;
import org.python.pydev.editor.codecompletion.CompletionRequest;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant;
import org.python.pydev.editor.codecompletion.revisited.ConcreteToken;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceToken;

public class PydevCompletionParticipant implements IPyDevCompletionParticipant {
	/**
	 * {@link CodeCompletionAggregator} to handle the actual creation of proposals.
	 */
	private final CodeCompletionAggregator fCompletionAggregator = new CodeCompletionAggregator();

	/**
	 * Constructor sets up {@link CodeCompletionAggregator} and loads registered {@link ICompletionProvider}.
	 */
	public PydevCompletionParticipant() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final ScriptType scriptType = scriptService.getAvailableScriptTypes().get(PythonHelper.SCRIPT_TYPE_PYTHON);

		fCompletionAggregator.setScriptType(scriptType);
		fCompletionAggregator.setCodeParser(scriptType.getCodeParser());
	}

	// Suppress warnings in line with PyDev examples
	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection getGlobalCompletions(CompletionRequest request, ICompletionState state) throws MisconfigurationException {
		String relevantText;
		try {
			relevantText = request.doc.get(0, request.documentOffset);
		} catch (BadLocationException e) {
			return Collections.emptyList();
		}

		return fCompletionAggregator.getCompletionProposals(request.editorFile, relevantText, request.documentOffset,
				0 /* Unused by CompletionContext */, null);
	}


	// Suppress warnings in line with PyDev examples
	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection getStringGlobalCompletions(CompletionRequest request, ICompletionState state) throws MisconfigurationException {
		return getGlobalCompletions(request, state);
	}

	@Override
	public Collection<IToken> getCompletionsForMethodParameter(ICompletionState state, ILocalScope localScope, Collection<IToken> interfaceForLocal) {
		return Collections.emptyList();
	}


	@Override
	public Collection<IToken> getCompletionsForTokenWithUndefinedType(ICompletionState state, ILocalScope localScope, Collection<IToken> interfaceForLocal) {
		return Collections.emptyList();
	}

	@Deprecated
	@Override
	public Collection<Object> getArgsCompletion(ICompletionState state, ILocalScope localScope, Collection<IToken> interfaceForLocal) {
		return Collections.emptyList();
	}


	@Override
	public Collection<IToken> getCompletionsForType(ICompletionState state) throws CompletionRecursionException {
		return Collections.emptyList();
	}


}
