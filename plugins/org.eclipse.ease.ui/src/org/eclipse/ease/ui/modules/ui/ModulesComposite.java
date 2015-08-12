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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ease.ui.help.hovers.ModuleToolTipDecorator;;

public class ModulesComposite extends Composite {
	private final TreeViewer treeViewer;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param modulesOnly
	 *            if true only the modules are shown in the tree. if false also the fields and functions are shown.
	 */
	public ModulesComposite(final Composite parent, final int style, final boolean modulesOnly) {
		super(parent, style);

		treeViewer = new TreeViewer(this, SWT.NONE);

		setLayout(new FillLayout(SWT.HORIZONTAL));
		treeViewer.getTree().setLayout(new FillLayout(SWT.HORIZONTAL));

		// ColumnViewerToolTipSupport.enableFor(treeViewer);
		ModuleToolTipDecorator.enableFor(treeViewer);

		// use a decorated label provider
		treeViewer.setLabelProvider(new ModulesDecoratedLabelProvider(new ModulesLabelProvider()));

		treeViewer.setContentProvider(new ModulesContentProvider(modulesOnly));

		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(final Object element) {
				if ((element instanceof IPath))
					return 1;
				if ((element instanceof ModuleDefinition))
					return 2;
				if ((element instanceof Field))
					return 2;
				if ((element instanceof Method))
					return 3;
				return 4;

			}
		});

		treeViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { TextTransfer.getInstance() }, new ModulesDragListener(treeViewer));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void setInput(final Object input) {
		treeViewer.setInput(input);
	}

	public void refresh() {
		treeViewer.refresh();
	}

	public void addFilter(final ViewerFilter filter) {
		treeViewer.addFilter(filter);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
}
