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

/**
 * Interface to create basis for language specific completion context.
 * 
 * Splits a given line of code into an {@link ICompletionContext} with each element in the call stack parsed to correct base type.
 * 
 * This base context can then be refined by ICompletionProviders.
 * 
 * @author Martin Kloesch
 *
 */
public interface ICompletionAnalyzer {
	/**
	 * Parse the given input to add all included code. The return is a String with all the code in order. Included code is put instead of the include line.
	 * 
	 * @param input
	 *            Code to be parsed
	 * @param parent
	 *            absolute path to parent directory.
	 * @return All code including imported code.
	 */
	String getIncludedCode(String input, String parent);

	/**
	 * Parse the given piece of code into a language specific {@link ICompletionContext}.
	 * 
	 * @param contents
	 *            Code to be parsed.
	 * @param position
	 *            End position for code to be parsed (for shell i.e. current cursor position).
	 * @return {@link ICompletionContext} with parsed information if successful, <code>null</code> in case invalid syntax given.
	 */
	ICompletionContext getContext(String contents, int position);
}