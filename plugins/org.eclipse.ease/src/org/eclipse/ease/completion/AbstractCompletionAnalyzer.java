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

package org.eclipse.ease.completion;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * Abstract base implementation for {@link ICompletionAnalyzer}.
 * 
 * Handles common logic and offers abstract methods for script languages to modify analysis based on their syntax.
 * 
 * @author Martin Kloesch
 *
 */
public abstract class AbstractCompletionAnalyzer implements ICompletionAnalyzer {
	/**
	 * Trims everything from the given input String up to the last occurrence of given delimiter.
	 * 
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring after last occurrence of delimiter.
	 */
	protected static String ltrim(String input, String delimiter) {
		if (input.contains(delimiter)) {
			return input.substring(input.lastIndexOf(delimiter) + 1);
		}
		return input;
	}

	/**
	 * Trims everything from the given input String up to the first occurrence of given delimiter.
	 * 
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring up to first occurrence of delimiter.
	 */
	protected static String rtrim(String input, String delimiter) {
		if (input.contains(delimiter)) {
			return input.substring(0, input.indexOf(delimiter));
		}
		return input;
	}

	/**
	 * Analyzes a given line of code and removes everything that is not part of the current parentheses' scope. Further performs check if still in opened String
	 * literal -> do not use auto-completion.
	 * 
	 * @param code
	 *            Line of code to be analyzed.
	 * @return String with everything in current parentheses' scope, <code>null</code> if String contains invalid syntax or String literal opened.
	 */
	protected static String getCurrentParentheses(String code) {
		Stack<Integer> parantheses = new Stack<Integer>();
		boolean inString = false;
		boolean stringEscape = false;
		for (int i = 0; i < code.length(); i++) {
			switch (code.charAt(i)) {
			case '(':
				if (!inString) {
					parantheses.push(i);
				}
				stringEscape = false;
				break;
			case ')':
				if (!inString) {
					try {
						parantheses.pop();
					} catch (EmptyStackException e) {
						// Invalid syntax, no completion possible
						return null;
					}
				}
				stringEscape = false;
				break;
			case '"':
				if (!stringEscape) {
					inString = !inString;
				}
				stringEscape = false;
				break;
			case '\\':
				if (inString) {
					stringEscape = !stringEscape;
				}
				break;
			default:
				stringEscape = false;
				break;
			}
		}

		// Check if still in string
		if (inString) {
			return null;
		}

		if (parantheses.isEmpty()) {
			return code;
		} else {
			return code.substring(parantheses.pop() + 1);
		}
	}

	/**
	 * Removes parameters in method calls because they are not necessary for completion. Java overloads must all have same return type so parameters can be
	 * ignored.
	 * 
	 * @param code
	 *            Piece of code to remove call parameters from.
	 * @return code with parameters removed from calls.
	 */
	protected static String removeParameters(String code) {
		StringBuilder sb = new StringBuilder();

		int level = 0;
		boolean inString = false;
		boolean stringEscape = false;

		for (int i = 0; i < code.length(); i++) {
			switch (code.charAt(i)) {
			case '(':
				if (!inString) {
					if (level == 0) {
						sb.append('(');
					}
					level++;
				}
				stringEscape = false;
				break;
			case ')':
				if (!inString) {
					if (level == 1) {
						sb.append(')');
					}
					level--;
				}
				stringEscape = false;
				break;
			case '\\':
				if (inString) {
					stringEscape = !stringEscape;
				}
				break;
			case '"':
				if (!stringEscape) {
					inString = !inString;
				}
			default:
				if (level == 0) {
					sb.append(code.charAt(i));
				}
				stringEscape = false;
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * Removes all part of a given piece of code that is not necessary for completion.
	 * 
	 * Different scripting languages may have different code to keep/remove.
	 * 
	 * @param code
	 *            Piece of code to remove everything not necessary from.
	 * @return Code with all unnecessary characters removed.
	 */
	protected abstract String removeUnnecessaryCode(String code);

	/**
	 * Parses a given piece of code to one of the base values of {@link ICompletionSource}.
	 * 
	 * These base values include constructors, methods and fields.
	 * 
	 * @param code
	 *            piece of code to be parsed to {@link ICompletionSource}
	 * @return {@link ICompletionSource} matching given piece of code if successful, <code>null</code> in case of error.
	 */
	protected abstract ICompletionSource toCompletionSource(String code);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.ICompletionAnalyzer#getContext(java.lang.String, int)
	 */
	@Override
	public ICompletionContext getContext(String contents, int position) {
		// Avoid NPE, should not occur but better safe than sorry
		if (contents == null) {
			return null;
		}
		String code = contents.substring(0, position);

		// Remove everything that is not part of the current parentheses
		String parsedCode = getCurrentParentheses(code);
		if (parsedCode == null) {
			return null;
		}

		// Remove parameters in calls (not used)
		parsedCode = removeParameters(parsedCode);

		// Remove everything that can be ignored during evaluation
		parsedCode = removeUnnecessaryCode(parsedCode);

		// In case code ends with '.' add a space to match against everything.
		if (parsedCode.endsWith(".")) {
			parsedCode += " ";
		}

		// Create call stack
		List<ICompletionSource> callStack = new ArrayList<ICompletionSource>();
		String[] splitCode = parsedCode.split("\\.");

		if (splitCode.length == 1) {
			return new CompletionContext(code, splitCode[0], callStack);
		}

		// Parse all elements in call stack to ICompletionSource
		ICompletionSource asSource = toCompletionSource(splitCode[0]);
		if (asSource == null) {
			return null;
		} else {
			callStack.add(asSource);
		}

		// Iterate over whole call stack (last element is filter)
		for (int i = 1; i < splitCode.length - 1; i++) {
			String current = splitCode[i];
			asSource = toCompletionSource(current);
			if (asSource == null || asSource.getSourceType().equals(SourceType.CONSTRUCTOR)) {
				return null;
			} else {
				callStack.add(asSource);
			}
		}

		return new CompletionContext(code, splitCode[splitCode.length - 1].trim(), callStack);
	}

}
