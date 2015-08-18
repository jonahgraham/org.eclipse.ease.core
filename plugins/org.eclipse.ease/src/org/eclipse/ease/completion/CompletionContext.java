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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.completion.ICompletionSource.SourceType;

/**
 * Simple data-storage implementation of {@link ICompletionContext}.
 * 
 * @author Martin Kloesch
 *
 */
public class CompletionContext implements ICompletionContext {
	private final String fInput;
	private final String fFilter;
	private List<ICompletionSource> fSourceStack;

	/**
	 * Constructor only stores parameters to members.
	 * 
	 * @param input
	 *            the original input as String.
	 * @param filter
	 *            the previously parsed filter.
	 * @param sourceStack
	 *            The calculated (but maybe not refined) source stack.
	 */
	public CompletionContext(final String input, final String filter, List<ICompletionSource> sourceStack) {
		fInput = input;
		fFilter = filter;
		fSourceStack = sourceStack;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.completion.ICompletionContext#getInput()
	 */
	@Override
	public String getInput() {
		return fInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.completion.ICompletionContext#getFilter()
	 */
	@Override
	public String getFilter() {
		return fFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.completion.ICompletionContext#getSourceStack()
	 */
	@Override
	public List<ICompletionSource> getSourceStack() {
		return fSourceStack;
	}

	/**
	 * Additional setter method for source stack.
	 * 
	 * @param sourceStack
	 *            the desired source stack to be set.
	 */
	public void setSourceStack(List<ICompletionSource> sourceStack) {
		fSourceStack = sourceStack;
	}

	/**
	 * Follows the given source stack and gathers necessary information about classes of single items in chain.
	 * 
	 * Does NOT update information in place but returns a modified copy of the original.
	 * 
	 * @param sourceStack
	 *            The source stack that needs to be refined.
	 * @return the new refined source stack if successful, <code>null</code> in case of error.
	 */
	public static List<ICompletionSource> refineSourceStack(List<ICompletionSource> sourceStack) {
		// Null means syntax error
		if (sourceStack == null) {
			return null;
		}

		// Empty source stack is well defined.
		if (sourceStack.size() == 0) {
			return sourceStack;
		}

		// Special care has to be taken about first element
		ICompletionSource root = sourceStack.get(0);
		Class<?> classOfInterest = null;

		// Get necessary information from first element in source stack
		switch (root.getSourceType()) {

		// For constructors we only need the class
		case JAVA_CONSTRUCTOR:
		case MODULE_CONSTRUCTOR:
		case LOCAL_CONSTRUCTOR:
			classOfInterest = root.getClazz();
			break;

		// For methods and functions we need the return type
		case CLASS_METHOD:
		case MODULE_METHOD:
		case LOCAL_FUNCTION:
			if (root.getObject() != null && root.getObject() instanceof Method) {
				classOfInterest = ((Method) root.getObject()).getReturnType();
			} else {
				return null;
			}
			break;

		// For fields, variables and constants we simply need the type.
		case CLASS_FIELD:
		case MODULE_FIELD:
		case LOCAL_VARIABLE:
			classOfInterest = root.getClazz();
			break;

		case STRING_LITERAL:
			// Source stack with String literal is only valid if the string is the only item in chain.
			if (sourceStack.size() == 1) {
				return sourceStack;
			} else {
				return null;
			}

		case JAVA_PACKAGE:
			// Java package cannot be inspected correctly (yet)
			return null;

		case KEYWORD:
			// Keyword cannot occur here
			return null;

		case METHOD:
		case INSTANCE:
		case CONSTRUCTOR:
		default:
			// Root not refined enough
			return null;
		}

		// Do not change call chain in-place
		List<ICompletionSource> refinedStack = new ArrayList<ICompletionSource>();
		refinedStack.add(sourceStack.get(0));

		// Iterate over the rest of the of the call chain up to last element
		for (int i = 1; i < sourceStack.size(); i++) {
			ICompletionSource refinedSource = getNext(classOfInterest, sourceStack.get(i));
			if (refinedSource != null) {
				switch (refinedSource.getSourceType()) {
				case CLASS_METHOD:
					if (refinedSource.getObject() != null) {
						classOfInterest = ((Method) refinedSource.getObject()).getReturnType();
					} else {
						return null;
					}
					break;
				case CLASS_FIELD:
					classOfInterest = refinedSource.getClazz();
					break;
				default:
					return null;
				}
				refinedStack.add(refinedSource);
			} else {
				return null;
			}
		}

		return refinedStack;
	}

	/**
	 * Refines the given {@link ICompletionSource} looking up all necessary information in given class object.
	 * 
	 * This will only work for class methods and fields, but since this method is only called with items that are not in the beginning of a chain this works
	 * fine.
	 * 
	 * @param hayStack
	 *            The class the given field or method should be part of.
	 * @param needle
	 *            All necessary information about name and type of the desired field or method.
	 * @return new {@link ICompletionSource} with parsed information if successful. <code>null</code> if no information could be found.
	 */
	private static ICompletionSource getNext(Class<?> hayStack, ICompletionSource needle) {
		// Avoid NPE
		if (hayStack == null || needle == null) {
			return null;
		}

		switch (needle.getSourceType()) {
		case METHOD:
		case CLASS_METHOD:
			for (Method method : hayStack.getMethods()) {
				if (method.getName().equals(needle.getName())) {
					return new CompletionSource(SourceType.CLASS_METHOD, needle.getName(), hayStack, method, null);
				}
			}
			break;
		case CLASS_FIELD:
			for (Field field : hayStack.getFields()) {
				if (field.getName().equals(needle.getName())) {
					return new CompletionSource(SourceType.CLASS_FIELD, needle.getName(), hayStack, field, null);
				}
			}
		default:
			return null;
		}

		return null;
	}
}