/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ease.completion.AbstractCompletionAnalyzer;
import org.eclipse.ease.completion.CompletionSource;
import org.eclipse.ease.completion.ICompletionSource;
import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * @author Martin Kloesch
 *
 */
public class JavaScriptCompletionAnalyzer extends AbstractCompletionAnalyzer {

	/**
	 * List of delimiters that indicate code that can be ignored for completion.
	 */
	private static List<String> CODE_DELIMITERS = new ArrayList<String>(Arrays.asList("=", "+", "-", "*", "/", "|", "&", "^", ",", "{", "}", "[", "]", ";"));

	/**
	 * Regex to parse includes from piece of code.
	 */
	private static final Pattern INCLUDE_PATTERN = Pattern.compile("include\\((?:'|\")(.*)(?:'|\")\\) *;?");

	/**
	 * Special case of {@link #removeUnnecessaryCode(String)}.
	 * 
	 * Code before spaces is not necessary for evaluation except when it contains the <code>new</code> keyword.
	 * 
	 * @param code
	 *            Piece of code to be "trimmed".
	 * @return Code with everything not necessary for code completion removed.
	 */
	private static String removeSpacesExceptCtor(String code) {
		if (code.contains("new ")) {
			// Check if constructor is last occurence of space
			if ((code.lastIndexOf("new ") + 3) == code.lastIndexOf(" ")) {
				return code.substring(code.lastIndexOf("new "));
			}
		}
		return ltrim(code, " ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.AbstractCompletionAnalyzer#removeUnnecessaryCode(java.lang.String)
	 */
	@Override
	protected String removeUnnecessaryCode(String code) {
		// Remove everything up to all code delimiters
		for (String delimiter : CODE_DELIMITERS) {
			code = ltrim(code, delimiter);
		}

		return removeSpacesExceptCtor(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.completion.AbstractCompletionAnalyzer#splitChain(java.lang.String)
	 */
	protected List<String> splitChain(String code) {
		List<String> split = new ArrayList<String>();

		// Check if code starts with constructor
		if (code.matches(CTOR_START_PATTERN)) {
			int splitPoint = code.indexOf(')') + 1;

			// Check boundaries
			if ((splitPoint + 1) < code.length()) {
				split.add(code.substring(0, splitPoint));
				code = code.substring(splitPoint + 1);
			}
		}

		// Add all other items (split on . is enough)
		for (String piece : code.split("\\.")) {
			split.add(piece);
		}

		return split;
	}

	/**
	 * Base regex for valid identifier. Reused for all other patterns.
	 * 
	 * Valid identifier must start with alphabetical character or underscore. Afterwards all alpha-numerical characters as well as underscore are allowed.
	 */
	private static final String IDENTIFIER_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]*";

	/**
	 * Base regex for constructors. Reused for all other constructor patterns.
	 */
	private static final String CTOR_BASE = "new " + IDENTIFIER_PATTERN + "(?:\\." + IDENTIFIER_PATTERN + ")*\\(\\)";

	/**
	 * Regex to match constructor at the beginning of a string.
	 */
	private static final String CTOR_START_PATTERN = "^" + CTOR_BASE + ".*$";

	/**
	 * Regex to check if string contains only constructor code.
	 */
	private static final String CTOR_PATTERN = "^" + CTOR_BASE + "$";

	/**
	 * Regex to check if string only contains function call code.
	 */
	private static final String FUNCTION_PATTERN = "^" + IDENTIFIER_PATTERN + "\\(\\)$";

	/**
	 * Regex to check if string only contains valid identifier.
	 */
	private static final String INSTANCE_PATTERN = "^" + IDENTIFIER_PATTERN + "$";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.AbstractCompletionAnalyzer#toCompletionSource(java.lang.String)
	 */
	@Override
	protected ICompletionSource toCompletionSource(String code) {
		ICompletionSource src = null;
		if (code.matches(CTOR_PATTERN)) {
			src = new CompletionSource(SourceType.CONSTRUCTOR, code.replace("new ", "").replace("()", ""), null, null, null);
		} else if (code.matches(FUNCTION_PATTERN)) {
			src = new CompletionSource(SourceType.METHOD, code.replace("()", ""), null, null, null);
		} else if (code.matches(INSTANCE_PATTERN)) {
			src = new CompletionSource(SourceType.INSTANCE, code, null, null, null);
		}
		return src;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.completion.AbstractCompletionAnalyzer#getIncludePattern()
	 */
	@Override
	protected Pattern getIncludePattern() {
		return INCLUDE_PATTERN;
	}
}
