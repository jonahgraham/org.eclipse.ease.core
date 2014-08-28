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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ManualTreeContentProvider implements ITreeContentProvider {

	private final Set<Object> mRootElements = new HashSet<Object>();
	private final Map<Object, List<Object>> mRelations = new HashMap<Object, List<Object>>();
	private final Map<Object, Object> mReverseRelations = new HashMap<Object, Object>();

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		mRootElements.clear();
		mRelations.clear();
		mReverseRelations.clear();

		if (newInput instanceof Object[]) {
			for (Object element : (Object[]) newInput)
				addElement(element, null);

		} else if (newInput instanceof Collection<?>) {
			for (Object element : (Collection<?>) newInput)
				addElement(element, null);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return mRootElements.toArray(new Object[mRootElements.size()]);
	}

	public void addElement(Object element, Object parent) {
		if (parent == null) {
			// add root element
			mRootElements.add(element);
			// no reverse lookup needed for root elements

		} else {
			// add child element
			if (!mRelations.containsKey(parent))
				mRelations.put(parent, new ArrayList<Object>());

			mRelations.get(parent).add(element);

			// register reverse relation
			mReverseRelations.put(element, parent);
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		List<Object> children = mRelations.get(parentElement);

		return (children != null) ? children.toArray() : new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return mReverseRelations.get(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		List<Object> children = mRelations.get(element);

		return (children != null) && (!children.isEmpty());
	}

	protected Collection<Object> getChildCollection(Object parentElement) {
		return (parentElement != null) ? mRelations.get(parentElement) : mRootElements;
	}

	@Override
	public void dispose() {
		// nothing to do
	}
}
