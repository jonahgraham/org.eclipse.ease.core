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
 * Simple readonly data-storage implementation of {@link ICompletionSource}.
 * 
 * @author Martin Kloesch
 *
 */
public class CompletionSource implements ICompletionSource {
	private final SourceType fSourceType;
	private final String fName;
	private final Class<?> fClass;
	private final Object fObject;

	/**
	 * Constructor only stores parameters to members.
	 * 
	 * @param sourceType
	 *            source type of completion chain item.
	 * @param name
	 *            name of completion chain item.
	 * @param clazz
	 *            parent class of completion chain item.
	 * @param object
	 *            the actual object in completion chain.
	 */
	public CompletionSource(final SourceType sourceType, final String name, final Class<?> clazz, final Object object) {
		fSourceType = sourceType;
		fName = name;
		fClass = clazz;
		fObject = object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.ICompletionSource#getSourceType()
	 */
	@Override
	public SourceType getSourceType() {
		return fSourceType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.ICompletionSource#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.ICompletionSource#getClazz()
	 */
	@Override
	public Class<?> getClazz() {
		return fClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.modules.ICompletionSource#getObject()
	 */
	@Override
	public Object getObject() {
		return fObject;
	}

}