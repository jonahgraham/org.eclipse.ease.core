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

import java.util.Map.Entry;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.viewers.StyledString;

/**
 * Provides completion proposals for variables stored in a script engine.
 */
public class VariablesCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && (context.getScriptEngine() != null) && (context.getType() == Type.NONE);
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		for (final Entry<String, Object> variable : context.getScriptEngine().getVariables().entrySet()) {
			// ignore mapped modules
			if (!variable.getKey().startsWith(EnvironmentModule.MODULE_PREFIX)) {
				if (matchesFilterIgnoreCase(variable.getKey())) {
					final String type = (variable.getValue() != null) ? variable.getValue().getClass().getSimpleName() : "null";
					final StyledString styledString = new StyledString(variable.getKey());
					styledString.append(" : " + type, StyledString.DECORATIONS_STYLER);
					styledString.append(" - " + "Variable", StyledString.QUALIFIER_STYLER);

					addProposal(styledString, variable.getKey(), Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png"),
							ScriptCompletionProposal.ORDER_FIELD, null);
				}
			}
		}
	}
}
