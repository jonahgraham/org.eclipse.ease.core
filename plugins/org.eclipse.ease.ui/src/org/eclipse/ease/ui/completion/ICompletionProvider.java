/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *     Christian Pontesegger - rewrite of this interface
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.Activator;

public interface ICompletionProvider {

	/** Trace enablement for code completion. */
	boolean TRACE_CODE_COMPLETION = Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease.ui/debug/codeCompletion"));

	/**
	 * Calculate all matching proposals for given {@link ICompletionContext}.
	 *
	 * @param context
	 *            {@link ICompletionContext} with necessary information to calculate proposals.
	 * @return Collection of matching proposals.
	 */
	Collection<? extends ScriptCompletionProposal> getProposals(ICompletionContext context);

	/**
	 * Query indicating that this providers completion proposals should be taken into account.
	 *
	 * @param context
	 *            {@link ICompletionContext} with necessary information to calculate proposals.
	 * @return <code>true</code> when active
	 */
	boolean isActive(ICompletionContext context);
}