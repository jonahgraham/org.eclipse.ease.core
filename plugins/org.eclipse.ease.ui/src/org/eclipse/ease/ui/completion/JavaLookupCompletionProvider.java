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

import org.eclipse.ease.completion.CompletionSource;
import org.eclipse.ease.completion.ICompletionContext;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * @author Martin Kloesch
 *
 */
public class JavaLookupCompletionProvider extends AbstractCompletionProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#addCode(java.lang.String)
	 */
	@Override
	public void addCode(String code) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#refineContext(org.eclipse.ease.completion.ICompletionContext)
	 */
	@Override
	public ICompletionContext refineContext(ICompletionContext context) {
		ICompletionContext refinedContext = null;
		if (context != null && context.getSourceStack() != null && !context.getSourceStack().isEmpty()) {
			ICompletionSource root = context.getSourceStack().get(0);
			if (root != null && root.getName() != null) {
				if (root.getSourceType() == SourceType.CONSTRUCTOR) {
					try {
						Class<?> clazz = Class.forName(root.getName());
						refinedContext = createRefinedContext(context, new CompletionSource(SourceType.JAVA_CONSTRUCTOR, root.getName(), clazz, new Object(), null));
					} catch (ClassNotFoundException e) {
						// ignore
					}
				}
			}
		}
		return refinedContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.completion.ICompletionProvider#getProposals(org.eclipse.ease.completion.ICompletionContext)
	 */
	@Override
	public Collection<ICompletionSource> getProposals(ICompletionContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
