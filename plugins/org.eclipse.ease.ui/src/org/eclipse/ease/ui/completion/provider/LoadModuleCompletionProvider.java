/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class LoadModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (context.getType() == Type.STRING_LITERAL) && (context.getCaller().endsWith("loadModule"));
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		// create a path to search for
		final IPath filterPath = new Path(context.getFilter());
		final IPath searchPath;
		if ((filterPath.segmentCount() > 1) || (filterPath.hasTrailingSeparator())) {
			if (filterPath.hasTrailingSeparator())
				searchPath = filterPath.makeAbsolute();
			else
				searchPath = filterPath.makeAbsolute().removeLastSegments(1);
		} else
			searchPath = new Path("/");

		final Collection<String> pathProposals = new HashSet<String>();

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		final Map<String, ModuleDefinition> availableModules = scriptService.getAvailableModules();
		for (final Entry<String, ModuleDefinition> moduleEntry : availableModules.entrySet()) {
			final Path modulePath = new Path(moduleEntry.getKey());
			if (searchPath.isPrefixOf(modulePath)) {
				// this is a valid candidate
				if ((searchPath.segmentCount() + 1) == modulePath.segmentCount()) {
					// add module proposal
					final StyledString displayString = new StyledString(modulePath.lastSegment());
					if (!moduleEntry.getValue().isVisible())
						displayString.append(" (hidden)", StyledString.DECORATIONS_STYLER);

					addProposal(proposals, context, displayString, moduleEntry.getKey(),
							Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/module.png"), 0);

				} else {
					// add path proposal; collect them first to avoid duplicates
					pathProposals.add(modulePath.removeLastSegments(1).toString());
				}
			}
		}

		// add path proposals
		for (final String pathProposal : pathProposals)
			addProposal(proposals, context, pathProposal, pathProposal + "/",
					PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), 10);

		return proposals;
	}
}