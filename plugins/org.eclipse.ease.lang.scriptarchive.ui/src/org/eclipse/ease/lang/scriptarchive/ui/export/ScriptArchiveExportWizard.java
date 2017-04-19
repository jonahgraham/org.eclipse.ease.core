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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.lang.scriptarchive.ui.PluginConstants;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ScriptArchiveExportWizard extends Wizard implements IExportWizard {

	private IStructuredSelection fSelection;
	private MainPage fMainPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection = selection;

		// load dialog settings
		final IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("ExportScriptAction"); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection("ExportScriptAction"); //$NON-NLS-1$

		setDialogSettings(wizardSettings);

		// set wizard image
		setDefaultPageImageDescriptor(Activator.getImageDescriptor(PluginConstants.PLUGIN_ID, "/icons/wizban/export_script_archive_wiz.png"));
	}

	@Override
	public void addPages() {
		super.addPages();

		fMainPage = new MainPage("Script Archive Export", "Executable Script Archive", null, fSelection);
		addPage(fMainPage);
	}

	@Override
	public boolean performFinish() {

		final IProject project = fMainPage.getSelectedProject();
		final IFile startupScript = fMainPage.getSelectedFile();
		final File targetFile = fMainPage.getTargetFile();

		// verify target location
		if (targetFile.exists()) {
			final boolean overwrite = MessageDialog.openQuestion(getShell(), "Question", "Target file already exists. Would you like to overwrite it?");
			if (!overwrite)
				return false;
		}

		if (!targetFile.getParentFile().exists()) {
			// folder does not exist, shall we create it?
			final boolean createTargetDir = MessageDialog.openQuestion(getShell(), "Question", "Target directory does not exist. Would you like to create it?");
			if (!createTargetDir)
				return false;

			if (!targetFile.getParentFile().mkdirs()) {
				ErrorDialog.openError(getShell(), "", null, // no special message
						new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Could not create target directories"));
				return false;
			}
		}

		// prepare manifest
		final String startupLocation = startupScript.getFullPath().removeFirstSegments(1).toPortableString();
		final IFile manifest = project.getFile(new Path("/META-INF/MANIFEST.MF"));

		final Properties properties = new Properties();
		if (manifest.exists()) {
			// load manifest
			try {
				properties.load(manifest.getContents());

			} catch (final IOException e) {
				ErrorDialog.openError(getShell(), "", null, // no special message
						new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Error reading project manifest", e));
				return false;
			} catch (final CoreException e) {
				// error while reading manifest
				ErrorDialog.openError(getShell(), "", null, // no special message
						new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Error reading project manifest", e));
				return false;
			}
		}

		// see if we need to modify the manifest
		try {
			if (!startupLocation.equals(properties.get("Main-Script"))) {
				properties.put("Main-Script", startupLocation);

				// manifest.set
				final ByteArrayOutputStream manifestContent = new ByteArrayOutputStream();
				properties.store(manifestContent, null);

				if (manifest.exists())
					manifest.setContents(new ByteArrayInputStream(manifestContent.toByteArray()), false, false, new NullProgressMonitor());
				else {
					if (!manifest.getParent().exists())
						((IFolder) manifest.getParent()).create(true, true, new NullProgressMonitor());

					manifest.create(new ByteArrayInputStream(manifestContent.toByteArray()), false, new NullProgressMonitor());
				}
			}
		} catch (final IOException e) {
			// error while dealing with ByteArrayStreams, not expected to throw
			ErrorDialog.openError(getShell(), "", null, // no special message
					new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Error modifying project manifest", e));
			return false;

		} catch (final CoreException e) {
			// error while writing manifest
			ErrorDialog.openError(getShell(), "", null, // no special message
					new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Error updating project manifest", e));
			return false;
		}

		// Save dirty editors if possible but do not stop if not all are saved
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);

		// about to invoke the operation so save our state
		saveWidgetValues();

		try {
			final ArchiveFileExportOperation archiveFileExportOperation = new ArchiveFileExportOperation(Arrays.asList(project.members()),
					targetFile.getAbsolutePath());
			return executeExportOperation(archiveFileExportOperation);
		} catch (final CoreException e) {
			ErrorDialog.openError(getShell(), "", null, // no special message
					new Status(IStatus.ERROR, PluginConstants.PLUGIN_ID, "Could not parse project content", e));
			return false;
		}
	}

	/**
	 * Persist wizard settings.
	 */
	private void saveWidgetValues() {
		final String[] lastLocations = getDialogSettings().getArray(MainPage.DIALOG_SETTINGS_LAST_LOCATIONS);
		if (lastLocations == null)
			getDialogSettings().put(MainPage.DIALOG_SETTINGS_LAST_LOCATIONS, new String[] { fMainPage.getTargetFile().getAbsolutePath() });

		else {
			final List<String> locations = new ArrayList<>(Arrays.asList(lastLocations));
			if (!locations.contains(fMainPage.getTargetFile().getAbsolutePath()))
				locations.add(0, fMainPage.getTargetFile().getAbsolutePath());

			getDialogSettings().put(MainPage.DIALOG_SETTINGS_LAST_LOCATIONS,
					locations.subList(0, Math.min(locations.size(), 5)).toArray(new String[locations.size()]));
		}
	}

	/**
	 * Export the passed resource and recursively export all of its child resources (iff it's a container). Answer a boolean indicating success.
	 */
	protected boolean executeExportOperation(ArchiveFileExportOperation op) {
		op.setCreateLeadupStructure(true);
		op.setUseCompression(true);
		op.setIncludeLinkedResources(true);

		try {
			getContainer().run(true, true, op);
		} catch (final InterruptedException e) {
			return false;
		} catch (final InvocationTargetException e) {
			displayErrorDialog(e.getTargetException().getMessage());
			return false;
		}

		final IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), "Export problems", null, // no special message
					status);
			return false;
		}

		return true;
	}

	/**
	 * Display an error dialog with the specified message.
	 *
	 * @param message
	 *            the error message
	 */
	protected void displayErrorDialog(String message) {
		MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(), "Script export error", message, SWT.SHEET);
	}
}
