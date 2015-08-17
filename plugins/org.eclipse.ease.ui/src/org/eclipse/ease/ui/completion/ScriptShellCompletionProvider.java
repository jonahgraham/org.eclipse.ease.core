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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.completion.CompletionContext;
import org.eclipse.ease.completion.CompletionSource;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * {@link ICompletionProvider} for script shell.
 * 
 * Holds reference to {@link IScriptEngine} and queries it for actual types at runtime.
 * 
 * @author Martin Kloesch
 *
 */
public class ScriptShellCompletionProvider extends AbstractCompletionProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#getProposals(org.eclipse.ease.modules.ICompletionContext)
	 */
	@Override
	public Collection<ICompletionSource> getProposals(ICompletionContext context) {
		List<ICompletionSource> proposals = new ArrayList<ICompletionSource>();

		// Only add root level matches
		if (fScriptEngine != null && context != null && (context.getSourceStack() == null || context.getSourceStack().isEmpty())) {
			addMatches(context.getFilter(), proposals);
		}

		return proposals;
	}

	/**
	 * Adds all matches for given input to the given list of proposals.
	 * 
	 * @param filter
	 *            Filter input for matches.
	 * @param proposals
	 *            Existing list of proposals that needs to be refined.
	 */
	private void addMatches(String filter, Collection<ICompletionSource> proposals) {
		Set<String> addedVariables = new HashSet<String>();

		for (Entry<String, Object> var : fScriptEngine.getVariables().entrySet()) {
			if (var.getKey().startsWith(filter)) {
				if (!addedVariables.contains(var.getKey())) {
					if (var.getValue() instanceof Method) {
						proposals.add(new CompletionSource(SourceType.LOCAL_FUNCTION, var.getKey(), null, var));
					} else {
						proposals.add(new CompletionSource(SourceType.LOCAL_VARIABLE, var.getKey(), var.getClass(), var));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.IContextProvider#addCode(java.lang.String)
	 */
	@Override
	public void addCode(String code) {
		// Can be ignored since live information is available via fEngine.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.IContextProvider#refineContext(org.eclipse.ease.modules.ICompletionContext)
	 */
	@Override
	public ICompletionContext refineContext(ICompletionContext context) {
		if (fScriptEngine == null) {
			return null;
		}
		
		if (context == null || context.getSourceStack() == null || context.getSourceStack().isEmpty()) {
			return null;
		}

		ICompletionSource src = context.getSourceStack().get(0);
		Object var = fScriptEngine.getVariable(src.getName());

		if (var == null) {
			return null;
		}

		List<ICompletionSource> newStack = context.getSourceStack();
		if (src.getSourceType().equals(SourceType.METHOD)) {
			newStack.set(0, new CompletionSource(SourceType.LOCAL_FUNCTION, src.getName(), var.getClass(), var));
		} else if (src.getSourceType().equals(SourceType.INSTANCE)){
			newStack.set(0, new CompletionSource(SourceType.LOCAL_VARIABLE, src.getName(), var.getClass(), var));
		} else {
			return null;
		}

		newStack = CompletionContext.refineSourceStack(newStack);
		if (newStack == null) {
			return null;
		} else {
			return new CompletionContext(context.getInput(), context.getFilter(), newStack);
		}
	}
}