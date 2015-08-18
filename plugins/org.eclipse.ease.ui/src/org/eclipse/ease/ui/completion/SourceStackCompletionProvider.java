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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ease.completion.CompletionContext;
import org.eclipse.ease.completion.CompletionSource;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.completion.ICompletionSource.SourceType;
import org.eclipse.jface.fieldassist.ContentProposal;

/**
 * Completion provider for following {@link ICompletionContext#getSourceStack()}.
 * 
 * Context needs to be refined, meaning that every {@link ICompletionSource} needs to have a class. {@link ICompletionProvider} can use
 * {@link CompletionContext#refineSourceStack(List)} to refine context
 * 
 * @author Martin Kloesch
 *
 */
public class SourceStackCompletionProvider extends AbstractCompletionProvider {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#getProposals(org.eclipse.ease.modules.ICompletionContext)
	 */
	@Override
	public Collection<ICompletionSource> getProposals(ICompletionContext context) {
		List<ICompletionSource> proposals = new ArrayList<ICompletionSource>();
		if (context != null && context.getSourceStack() != null && !context.getSourceStack().isEmpty()) {
			ICompletionSource parent = context.getSourceStack().get(context.getSourceStack().size() - 1);

			Class<?> root = parent.getClazz();

			switch (parent.getSourceType()) {
			case MODULE_METHOD:
			case CLASS_METHOD:
			case LOCAL_FUNCTION:
				if (parent.getObject() != null && parent.getObject() instanceof Method) {
					root = ((Method) parent.getObject()).getReturnType();
				} else {
					root = null;
				}
			default:
				break;
			}

			if (root != null) {
				addMatches(root, context.getFilter(), proposals);
			}
		}

		return proposals;
	}

	/**
	 * Adds all matching methods and members from given class to the given list of proposals.
	 * 
	 * @param clazz
	 *            Class to search for methods and members in.
	 * @param toMatch
	 *            pattern to match methods and members against.
	 * @param proposals
	 *            List of {@link ContentProposal} to append matches to.
	 */
	protected void addMatches(Class<?> clazz, String toMatch, final Collection<ICompletionSource> proposals) {
		// Show overloads only once
		Set<String> addedVariables = new HashSet<String>();

		// add fields from modules
		for (Field field : clazz.getFields()) {
			if ((field.getName().startsWith(toMatch)) && (toMatch.length() < field.getName().length())) {
				if (!addedVariables.contains(field.getName())) {
					addedVariables.add(field.getName());
					proposals.add(new CompletionSource(SourceType.CLASS_FIELD, field.getName(), clazz, field, CompletionDescriptionFormatter.format(field,
							clazz)));
				}
			}
		}

		// add methods from modules
		for (Method method : clazz.getMethods()) {
			if ((method.getName().startsWith(toMatch)) && (toMatch.length() < method.getName().length())) {
				if (!addedVariables.contains(method.getName())) {
					addedVariables.add(method.getName());
					proposals.add(new CompletionSource(SourceType.CLASS_METHOD, method.getName(), clazz, method, CompletionDescriptionFormatter.format(method,
							clazz)));
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
		// No need to parse code.

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.IContextProvider#refineContext(org.eclipse.ease.modules.ICompletionContext)
	 */
	@Override
	public ICompletionContext refineContext(ICompletionContext context) {
		// No need to refine context.
		return null;
	}
}