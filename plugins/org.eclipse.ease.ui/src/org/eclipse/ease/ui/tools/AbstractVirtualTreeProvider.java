/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A virtual tree content provider. Allows to build a tree structure by registering tree elements using {@link IPath}s. When the input changes
 * {@link #populateElements(Object)} is called on the derived class to create the tree structure.
 * 
 */
public abstract class AbstractVirtualTreeProvider implements ITreeContentProvider {

	/** Static root node. */
	private static final IPath ROOT = new Path("");

	/** Tree elements and paths. */
	private final Map<IPath, Collection<Object>> fElements = new HashMap<IPath, Collection<Object>>();

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// nothing to do
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		fElements.clear();
		registerPath(ROOT);

		populateElements(inputElement);

		return fElements.get(ROOT).toArray();
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		Collection<Object> children = fElements.get(parentElement);
		if (children != null)
			return children.toArray();

		return new Object[0];
	}

	@Override
	public Object getParent(final Object element) {
		for (IPath path : fElements.keySet()) {
			if (fElements.get(path).contains(element))
				return path;
		}

		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return (fElements.containsKey(element)) && (!fElements.get(element).isEmpty());
	}

	/**
	 * Register an element contained within the tree. To register an element 'myFoo' under the entry '/my/element/is/myFoo' use '/my/element/is' as path. The
	 * LabelProvider needs to take care of the rendering of the element itself.
	 * 
	 * @param path
	 *            full path to be used to display this element (excluding element entry)
	 * @param element
	 *            element to be stored within path
	 */
	public void registerElement(IPath path, final Object element) {
		path = path.makeRelative();
		registerPath(path);

		fElements.get(path).add(element);
	}

	/**
	 * Register an element path to be visible on the tree.
	 * 
	 * @param path
	 *            path to be visible
	 */
	public void registerPath(IPath path) {
		path = path.makeRelative();
		if (!fElements.containsKey(path)) {
			fElements.put(path, new HashSet<Object>());

			if (!path.isEmpty()) {
				IPath parent = path.removeLastSegments(1);
				registerPath(parent);
				fElements.get(parent).add(path);
			}
		}
	}

	/**
	 * Needs to register all tree elements with their paths.
	 * 
	 * @param inputElement
	 *            tree input
	 */
	protected abstract void populateElements(Object inputElement);
}
