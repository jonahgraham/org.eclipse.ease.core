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

import java.util.List;

/**
 * Interface for completion context.
 * 
 * This context helps ICompletionProvider to simplify completion proposal calculation.
 * 
 * Stores information about: o given input. o filter for part of interest. o Source stack for part of interest.
 * 
 * @author Martin Kloesch
 *
 */
public interface ICompletionContext {
	/**
	 * Returns the initial user input that has been parsed to {@link ICompletionContext}.
	 * 
	 * @return Original user input.
	 */
	String getInput();

/**
	 * Returns the filter (part of the code that needs completion.)
	 * 
	 * In combination with {@link #getSourceStack() this can simplify filtering.
	 * 
	 * @return Part of code that needs completion.
	 */
	String getFilter();

	/**
	 * Returns a list of all elements in the source stack.
	 * 
	 * The source stack is a list of chained {@link ICompletionSource} objects. The source stack contains the actual "call chain" of the relevant field.
	 * 
	 * Example: new String().substring(0).fo -> [JAVA_CLASS "String", JAVA_METHOD "substring"]
	 *
	 * @return source stack as list.
	 */
	List<ICompletionSource> getSourceStack();
}