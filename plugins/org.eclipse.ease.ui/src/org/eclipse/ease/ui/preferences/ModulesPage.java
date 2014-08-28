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

import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ModulesPage extends PreferencePage implements IWorkbenchPreferencePage {
	private IWorkbench mWorkbench;
	private TreeViewer visibleTreeViewer;
	private TreeViewer invisibleTreeViewer;

	public ModulesPage() {
	}

	@Override
	public void init(IWorkbench workbench) {
		mWorkbench = workbench;
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(3, false));

		Label lblVisibleModules = new Label(composite, SWT.NONE);
		lblVisibleModules.setText("Visible Modules");
		new Label(composite, SWT.NONE);

		Label lblHiddenModules = new Label(composite, SWT.NONE);
		lblHiddenModules.setText("Hidden Modules");

		visibleTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI);
		Tree tree = visibleTreeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		visibleTreeViewer.setLabelProvider(new LabelProvider());
		visibleTreeViewer.setContentProvider(new StringTreeContentProvider());
		visibleTreeViewer.setSorter(new ViewerSorter());

		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) visibleTreeViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object element : selection.toList()) {
						String path = ((StringTreeContentProvider) visibleTreeViewer.getContentProvider()).getAbsolutePath(element);
						hideModule(path);
					}

					populateTrees();
				}
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, true, 1, 1));
		btnNewButton.setText("-->");

		invisibleTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI);
		Tree tree_1 = invisibleTreeViewer.getTree();
		tree_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		invisibleTreeViewer.setLabelProvider(new LabelProvider());
		invisibleTreeViewer.setContentProvider(new StringTreeContentProvider());
		invisibleTreeViewer.setSorter(new ViewerSorter());

		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) invisibleTreeViewer.getSelection();
				if (!selection.isEmpty()) {
					for (Object element : selection.toList()) {
						String path = ((StringTreeContentProvider) invisibleTreeViewer.getContentProvider()).getAbsolutePath(element);
						unhideModule(path);
					}

					populateTrees();
				}
			}
		});
		btnNewButton_1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));
		btnNewButton_1.setText("<--");

		populateTrees();

		return composite;
	}

	protected void hideModule(String name) {
		// FIXME temporary disabled
		// final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		// for (ModuleDefinition definition : scriptService.getAvailableModules().values()) {
		// if (definition.getPath().toString().startsWith(name)) {
		// IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		// Preferences node = prefs.node("modules");
		// node.putBoolean(definition.getAbsoluteName(), false);
		// }
		// }
	}

	protected void unhideModule(String name) {
		// FIXME temporary disabled
		// final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		// for (ModuleDefinition definition : scriptService.getAvailableModules().values()) {
		// if (definition.getAbsoluteName().startsWith(name)) {
		// IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		// Preferences node = prefs.node("modules");
		// node.remove(definition.getAbsoluteName());
		// }
		// }
	}

	private void populateTrees() {
		visibleTreeViewer.setInput("foo"); // enable tree, we fill the content provider afterwards
		invisibleTreeViewer.setInput("foo"); // enable tree, we fill the content provider afterwards

		// FIXME temporary disabled
		// final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		// for (ModuleDefinition definition : scriptService.getAvailableModules().values()) {
		// if (definition.isVisible())
		// ((StringTreeContentProvider) (visibleTreeViewer.getContentProvider())).addElement(definition.getAbsoluteName(), null);
		// else
		// ((StringTreeContentProvider) (invisibleTreeViewer.getContentProvider())).addElement(definition.getAbsoluteName(), null);
		// }

		visibleTreeViewer.refresh();
		invisibleTreeViewer.refresh();
	}
}
