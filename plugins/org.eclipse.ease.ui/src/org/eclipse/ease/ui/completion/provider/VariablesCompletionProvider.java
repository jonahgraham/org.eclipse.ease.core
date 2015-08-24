/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.completion.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.viewers.StyledString;

/**
 * Provides completion proposals for variables stored in a script engine.
 */
public class VariablesCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && (context.getScriptEngine() != null);
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		for (final Entry<String, Object> variable : context.getScriptEngine().getVariables().entrySet()) {
			// ignore mapped modules
			if (!variable.getKey().startsWith(EnvironmentModule.MODULE_PREFIX)) {

				final String type = (variable.getValue() != null) ? variable.getValue().getClass().getSimpleName() : "null";
				final StyledString styledString = new StyledString(variable.getKey() + " : " + type);
				styledString.append(" - " + "Variable");

				addProposal(proposals, context, styledString, variable.getKey(), JavaCompletionProvider.getSharedImage(ISharedImages.IMG_FIELD_PUBLIC),
						ScriptCompletionProposal.ORDER_FIELD);
			}
		}

		return proposals;
	}
}