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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.completion.CompletionContext;
import org.eclipse.ease.completion.CompletionSource;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * {@link ICompletionProvider} parsing code for custom annotation.
 * 
 * The annotation has the following syntax: It must start with "@type" followed by at least one space or tabulator followed by the variable name followed by at
 * least one space or tabulator followed by the class name.
 * 
 * The parser checks for this annotation anywhere in the code but to not break the interpreters the syntax must be used in comments.
 * 
 * @example: "@type foo java.lang.String"
 * 
 *
 * @author Martin Kloesch
 *
 */
public class CustomAnnotationCompletionProvider extends AbstractCompletionProvider {

	/**
	 * Regular expression to query for all annotated variable types.
	 */
	private static final Pattern CUSTOM_ANNOTATION_PATTERN = Pattern.compile("@type[ |\\t]+([a-zA-Z_$][a-zA-Z0-9_$]*)[ |\\t]+([a-zA-Z_$][a-zA-Z0-9_$\\.]*)");

	/**
	 * Map storing information about annotated variables and their types.
	 */
	protected Map<String, Class<?>> fAnnotatedVariables = new HashMap<String, Class<?>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#addCode(java.lang.String)
	 */
	@Override
	public void addCode(String code) {
		Matcher matcher = CUSTOM_ANNOTATION_PATTERN.matcher(code);

		// Get all annotated variables
		while (matcher.find()) {
			try {
				fAnnotatedVariables.put(matcher.group(1), Class.forName(matcher.group(2)));
			} catch (ClassNotFoundException e) {
				// Ignore if class could not be found
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#refineContext(org.eclipse.ease.completion.ICompletionContext)
	 */
	@Override
	public ICompletionContext refineContext(ICompletionContext context) {
		// Do not refine context if working in live shell
		if (fScriptEngine != null) {
			return null;
		}

		// Check if valid refinable context given
		if (context == null || context.getSourceStack() == null || context.getSourceStack().isEmpty()) {
			return null;
		}

		// Get first element in source stack
		ICompletionSource src = context.getSourceStack().get(0);
		Class<?> clazz = fAnnotatedVariables.get(src.getName());

		// If first element found the rest of the source stack can easily be parsed.
		if (clazz != null) {
			List<ICompletionSource> newStack = context.getSourceStack();
			newStack.set(0, new CompletionSource(SourceType.LOCAL_VARIABLE, src.getName(), clazz, new Object()));
			newStack = CompletionContext.refineSourceStack(newStack);
			if (newStack == null) {
				return null;
			} else {
				return new CompletionContext(context.getInput(), context.getFilter(), newStack);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#getProposals(org.eclipse.ease.completion.ICompletionContext)
	 */
	@Override
	public Collection<ICompletionSource> getProposals(ICompletionContext context) {
		List<ICompletionSource> proposals = new ArrayList<ICompletionSource>();

		// Only add root level matches
		if (fScriptEngine != null || context != null && (context.getSourceStack() == null || context.getSourceStack().isEmpty())) {
			for (String varName : fAnnotatedVariables.keySet()) {
				if (varName.startsWith(context.getFilter())) {
					proposals.add(new CompletionSource(SourceType.LOCAL_VARIABLE, varName, fAnnotatedVariables.get(varName), new Object()));
				}
			}
		}
		return proposals;
	}
}
