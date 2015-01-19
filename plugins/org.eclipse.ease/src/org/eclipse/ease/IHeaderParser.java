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

/**
 * Parser interface for source code parsers to extract script keywords. The parser should fetch the very first comment in a source stream and extract key:value
 * pairs. Where keys are single words followed by a ':'. The rest of the line should be treated as value.
 */
public interface IHeaderParser {

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
}
