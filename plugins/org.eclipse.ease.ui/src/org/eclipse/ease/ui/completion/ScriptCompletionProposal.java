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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.help.hovers.IHelpResolver;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class ScriptCompletionProposal
		implements ICompletionProposal, ICompletionProposalExtension5, ICompletionProposalExtension6, IContentProposal, Comparable<ScriptCompletionProposal> {

	public static final int ORDER_FIELD = 20;
	public static final int ORDER_METHOD = 40;
	public static final int ORDER_PACKAGE = 60;
	public static final int ORDER_CLASS = 80;
	public static final int ORDER_DEFAULT = 100;

	private final String fDisplayString;
	private final String fReplacementString;
	private final ImageDescriptor fImageDescriptor;
	private StyledString fStyledString;
	private final int fSortOrder;

	private final IHelpResolver fHelpResolver;
	private final ICompletionContext fContext;

	public ScriptCompletionProposal(final ICompletionContext context, final String displayString, final String replacementString,
			final ImageDescriptor imageDescriptor, final int sortOrder, final IHelpResolver helpResolver) {
		fContext = context;
		fDisplayString = displayString;
		fReplacementString = replacementString;
		fImageDescriptor = imageDescriptor;
		fSortOrder = sortOrder;
		fHelpResolver = helpResolver;
	}

	public ScriptCompletionProposal(final ICompletionContext context, final StyledString styledString, final String replacementString,
			final ImageDescriptor imageDescriptor, final int sortOrder, final IHelpResolver helpResolver) {
		this(context, styledString.getString(), replacementString, imageDescriptor, sortOrder, helpResolver);
		fStyledString = styledString;
	}

	@Override
	public int compareTo(final ScriptCompletionProposal o) {
		final int priority = fSortOrder - o.fSortOrder;
		if (priority != 0)
			return priority;

		return getDisplayString().compareToIgnoreCase(o.getDisplayString());
	}
	// ----------------------------------------------------------------------
	// ICompletionProposal interface implementation (for editor replacements)
	// ----------------------------------------------------------------------

	@Override
	public String getDisplayString() {
		return fDisplayString;
	}

	@Override
	public Image getImage() {
		return (fImageDescriptor != null) ? fImageDescriptor.createImage() : null;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return (fStyledString != null) ? fStyledString : new StyledString(getDisplayString());
	}

	@Override
	public void apply(final IDocument document) {
		try {
			if (fContext.getFilter() != null)
				document.replace(fContext.getOffset() - fContext.getFilter().length(), fContext.getFilter().length(), fReplacementString);

			else
				document.replace(fContext.getOffset(), 0, fReplacementString);

		} catch (final BadLocationException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not insert completion proposal into document", e);
		}
	}

	@Override
	public Point getSelection(final IDocument document) {
		if (fContext.getFilter() != null)
			return new Point((fContext.getOffset() - fContext.getFilter().length()) + fReplacementString.length(), 0);

		else
			return new Point(fContext.getOffset() + fReplacementString.length(), 0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		if (fHelpResolver != null)
			return fHelpResolver.resolveHTMLHelp();

		return null;
	}

	@Override
	public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
		return getAdditionalProposalInfo();
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	// ------------------------------------------------------------------
	// IContentProposal interface implementation (for shell replacements)
	// ------------------------------------------------------------------
	@Override
	public String getContent() {
		String original = fContext.getOriginalCode();
		return original.substring(0, original.length() - fContext.getFilter().length()) + fReplacementString;
	}

	@Override
	public int getCursorPosition() {
		return getContent().length();
	}

	@Override
	public String getLabel() {
		return getDisplayString() + "x";
	}

	@Override
	public String getDescription() {
		return getAdditionalProposalInfo();
	}
}
