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
package org.eclipse.ease;

import java.io.InputStream;

import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;

/**
 * Parser interface for source code parsers to extract relevant script data.
 */
public interface ICodeParser {

	/**
	 * Parses the file for a comment section at the beginning.
	 * 
	 * @param stream
	 *            code content stream
	 * @return comment data without decoration characters (eg: '*' at beginning of each line)
	 */
	String getHeaderComment(final InputStream stream);

	/**
	 * Verify if a line of code is accepted before the header comment section. This allows special magic tokens to be placed before the header comment as some script languages depend on that.
	 * 
	 * @param line line of code
	 * @return <code>true</code> when line is accepted before the comment header
	 */
	boolean isAcceptedBeforeHeader(final String line);
	
	/**
	 * Parse the given piece of code into a language specific {@link ICompletionContext}.
	 *
	 * @param scriptEngine
	 *            running script engine
	 * @param resource
	 *            resource instance to be parsed
	 * @param contents
	 *            code to be parsed (only up to cursor position)
	 * @param position
	 *            cursor position within contents
	 * @param selectionRange
	 *            amount of selected characters from cursor position
	 * @return {@link ICompletionContext} with parsed information if successful, <code>null</code> in case invalid syntax given.
	 */
	ICompletionContext getContext(IScriptEngine scriptEngine, Object resource, String contents, int position, int selectionRange);

	/**
	 * Gets signature, certificates, provider and message-digest algorithm of signature, and content excluding signature block.
	 *
	 * @param stream
	 *            provide {@link InputStream} to get signature from
	 * @return {@link SignatureInfo} instance containing signature, certificates, provider and message-digest algorithm, and content excluding signature block
	 *         or <code>null</code> if signature is not found or is not in proper format
	 * @throws ScriptSignatureException
	 *             when there is text after signature block or error occurs while reading from provided input stream
	 */
	SignatureInfo getSignatureInfo(InputStream stream) throws ScriptSignatureException;
}
