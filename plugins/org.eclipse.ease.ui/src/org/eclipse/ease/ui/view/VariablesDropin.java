/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;

public class VariablesDropin implements IShellDropin, IExecutionListener {

	private TreeViewer fVariablesTree = null;
	private IScriptEngine fEngine;

	@Override
	public void setScriptEngine(IScriptEngine engine) {
		if (fEngine != null)
			fEngine.removeExecutionListener(this);

		fEngine = engine;

		if (fEngine != null)
			fEngine.addExecutionListener(this);

		// set tree input
		if (fVariablesTree != null) {
			fVariablesTree.setInput(engine);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					fVariablesTree.refresh();
				}
			});
		}
	}

	@Override
	public Composite createPartControl(final IWorkbenchPartSite site, final Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		final TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		composite.setLayout(treeColumnLayout);

		fVariablesTree = new TreeViewer(composite, SWT.BORDER);
		Tree tree = fVariablesTree.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		fVariablesTree.setFilters(new ViewerFilter[] {

				// filter modules
				new ViewerFilter() {

					@Override
					public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
						return !((Entry<?, ?>) element).getKey().toString().startsWith(EnvironmentModule.MODULE_PREFIX);
					}
				},

				// filter default methods of Object class
				new ViewerFilter() {

					@Override
					public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
						final Object name = ((Entry<?, ?>) element).getKey();
						if (("wait()".equals(name)) || ("notify()".equals(name)) || ("notifyAll()".equals(name)) || ("equals()".equals(name))
								|| ("getClass()".equals(name)) || ("hashCode()".equals(name)) || ("toString()".equals(name)))
							return false;

						return true;
					}
				} });

		fVariablesTree.setComparator(new ViewerComparator() {
			@Override
			public int category(final Object element) {
				return (((Entry<?, ?>) element).getKey().toString().endsWith("()")) ? 2 : 1;
			}
		});

		fVariablesTree.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(final Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object getParent(final Object element) {
				return null;
			}

			@Override
			public Object[] getElements(final Object inputElement) {
				if (inputElement instanceof IScriptEngine)
					return ((IScriptEngine) inputElement).getVariables().entrySet().toArray();

				return new Object[0];
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				final Object parent = ((Entry<?, ?>) parentElement).getValue();

				// use reflection to resolve elements
				final Map<String, Object> children = new HashMap<String, Object>();

				if (parent != null) {
					if (!((Entry<?, ?>) parentElement).getKey().toString().endsWith("()")) {
						// fields
						for (final Field field : parent.getClass().getFields()) {
							try {
								children.put(field.getName(), field.get(parent));
							} catch (final Exception e) {
								// ignore, try next
							}
						}

						// methods
						for (final Method method : parent.getClass().getMethods()) {
							try {
								children.put(method.getName() + "()", method.getReturnType().getName());
							} catch (final Exception e) {
								// ignore, try next
							}
						}
					}
				}

				return children.entrySet().toArray();
			}
		});

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column = treeViewerColumn.getColumn();
		treeColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText("Variable");
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Entry<?, ?>) element).getKey().toString();
			}

			@Override
			public Image getImage(final Object element) {
				if (((Entry<?, ?>) element).getKey().toString().endsWith("()"))
					return Activator.getImage(Activator.PLUGIN_ID, Activator.ICON_METHOD, true);

				return Activator.getImage(Activator.PLUGIN_ID, Activator.ICON_FIELD, true);
			}
		});

		final TreeViewerColumn treeViewerColumn2 = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column2 = treeViewerColumn2.getColumn();
		treeColumnLayout.setColumnData(column2, new ColumnWeightData(1));
		column2.setText("Content");
		treeViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final Object value = ((Entry<?, ?>) element).getValue();
				return (value != null) ? value.toString() : "[null]";
			}
		});

		fVariablesTree.setInput(fEngine);

		return composite;
	}

	@Override
	public String getTitle() {
		return "Variables";
	}

	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		switch (status) {
		case IExecutionListener.SCRIPT_END:
		case IExecutionListener.SCRIPT_INJECTION_END:
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					fVariablesTree.refresh();
				}
			});
			break;

		case IExecutionListener.ENGINE_END:
			engine.removeExecutionListener(this);
			break;
		}
	}
}
