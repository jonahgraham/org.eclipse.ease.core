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

import org.eclipse.ease.Logger;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class ScriptCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6, IContentProposal {

	public static final int ORDER_FIELD = 20;
	public static final int ORDER_METHOD = 40;
	public static final int ORDER_PACKAGE = 60;
	public static final int ORDER_DEFAULT = 100;

	private final String fDisplayString;
	private final String fReplacementString;
	private int fCursorPosition;
	private final ImageDescriptor fImageDescriptor;
	private StyledString fStyledString;
	private final int fSortOrder;

	public ScriptCompletionProposal(String displayString, String replacementString, int cursorPosition, ImageDescriptor imageDescriptor, int sortOrder) {
		fDisplayString = displayString;
		fReplacementString = replacementString;
		fCursorPosition = cursorPosition;
		fImageDescriptor = imageDescriptor;
		fSortOrder = sortOrder;
	}

	public ScriptCompletionProposal(String replacementString, int cursorPosition) {
		this(replacementString, null, cursorPosition, null, ORDER_DEFAULT);
	}

	public ScriptCompletionProposal(StyledString styledString, String replacementString, int cursorPosition, ImageDescriptor imageDescriptor, int sortOrder) {
		this(styledString.getString(), replacementString, cursorPosition, imageDescriptor, sortOrder);
		fStyledString = styledString;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return (fStyledString != null) ? fStyledString : new StyledString(getDisplayString());
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fCursorPosition, 0, fReplacementString);
		} catch (final BadLocationException e) {
			Logger.logError("Could not insert completion proposal into document", e);
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fCursorPosition + fReplacementString.length(), 0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return fDisplayString;
	}

	public String getReplacementString() {
		return fReplacementString;
	}

	@Override
	public Image getImage() {
		return (fImageDescriptor != null) ? fImageDescriptor.createImage() : null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	// -----------------------------------------
	// IContentProposal interface implementation
	// -----------------------------------------
	@Override
	public String getContent() {
		return getReplacementString();
	}

	@Override
	public int getCursorPosition() {
		return getSelection(null).x;
	}

	@Override
	public String getLabel() {
		return getDisplayString();
	}

	@Override
	public String getDescription() {
		return getAdditionalProposalInfo();
	}

	public void setCursorPosition(int cursorPosition) {
		fCursorPosition = cursorPosition;
	}

	public int getSortOrder() {
		return fSortOrder;
	}
}
