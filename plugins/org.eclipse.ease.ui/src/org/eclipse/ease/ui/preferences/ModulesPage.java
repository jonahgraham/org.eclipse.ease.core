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
package org.eclipse.ease.ui.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class ModulesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private class VisibilityFilter extends ViewerFilter {

		private final boolean fShowVisible;

		public VisibilityFilter(final boolean showVisible) {
			fShowVisible = showVisible;
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
			if (element instanceof ModuleDefinition)
				return ((ModuleDefinition) element).isVisible() == fShowVisible;

			if (element instanceof IPath) {
				final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
				for (ModuleDefinition definition : scriptService.getAvailableModules().values()) {
					if ((((IPath) element).isPrefixOf(definition.getPath())) && (definition.isVisible() == fShowVisible))
						return true;
				}

				return false;
			}

			return true;
		}
	};

	private IWorkbench fWorkbench;
	private TreeViewer visibleTreeViewer;
	private TreeViewer invisibleTreeViewer;

	public ModulesPage() {
	}

	@Override
	public void init(final IWorkbench workbench) {
		fWorkbench = workbench;
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));

		Label lblUseDragAnd = new Label(composite, SWT.NONE);
		lblUseDragAnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblUseDragAnd.setText("Use drag and drop to organize module visibility.");

		Label lblVisibleModules = new Label(composite, SWT.NONE);
		lblVisibleModules.setText("Visible Modules");

		Label lblHiddenModules = new Label(composite, SWT.NONE);
		lblHiddenModules.setText("Hidden Modules");

		visibleTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI);
		Tree tree = visibleTreeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		visibleTreeViewer.setLabelProvider(new ModulesLabelProvider());
		visibleTreeViewer.setContentProvider(new ModulesContentProvider());
		visibleTreeViewer.setSorter(new ViewerSorter() {
			@Override
			public int category(final Object element) {
				return (element instanceof IPath) ? 1 : 2;
			}
		});
		visibleTreeViewer.setFilters(new ViewerFilter[] { new VisibilityFilter(true) });
		addDNDSupport(visibleTreeViewer, true);

		invisibleTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI);
		Tree tree_1 = invisibleTreeViewer.getTree();
		tree_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		invisibleTreeViewer.setLabelProvider(new ModulesLabelProvider());
		invisibleTreeViewer.setContentProvider(new ModulesContentProvider());
		invisibleTreeViewer.setSorter(new ViewerSorter() {
			@Override
			public int category(final Object element) {
				return (element instanceof IPath) ? 1 : 2;
			}
		});
		invisibleTreeViewer.setFilters(new ViewerFilter[] { new VisibilityFilter(false) });
		addDNDSupport(invisibleTreeViewer, false);

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		Collection<ModuleDefinition> modules = scriptService.getAvailableModules().values();
		visibleTreeViewer.setInput(modules);
		invisibleTreeViewer.setInput(modules);

		return composite;
	}

	private void addDNDSupport(final StructuredViewer viewer, final boolean dropVisible) {
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new DragSourceAdapter() {
			@Override
			public void dragSetData(final DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(viewer.getSelection());
			}

			@Override
			public void dragFinished(final DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}
		});

		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() }, new DropTargetAdapter() {
			@Override
			public void drop(final DropTargetEvent event) {
				IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
				for (Object element : selection.toArray()) {
					if (element instanceof ModuleDefinition)
						((ModuleDefinition) element).setVisible(dropVisible);

					else if (element instanceof IPath) {
						final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
						for (ModuleDefinition definition : scriptService.getAvailableModules().values()) {
							if (((IPath) element).isPrefixOf(definition.getPath()))
								definition.setVisible(dropVisible);
						}
					}
				}

				visibleTreeViewer.refresh();
				invisibleTreeViewer.refresh();
			}
		});
	}

	@Override
	protected void performDefaults() {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		for (ModuleDefinition definition : scriptService.getAvailableModules().values())
			definition.resetVisible();

		visibleTreeViewer.refresh();
		invisibleTreeViewer.refresh();
	}
}
