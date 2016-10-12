/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.tools.StringTools;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

public class ModulesDragListener implements DragSourceListener {

	private final TreeViewer fTreeViewer;
	private final TableViewer fTableViewer;

	public ModulesDragListener(final TreeViewer viewer) {
		fTreeViewer = viewer;
		fTableViewer = null;
	}

	public ModulesDragListener(final TableViewer viewer) {
		fTableViewer = viewer;
		fTreeViewer = null;
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		event.doit = !(event.data instanceof IPath);
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {

		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
			LocalSelectionTransfer.getTransfer().setSelection(getSelection());

		} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {

			Object firstElement = getSelection().getFirstElement();

			// unpack field/method
			if (firstElement instanceof ModuleEntry)
				firstElement = ((ModuleEntry) firstElement).getEntry();

			final StringBuilder data = new StringBuilder();

			if (firstElement instanceof ModuleDefinition)
				data.append("loadModule('").append(((ModuleDefinition) firstElement).getPath().toString()).append("');" + StringTools.LINE_DELIMITER);

			else if (firstElement instanceof Field)
				data.append(((Field) firstElement).getName());

			else if (firstElement instanceof Method)
				data.append(ModulesTools.getSignature((Method) firstElement, false)).append(";" + StringTools.LINE_DELIMITER);

			event.data = data.toString();
		}
	}

	private IStructuredSelection getSelection() {
		if (fTreeViewer != null)
			return fTreeViewer.getStructuredSelection();

		if (fTableViewer != null)
			return fTableViewer.getStructuredSelection();

		return StructuredSelection.EMPTY;
	}

	@Override
	public void dragFinished(final DragSourceEvent event) {
		// nothing to do
	}
}
