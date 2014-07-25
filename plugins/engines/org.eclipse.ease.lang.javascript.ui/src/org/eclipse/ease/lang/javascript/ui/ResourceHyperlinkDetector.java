/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.lang.javascript.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ResourceHyperlinkDetector extends AbstractHyperlinkDetector {

	private static final Pattern STRING_LITERAL = Pattern.compile("(?:\"(.*?)\")|(?:'(.*?)')");

	@Override
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, final boolean canShowMultipleHyperlinks) {
		IDocument document = textViewer.getDocument();

		IRegion lineInformation;
		String line;
		try {
			lineInformation = document.getLineInformationOfOffset(region.getOffset());
			line = document.get(lineInformation.getOffset(), lineInformation.getLength());
		} catch (BadLocationException e) {
			return null;
		}

		int offsetInLine = region.getOffset() - lineInformation.getOffset();
		Matcher matcher = STRING_LITERAL.matcher(line);

		while (matcher.find()) {
			// literal detected, see if it is under the cursor
			if ((offsetInLine >= matcher.start()) && (offsetInLine < matcher.end())) {
				// extract filename
				String filename = matcher.group((matcher.group(1) != null) ? 1 : 2);

				// try to locate parent file
				IFile parent = null;
				IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
				if (input instanceof FileEditorInput)
					parent = ((FileEditorInput) input).getFile();

				// resolve target file location
				Object file = ResourceTools.resolveFile(filename, parent);
				if (file instanceof IFile) {
					if (((IFile) file).exists()) {
						Region linkRegion = new Region(lineInformation.getOffset() + matcher.start() + 1, filename.length());
						return new IHyperlink[] { new ResourceHyperlink((IFile) file, linkRegion) };
					}
				}
			}
		}

		return null;
	}
}
