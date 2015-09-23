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

package org.eclipse.ease.ui.completion;

import java.util.Collection;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.ui.help.hovers.IHelpResolver;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

public abstract class AbstractCompletionProvider implements ICompletionProvider {

	protected void addProposal(final Collection<ScriptCompletionProposal> proposals, final ICompletionContext context, final StyledString displayString,
			String replacementString, final ImageDescriptor image, final int priority) {

		if (context.getFilter() != null) {
			// eventually filter proposal
			if (!replacementString.startsWith(context.getFilter()))
				// filter proposal
				return;

			// do not filter, but adapt replacementString
			replacementString = replacementString.substring(context.getFilter().length());
		}

		proposals.add(new ScriptCompletionProposal(displayString, replacementString, context.getOffset(), image, priority));
	}

	protected void addProposal(final Collection<ScriptCompletionProposal> proposals, final ICompletionContext context, final StyledString displayString,
			String replacementString, final ImageDescriptor image, final int priority, final IHelpResolver helpResolver) {

		if (context.getFilter() != null) {
			// eventually filter proposal
			if (!replacementString.startsWith(context.getFilter()))
				// filter proposal
				return;

			// do not filter, but adapt replacementString
			replacementString = replacementString.substring(context.getFilter().length());
		}

		proposals.add(new ScriptCompletionProposal(displayString, replacementString, context.getOffset(), image, priority, helpResolver));
	}

	protected void addProposal(final Collection<ScriptCompletionProposal> proposals, final ICompletionContext context, final String displayString,
			String replacementString, final ImageDescriptor image, final int priority) {

		if (context.getFilter() != null) {
			// eventually filter proposal
			if (!replacementString.startsWith(context.getFilter()))
				// filter proposal
				return;

			if (replacementString.equals(context.getFilter()))
				// proposal equals existing text, do not display anymore
				return;

			// do not filter, but adapt replacementString
			replacementString = replacementString.substring(context.getFilter().length());
		}

		proposals.add(new ScriptCompletionProposal(displayString, replacementString, context.getOffset(), image, priority));
	}

	protected void addProposal(final Collection<ScriptCompletionProposal> proposals, final ICompletionContext context, final String displayString,
			String replacementString, final ImageDescriptor image, final int priority, final IHelpResolver helpResolver) {

		if (context.getFilter() != null) {
			// eventually filter proposal
			if (!replacementString.startsWith(context.getFilter()))
				// filter proposal
				return;

			if (replacementString.equals(context.getFilter()))
				// proposal equals existing text, do not display anymore
				return;

			// do not filter, but adapt replacementString
			replacementString = replacementString.substring(context.getFilter().length());
		}

		proposals.add(new ScriptCompletionProposal(displayString, replacementString, context.getOffset(), image, priority, helpResolver));
	}

	@Override
	public boolean isActive(final ICompletionContext context) {
		return context.getType() != Type.UNKNOWN;
	}
}
