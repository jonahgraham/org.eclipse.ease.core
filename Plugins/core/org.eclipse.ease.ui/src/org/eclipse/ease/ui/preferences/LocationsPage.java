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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.repository.IEntry;
import org.eclipse.ease.ui.repository.ILocation;
import org.eclipse.ease.ui.repository.IRepositoryFactory;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.osgi.service.prefs.BackingStoreException;

public class LocationsPage extends PreferencePage implements IWorkbenchPreferencePage {
	private TableViewer tableViewer;
	private final Set<IEntry> fEntries = new HashSet<IEntry>();;

	public LocationsPage() {
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		{
			Label lblProvideLocationsTo = new Label(container, SWT.NONE);
			lblProvideLocationsTo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			lblProvideLocationsTo.setText("Provide locations to look for scripts. The default location will be used to store your recorded scripts.");
		}
		{
			Composite composite = new Composite(container, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 6));
			TableColumnLayout tcl_composite = new TableColumnLayout();
			composite.setLayout(tcl_composite);
			{
				tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
				final Table table = tableViewer.getTable();
				table.setHeaderVisible(true);
				table.setLinesVisible(true);
				{
					TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
					tableViewerColumn.setEditingSupport(new EditingSupport(tableViewer) {
						@Override
						protected boolean canEdit(final Object element) {
							return true;
						}

						@Override
						protected CellEditor getCellEditor(final Object element) {
							return new TextCellEditor(table);
						}

						@Override
						protected Object getValue(final Object element) {
							if (element instanceof IEntry)
								return ((IEntry) element).getLocation();

							return "";
						}

						@Override
						protected void setValue(final Object element, final Object value) {
							if (element instanceof IEntry) {
								((IEntry) element).setLocation(value.toString());
								tableViewer.update(element, null);
							}
						}
					});
					TableColumn tblclmnLocation = tableViewerColumn.getColumn();
					tcl_composite.setColumnData(tblclmnLocation, new ColumnWeightData(5, ColumnWeightData.MINIMUM_WIDTH, true));
					tblclmnLocation.setText("Location");
					tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
						@Override
						public String getText(final Object element) {
							if (element instanceof IEntry) {
								if (((IEntry) element).isDefault())
									return ((IEntry) element).getLocation() + " (default)";
								else
									return ((IEntry) element).getLocation();
							}

							return super.getText(element);
						}

						@Override
						public Font getFont(final Object element) {
							if (element instanceof IEntry) {
								if (((IEntry) element).isDefault())
									return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
							}

							return super.getFont(element);
						}
					});
				}
				{
					TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
					tableViewerColumn.setEditingSupport(new EditingSupport(tableViewer) {
						@Override
						protected boolean canEdit(final Object element) {
							return true;
						}

						@Override
						protected CellEditor getCellEditor(final Object element) {
							return new CheckboxCellEditor(table);
						}

						@Override
						protected Object getValue(final Object element) {
							if (element instanceof IEntry)
								return ((IEntry) element).isRecursive();

							return false;
						}

						@Override
						protected void setValue(final Object element, final Object value) {
							if (element instanceof IEntry) {
								((IEntry) element).setRecursive((Boolean) value);
								tableViewer.update(element, null);
							}
						}
					});
					TableColumn tblclmnRecursive = tableViewerColumn.getColumn();
					tcl_composite.setColumnData(tblclmnRecursive, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
					tblclmnRecursive.setText("Recursive");
					tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
						@Override
						public String getText(final Object element) {
							if (element instanceof IEntry)
								return ((IEntry) element).isRecursive() ? "true" : "false";

							return super.getText(element);
						}
					});
				}

				tableViewer.setContentProvider(ArrayContentProvider.getInstance());
				tableViewer.setComparator(new ViewerComparator() {
					@Override
					public int compare(final Viewer viewer, final Object e1, final Object e2) {
						if ((e1 instanceof ILocation) && (e2 instanceof ILocation))
							return (((ILocation) e1).getLocation()).compareTo(((ILocation) e2).getLocation());

						return super.compare(viewer, e1, e2);
					}
				});

				tableViewer.setInput(fEntries);
			}
		}
		{
			Button btnAddWorkspace = new Button(container, SWT.NONE);
			btnAddWorkspace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), true,
							"Select script folder");
					if (dialog.open() == Window.OK) {
						Object[] result = dialog.getResult();
						if ((result.length > 0) && (result[0] instanceof IPath))
							addEntry("workspace:/" + result[0].toString());
					}
				}
			});
			btnAddWorkspace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnAddWorkspace.setText("Add Workspace...");
		}
		{
			Button btnAddFileSystem = new Button(container, SWT.NONE);
			btnAddFileSystem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					DirectoryDialog dialog = new DirectoryDialog(getShell());
					String path = dialog.open();
					if (path != null)
						addEntry(new File(path).toURI().toString());
				}
			});
			btnAddFileSystem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnAddFileSystem.setText("Add File System...");
		}
		{
			Button btnAddUri = new Button(container, SWT.NONE);
			btnAddUri.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					InputDialog dialog = new InputDialog(getShell(), "Enter location URI", "Enter the URI of a location to add", "", new URIValidator());
					if (dialog.open() == Window.OK)
						addEntry(dialog.getValue());
				}
			});
			btnAddUri.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnAddUri.setText("Add URI...");
		}
		;
		new Label(container, SWT.NONE);
		;
		{
			Button btnSetAsDefault = new Button(container, SWT.NONE);
			btnSetAsDefault.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
					if (!selection.isEmpty()) {
						Collection<IEntry> entries = (Collection<IEntry>) tableViewer.getInput();
						for (IEntry entry : entries)
							entry.setDefault(entry.equals(selection.getFirstElement()));
					}

					tableViewer.refresh();
				}
			});
			btnSetAsDefault.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, true, 1, 1));
			btnSetAsDefault.setText("Default");
		}
		{
			Button btnDelete = new Button(container, SWT.NONE);
			btnDelete.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
					if (!selection.isEmpty()) {
						for (Object location : selection.toList())
							fEntries.remove(location);

						// verify that we have a default entry
						boolean hasDefault = false;
						for (IEntry entry : fEntries)
							hasDefault |= entry.isDefault();

						if ((!hasDefault) && (!fEntries.isEmpty()))
							fEntries.iterator().next().setDefault(true);

						// refresh UI
						tableViewer.refresh();
					}
				}
			});
			btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
			btnDelete.setText("Delete");
		}

		performDefaults();

		return container;
	}

	private void addEntry(final String location) {

		IEntry entry = IRepositoryFactory.eINSTANCE.createEntry();
		entry.setLocation(location);
		entry.setRecursive(true);
		// first entry is also the default entry
		entry.setDefault(fEntries.isEmpty());

		fEntries.add(entry);

		tableViewer.refresh();
	}

	@Override
	protected void performDefaults() {

		fEntries.clear();
		fEntries.addAll(PreferencesHelper.getLocations());

		// update UI
		if (tableViewer != null)
			tableViewer.refresh();

		super.performDefaults();
	}

	@Override
	public boolean performOk() {

		// remove existing child nodes
		try {
			PreferencesHelper.clearLocations();
		} catch (BackingStoreException e) {
			Logger.logError("Could not update script location preferences", e);
			return false;
		}

		// add entries
		for (IEntry entry : fEntries)
			PreferencesHelper.addLocation(entry);

		// update repository
		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
		repositoryService.updateLocations();

		return super.performOk();
	}
}
