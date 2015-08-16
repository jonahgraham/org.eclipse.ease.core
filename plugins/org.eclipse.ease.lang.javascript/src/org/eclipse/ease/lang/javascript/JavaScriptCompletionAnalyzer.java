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
	 * List of delimiters that can be ignored for completion.
	 * 
	 * Used by {@link #parseCallChain(String)} to only get necessary information about items in call chain.
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
			if ((code.lastIndexOf("new ") + 3) == code.lastIndexOf(" ")) {
				return ltrim(code, "new ");
			}
		}
		return ltrim(code, " ");
	}
	
	/* (non-Javadoc)
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
	
	private static final String CTOR_PATTERN = "^new [a-zA-Z][a-zA-Z0-9_]*\\(\\)$";
	private static final String FUNCTION_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*\\(\\)$";
	private static final String INSTANCE_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";

	/* (non-Javadoc)
	 * @see org.eclipse.ease.modules.AbstractCompletionAnalyzer#toCompletionSource(java.lang.String)
	 */
	@Override
	protected ICompletionSource toCompletionSource(String code) {
		ICompletionSource src = null;
		if (code.matches(CTOR_PATTERN)) {
			src = new CompletionSource(SourceType.CONSTRUCTOR, code.replace("new ", "").replace("()", ""), null, null);
		} else if (code.matches(FUNCTION_PATTERN)) {
			src = new CompletionSource(SourceType.METHOD, code.replace("()", ""), null, null);
		} else if (code.matches(INSTANCE_PATTERN)) {
			src = new CompletionSource(SourceType.INSTANCE, code, null, null);
		}
		return src;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ease.completion.AbstractCompletionAnalyzer#getIncludePattern()
	 */
	@Override
	protected Pattern getIncludePattern() {
		return INCLUDE_PATTERN;
	}
}
