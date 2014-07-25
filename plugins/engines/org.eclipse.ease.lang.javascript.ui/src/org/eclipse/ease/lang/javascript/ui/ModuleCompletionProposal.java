/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.lang.javascript.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;

public class ModuleCompletionProposal extends ModuleProposalCalculator implements IJavaCompletionProposalComputer {

	@Override
	public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
		Collection<ModuleDefinition> modules = getLoadedModules(context);

		try {
			Matcher matcher = matchLastToken(context);
			if (matcher.matches()) {
				if (".".equals(matcher.group(1))) {
					// code tries to call a class method, not a function
					return Collections.EMPTY_LIST;
				}

				List<CompletionProposal> proposals = createProposals(context, modules, matcher.group(2));
				return proposals;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();

			// ignore
		}

		return Collections.EMPTY_LIST;
	}

	private List<CompletionProposal> createProposals(final ContentAssistInvocationContext context, final Collection<ModuleDefinition> modules,
			final String prefix) {
		List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

		for (ModuleDefinition definition : modules) {
			for (Field field : definition.getFields()) {
				if (field.getName().startsWith(prefix)) {
					proposals.add(new CompletionProposal(field.getName(), context.getInvocationOffset() - prefix.length(), prefix.length(), field.getName()
							.length()));
				}

			}

			for (Method method : definition.getMethods()) {
				if (method.getName().startsWith(prefix)) {
					proposals.add(new CompletionProposal(method.getName() + "()", context.getInvocationOffset() - prefix.length(), prefix.length(), method
							.getName().length() + 2));
				}
			}
		}

		return proposals;
	}

	@Override
	public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub
	}
}
