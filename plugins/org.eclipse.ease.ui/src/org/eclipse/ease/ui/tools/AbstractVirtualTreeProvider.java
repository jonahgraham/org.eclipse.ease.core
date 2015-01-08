/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
	public static final IPath ROOT = new Path("");

	/** Tree elements and paths. */
	private final Map<IPath, Collection<Object>> fElements = new HashMap<IPath, Collection<Object>>();

	/** Replacement elements for nodes. */
	private final Map<IPath, Object> fReplacements = new HashMap<IPath, Object>();

	/** Marker to show/hide root node. */
	private boolean fShowRoot = false;

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

		populateElements(inputElement);
		if (fElements.isEmpty())
			registerPath(ROOT);

		if (fShowRoot) {
			if (fReplacements.containsKey(ROOT))
				return new Object[] { fReplacements.get(ROOT) };

			else
				return new Object[] { ROOT };
		}

		return replaceElements(fElements.get(ROOT));
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		final Object treeElement = findPathForReplacement(parentElement);
		return replaceElements(fElements.get(treeElement));
	}

	@Override
	public Object getParent(final Object element) {
		final Object treeElement = findPathForReplacement(element);

		for (final IPath path : fElements.keySet()) {
			if (fElements.get(path).contains(treeElement))
				return path;
		}

		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		final Object treeElement = findPathForReplacement(element);

		return (fElements.containsKey(treeElement)) && (!fElements.get(treeElement).isEmpty());
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
				final IPath parent = path.removeLastSegments(1);
				registerPath(parent);
				fElements.get(parent).add(path);
			}
		}
	}

	/**
	 * Register an element that should be used instead of a dedicated path node. Allows to display a dedicated object instead of a path. To replace the root
	 * element use {@link #ROOT} as path. Do not replace multiple nodes with the same object (regarding its equals() method) as the tree cannot handle such
	 * structures.
	 *
	 * @param path
	 *            path to be replaced
	 * @param element
	 *            replacement
	 */
	public void registerNodeReplacement(IPath path, Object element) {
		if (element instanceof IPath)
			throw new RuntimeException("Cannot replace a path with another path");

		fReplacements.put(path, element);
	}

	/**
	 * Set to show the single root node. The root node is hidden by default.
	 *
	 * @param showRoot
	 *            <code>true</code> to display the root node
	 */
	public void setShowRoot(boolean showRoot) {
		fShowRoot = showRoot;
	}

	/**
	 * Substitutes path elements with their registered replacements. If no replacement exists for a certain element, the element itself is returned.
	 *
	 * @param elements
	 *            elements to parse for replacements
	 * @return array with same size as elements containing replacements
	 */
	private Object[] replaceElements(Collection<Object> elements) {
		if (elements == null)
			return null;

		final HashSet<Object> result = new HashSet<Object>(elements);
		for (final Object element : elements) {
			if (fReplacements.containsKey(element)) {
				result.remove(element);
				result.add(fReplacements.get(element));
			}
		}

		return result.toArray(new Object[result.size()]);
	}

	/**
	 * Reverse lookup for replacements. Finds original path element for a dedicated object.
	 *
	 * @param replacement
	 *            replacement element to look up
	 * @return original path element or replacement, if not found
	 */
	private Object findPathForReplacement(Object replacement) {
		if (replacement instanceof IPath)
			return replacement;

		if (fReplacements.values().contains(replacement)) {
			for (final Entry<IPath, Object> entry : fReplacements.entrySet()) {
				if (replacement.equals(entry.getValue()))
					return entry.getKey();
			}
		}

		return replacement;
	}

	/**
	 * Needs to register all tree elements with their paths.
	 *
	 * @param inputElement
	 *            tree input
	 */
	protected abstract void populateElements(Object inputElement);
}
