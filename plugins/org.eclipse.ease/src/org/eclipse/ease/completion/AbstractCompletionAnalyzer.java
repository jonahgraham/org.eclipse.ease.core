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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.completion.ICompletionSource.SourceType;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Abstract base implementation for {@link ICompletionAnalyzer}.
 *
 * Handles common logic and offers abstract methods for script languages to modify analysis based on their syntax.
 *
 * @author Martin Kloesch
 *
 */
public abstract class AbstractCompletionAnalyzer implements ICodeParser {
	/**
	 * Trims everything from the given input String up to the last occurrence of given delimiter.
	 *
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring after last occurrence of delimiter.
	 */
	protected static String ltrim(final String input, final String delimiter) {
		if (input.contains(delimiter)) {
			int splitPoint = input.lastIndexOf(delimiter) + delimiter.length();
			if (splitPoint < input.length()) {
				return input.substring(splitPoint);
			}
		}
		return input;
	}

	/**
	 * Trims everything from the given input String after the first occurrence of given delimiter.
	 *
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring up to first occurrence of delimiter.
	 */
	protected static String rtrim(final String input, final String delimiter) {
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
	protected static String getCurrentParentheses(final String code) {
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
	protected static String removeParameters(final String code) {
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

	/**
	 * Returns a regular expression to match for includes.
	 *
	 * This pattern must have a single group containing the code to be imported.
	 *
	 * @return Pattern to help find includes.
	 */
	protected abstract Pattern getIncludePattern();

	/**
	 * Splits the given piece of code into a call chain (strings only).
	 *
	 * @param code
	 *            Code to be split into chain
	 * @return call chain for given piece of code as list of strings.
	 */
	protected List<String> splitChain(final String code) {
		return Arrays.asList(code.split("\\."));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.completion.ICompletionAnalyzer#getContext(java.lang.String, int)
	 */
	@Override
	public ICompletionContext getContext(final String contents, final int position) {
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

		// Remove leading / trailing whitespace
		parsedCode = parsedCode.trim();

		// In case code ends with '.' add a space to match against everything.
		if (parsedCode.endsWith(".")) {
			parsedCode += " ";
		}

		// Create call stack
		List<ICompletionSource> callStack = new ArrayList<ICompletionSource>();
		List<String> splitCode = splitChain(parsedCode);

		if (splitCode.size() == 1) {
			return new CompletionContext(code, splitCode.get(0).trim(), callStack);
		}

		// Parse all elements in call stack to ICompletionSource
		ICompletionSource asSource = toCompletionSource(splitCode.get(0));
		if (asSource == null) {
			return null;
		} else {
			callStack.add(asSource);
		}

		// Iterate over whole call stack (last element is filter)
		for (int i = 1; i < (splitCode.size() - 1); i++) {
			String current = splitCode.get(i);
			asSource = toCompletionSource(current);
			if ((asSource == null) || asSource.getSourceType().equals(SourceType.CONSTRUCTOR)) {
				return null;
			} else {
				callStack.add(asSource);
			}
		}

		return new CompletionContext(code, splitCode.get(splitCode.size() - 1).trim(), callStack);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.completion.ICompletionAnalyzer#getIncludedCode(java.lang.String)
	 */
	@Override
	public String getIncludedCode(String input, final Object parent) {
		// Get regular expression to find includes
		Pattern includePattern = getIncludePattern();
		if (includePattern != null) {
			Set<String> includedFiles = new HashSet<String>();

			// Code will be called as long as there are includes left
			boolean updated = true;
			while (updated) {
				updated = false;

				// Get all matches in the code
				Matcher matcher = includePattern.matcher(input);
				while (matcher.find()) {
					// get file to be included
					String includePath = matcher.group(1);

					// Get file path (might be eclipse specific URL)
					Object included = ResourceTools.resolveFile(includePath, parent, true);

					// Check if file found
					if (included != null) {
						String absPath = null;
						InputStream is = null;

						// Parse information from included File or IFile
						if (included instanceof IFile) {
							IFile includedFile = (IFile) included;
							absPath = includedFile.getFullPath().toOSString();
							try {
								is = includedFile.getContents();
							} catch (CoreException e) {
								// 404 ignore
								continue;
							}
						} else if (included instanceof File) {
							File includedFile = (File) included;
							absPath = includedFile.getAbsolutePath();
							try {
								is = new FileInputStream(includedFile);
							} catch (FileNotFoundException e) {
								// 404 ignore
								continue;
							}
						} else {
							// Neither File nor IFile, ignore
							continue;
						}

						// Check if file already included
						if (!includedFiles.contains(absPath)) {
							updated = true;
							includedFiles.add(absPath);

							// Get the actual content of the file
							String includedContent;
							try {
								includedContent = ResourceTools.toString(is);
							} catch (IOException e) {
								// Ignore if content could not be read
								continue;
							}
							if (includedContent != null) {
								StringBuilder sb = new StringBuilder();

								// Create new input code:
								// o Part before the include statement
								// o Included code instead of include statement
								// o Part after the include statement
								sb.append(input.substring(0, matcher.start()));
								sb.append(includedContent);
								sb.append(input.substring(matcher.end()));
								input = sb.toString();
							}
						}
					}
				}
			}
		}
		return input;
	}

	@Override
	public Map<String, String> parse(final InputStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createHeader(final Map<String, String> headerContent) {
		// TODO Auto-generated method stub
		return null;
	}
}
