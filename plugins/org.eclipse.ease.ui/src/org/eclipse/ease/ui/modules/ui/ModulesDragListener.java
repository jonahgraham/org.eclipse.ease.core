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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

public class ModulesDragListener implements DragSourceListener {

	private final TreeViewer mTreeViewer;

	public ModulesDragListener(final TreeViewer viewer) {
		mTreeViewer = viewer;
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		event.doit = !(event.data instanceof IPath);
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) mTreeViewer.getSelection();
		Object firstElement = selection.getFirstElement();

		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {

			StringBuilder data = new StringBuilder();
			if (firstElement instanceof ModuleDefinition)
				data.append("loadModule('").append(((ModuleDefinition) firstElement).getPath().toString()).append("');\n");

			else if (firstElement instanceof Field)
				data.append(((Field) firstElement).getName());

			else if (firstElement instanceof Method)
				data.append(ModulesTools.getSignature((Method) firstElement)).append(";\n");

			event.data = data.toString();
		}

	}

	@Override
	public void dragFinished(final DragSourceEvent event) {
		// nothing to do
	}
}
