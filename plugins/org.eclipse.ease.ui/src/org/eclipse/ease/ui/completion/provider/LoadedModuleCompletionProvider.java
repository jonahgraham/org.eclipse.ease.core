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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.modules.ui.ModulesTools;
import org.eclipse.jface.viewers.StyledString;

public class LoadedModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return context.getType() == Type.NONE;
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		for (final ModuleDefinition definition : context.getLoadedModules()) {
			// field proposals
			for (final Field field : definition.getFields()) {
				if (matchesFilterIgnoreCase(field.getName())) {
					final StyledString styledString = new StyledString(field.getName());
					styledString.append(" : " + field.getType().getSimpleName(), StyledString.DECORATIONS_STYLER);
					styledString.append(" - " + definition.getName(), StyledString.QUALIFIER_STYLER);

					addProposal(styledString, field.getName(), Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png"),
							ScriptCompletionProposal.ORDER_FIELD, null);
				}
			}

			// method proposals
			for (final Method method : definition.getMethods()) {
				if (matchesFilterIgnoreCase(method.getName())) {
					final StyledString styledString = ModulesTools.getSignature(method, true);
					styledString.append(" - " + definition.getName(), StyledString.QUALIFIER_STYLER);

					if ((method.getParameterTypes().length - ModulesTools.getOptionalParameterCount(method)) > 0) {
						addProposal(styledString, method.getName() + "(", Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png"),
								ScriptCompletionProposal.ORDER_METHOD, null);
					} else {
						addProposal(styledString, method.getName() + "()", Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png"),
								ScriptCompletionProposal.ORDER_METHOD, null);
					}
				}
			}
		}
	}
}
