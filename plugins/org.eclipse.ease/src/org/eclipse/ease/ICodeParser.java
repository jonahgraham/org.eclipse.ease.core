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
import java.util.Map;

import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureInfo;

/**
 * Parser interface for source code parsers to extract script keywords. The parser should fetch the very first comment in a source stream and extract key:value
 * pairs. Where keys are single words followed by a ':'. The rest of the line should be treated as value.
 */
public interface ICodeParser {

	/**
	 * Parse stream for key:value pairs.
	 *
	 * @param stream
	 *            stream to parse
	 * @return map containing keywords or empty map
	 */
	Map<String, String> parse(InputStream stream);

	/**
	 * Create a script header for given keywords.
	 *
	 * @param headerContent
	 *            key:value pairs to be stored
	 * @return String representation of header
	 */
	String createHeader(Map<String, String> headerContent);

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
