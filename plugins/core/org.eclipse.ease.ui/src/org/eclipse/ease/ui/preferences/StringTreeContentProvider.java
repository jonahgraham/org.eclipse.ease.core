/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.preferences;

import java.util.Collection;

public class StringTreeContentProvider extends ManualTreeContentProvider {

	private final String mDelimiter;

	public StringTreeContentProvider() {
		super();

		mDelimiter = "/";
	}

	public StringTreeContentProvider(String delimiter) {
		super();

		mDelimiter = delimiter;
	}

	@Override
	public void addElement(Object element, Object parent) {
		if ((element instanceof String) && (parent instanceof String)) {
			addElement(element + "/" + parent, null);

		} else if (element instanceof String) {
			addElement((String) element, parent);

		} else if (parent instanceof String) {
			Object newNode = addElement((String) parent, null);
			getChildCollection(newNode).add(element);

		} else
			super.addElement(element, parent);
	}

	private Object addElement(String element, Object parent) {
		// create parent scope
		String[] tokens = element.split(mDelimiter);
		Collection<Object> elements = getChildCollection(parent);

		for (String token : tokens) {
			if (!token.isEmpty()) {
				Object existingElement = (elements != null) ? findElement(token, elements) : null;
				if (existingElement == null) {
					existingElement = new StringBuilder(token);
					super.addElement(existingElement, parent);
				}

				elements = getChildCollection(existingElement);
				parent = existingElement;
			}
		}

		return parent;
	}

	private Object findElement(String needle, Collection<Object> elements) {
		for (Object candidate : elements) {
			if (candidate.toString().equals(needle))
				return candidate;
		}

		return null;
	}

	public String getAbsolutePath(Object element) {
		StringBuilder path = new StringBuilder();

		Object parent;
		do {
			path.insert(0, element.toString());
			path.insert(0, "/");
			parent = getParent(element);
		} while (parent != null);

		return path.toString();
	}
}
