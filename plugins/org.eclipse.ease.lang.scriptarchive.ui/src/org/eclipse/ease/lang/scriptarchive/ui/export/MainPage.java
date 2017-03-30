/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.scriptarchive.ui.export;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class MainPage extends WizardPage {

	public static final String DIALOG_SETTINGS_LAST_LOCATIONS = "Last path";

	private Text fTxtProject;
	private Text fTxtScriptFile;
	private final IStructuredSelection fSelection;
	private IProject fSelectedProject;
	private IFile fSelectedFile;
	private Combo fCmbTargetLocation;
	private File fTargetFile;

	/**
	 * Creates a new wizard page with the given name, title, and image.
	 *
	 * @param pageName
	 *            the name of the page
	 * @param title
	 *            the title for this wizard page, or <code>null</code> if none
	 * @param titleImage
	 *            the image descriptor for the title of this wizard page, or <code>null</code> if none
	 * @param selection
	 */
	protected MainPage(String pageName, String title, ImageDescriptor titleImage, IStructuredSelection selection) {
		super(pageName, title, titleImage);
		fSelection = selection;

		setDescription("Create an executable script archive.");
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(3, false));

		final Label lblProject = new Label(container, SWT.NONE);
		lblProject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblProject.setText("Project:");

		fTxtProject = new Text(container, SWT.BORDER);
		fTxtProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Button btnBrowseProject = new Button(container, SWT.NONE);
		btnBrowseProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				final ListDialog dialog = new ListDialog(getShell());
				dialog.setContentProvider(ArrayContentProvider.getInstance());
				dialog.setLabelProvider(new WorkbenchLabelProvider());
				dialog.setInput(getOpenProject());

				dialog.setTitle("Project selection");
				dialog.setMessage("Select project to export.");

				if (dialog.open() == Window.OK) {
					final IProject project = (IProject) dialog.getResult()[0];
					fTxtProject.setText(project.getName());

					validateData();
				}
			}
		});
		btnBrowseProject.setText("Browse...");

		final Label lblStartScript = new Label(container, SWT.NONE);
		lblStartScript.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartScript.setText("Start Script:");

		fTxtScriptFile = new Text(container, SWT.BORDER);
		fTxtScriptFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Button btnBrowseScript = new Button(container, SWT.NONE);
		btnBrowseScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider());

				final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fTxtProject.getText());

				dialog.setInput(project.exists() ? project : ResourcesPlugin.getWorkspace().getRoot());
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

				dialog.setTitle("Script selection");
				dialog.setMessage("Select startup script.");

				if (dialog.open() == Window.OK) {
					final Object selectedFile = dialog.getFirstResult();
					if (selectedFile instanceof IFile) {
						fTxtProject.setText(((IFile) selectedFile).getProject().getName());
						fTxtScriptFile.setText(((IFile) selectedFile).getFullPath().removeFirstSegments(1).toPortableString());

						validateData();
					}
				}
			}
		});
		btnBrowseScript.setText("Browse...");

		final Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

		final Label lblToArchiveFile = new Label(container, SWT.NONE);
		lblToArchiveFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblToArchiveFile.setText("To archive file:");

		fCmbTargetLocation = new Combo(container, SWT.NONE);
		fCmbTargetLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Button btnBrowseArchive = new Button(container, SWT.NONE);
		btnBrowseArchive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
				dialog.setText("Target location selection");

				dialog.setFilterExtensions(new String[] { "*.sar;*.jar;*.zip", "*.*" });
				dialog.setFilterNames(new String[] { "Script Archives", "All Files" });

				final String targetLocation = dialog.open();
				if (targetLocation != null) {
					fCmbTargetLocation.setText(targetLocation);
					validateData();
				}
			}
		});
		btnBrowseArchive.setText("Browse...");

		setPageComplete(false);

		// apply user selection
		if (fSelection != null) {
			final Object candidate = fSelection.getFirstElement();
			if (candidate instanceof IResource) {
				final IProject project = ((IResource) candidate).getProject();
				if (project.isOpen()) {
					fTxtProject.setText(project.getName());

					// now look for a file resource
					if (candidate instanceof IFile)
						fTxtScriptFile.setText(((IResource) candidate).getFullPath().removeFirstSegments(1).toPortableString());
				}
			}
		}

		// restore previous locations
		final String[] lastLocations = getDialogSettings().getArray(DIALOG_SETTINGS_LAST_LOCATIONS);
		if (lastLocations != null) {
			fCmbTargetLocation.setItems(lastLocations);
			fCmbTargetLocation.setText(lastLocations[0]);
		}

		// validate to enable/disable finish button
		validateData();
		// do not display error messages right after opening the wizard
		super.setErrorMessage(null);

		// activate modify listeners
		fTxtProject.addModifyListener((e) -> validateData());
		fTxtScriptFile.addModifyListener((e) -> validateData());
		fCmbTargetLocation.addModifyListener((e) -> validateData());
	}

	/**
	 * Returns a list of open projects in the workspace.
	 *
	 * @return list of open projects
	 */
	private Object getOpenProject() {
		final ArrayList<IProject> openProjects = new ArrayList<>();
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen())
				openProjects.add(project);
		}

		return openProjects;
	}

	/**
	 * Validate input data and set page completion status.
	 */
	private void validateData() {
		final String projectName = fTxtProject.getText();
		if (!projectName.isEmpty()) {

			final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project.exists()) {

				// validate file
				final String fileName = fTxtScriptFile.getText();
				if (!fileName.isEmpty()) {
					final IFile file = project.getFile(new Path(fileName));
					if (file.exists()) {

						// validate target location
						final String targetLocation = fCmbTargetLocation.getText();
						if (!targetLocation.isEmpty()) {
							final File targetFile = new File(targetLocation);
							if (!targetFile.isDirectory()) {
								// validation done
								fSelectedProject = project;
								fSelectedFile = file;
								fTargetFile = targetFile;

								setErrorMessage(null);

							} else
								setErrorMessage("Expot destionation must be a file, not a directory.");

						} else
							setErrorMessage("Please select an export location.");

					} else
						setErrorMessage("The file \"" + fileName + "\" does not exist.");

				} else
					setErrorMessage("Please select a script file to execute.");

			} else
				setErrorMessage("The project \"" + projectName + "\" does not exist.");

		} else
			setErrorMessage("Please select a project.");
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		setPageComplete(errorMessage == null);

		super.setErrorMessage(errorMessage);
	}

	public IFile getSelectedFile() {
		return fSelectedFile;
	}

	public IProject getSelectedProject() {
		return fSelectedProject;
	}

	public File getTargetFile() {
		return fTargetFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.wizard.WizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
	 */
	@Override
	public void setWizard(IWizard newWizard) {
		// TODO Auto-generated method stub
		super.setWizard(newWizard);
	}
}
