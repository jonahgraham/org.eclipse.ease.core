/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.ease.Logger;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Quick launcher for EASE script files.
 */
public abstract class AbstractLaunchDelegate implements ILaunchShortcut, ILaunchShortcut2, ILaunchConfigurationDelegate, ILaunchConfigurationDelegate2 {

	// **********************************************************************
	// ILaunchShortcut
	// **********************************************************************

	@Override
	public final void launch(final IEditorPart editor, final String mode) {
		launch(getLaunchableResource(editor), mode);
	}

	@Override
	public final void launch(final ISelection selection, final String mode) {
		launch(getLaunchableResource(selection), mode);
	}

	// **********************************************************************
	// ILaunchShortcut2
	// **********************************************************************

	@Override
	public final IResource getLaunchableResource(final IEditorPart editorpart) {
		final IEditorInput input = editorpart.getEditorInput();
		if (input instanceof FileEditorInput)
			return ((FileEditorInput) input).getFile();

		return null;
	}

	@Override
	public final IResource getLaunchableResource(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (final Object element : ((IStructuredSelection) selection).toArray()) {
				if (element instanceof IFile)
					return (IResource) element;

				final Object adaptedFile = Platform.getAdapterManager().getAdapter(element, IResource.class);
				if (adaptedFile instanceof IFile)
					return (IResource) adaptedFile;
			}
		}

		return null;
	}

	@Override
	public final ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editorpart) {
		return getLaunchConfigurations(getLaunchableResource(editorpart), ILaunchManager.RUN_MODE);
	}

	@Override
	public final ILaunchConfiguration[] getLaunchConfigurations(final ISelection selection) {
		return getLaunchConfigurations(getLaunchableResource(selection), ILaunchManager.RUN_MODE);
	}

	// **********************************************************************
	// ILaunchConfigurationDelegate2
	// **********************************************************************

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return false;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	// **********************************************************************
	// internal stuff
	// **********************************************************************

	/**
	 * Get all launch configurations that target a dedicated resource file.
	 *
	 * @param resource
	 *            root file to execute
	 * @param mode
	 * @return {@link ILaunchConfiguration}s using resource
	 */
	protected ILaunchConfiguration[] getLaunchConfigurations(final IResource resource, final String mode) {
		final List<ILaunchConfiguration> configurations = new ArrayList<>();

		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = manager.getLaunchConfigurationType(getLaunchConfigurationId());

		// try to find existing configurations using the same file
		final String resourceLocation = ResourceTools.toAbsoluteLocation(resource, null);
		if (resourceLocation != null) {
			try {
				for (final ILaunchConfiguration configuration : manager.getLaunchConfigurations(type)) {
					final String configurationUri = getFileLocation(configuration);
					if (resourceLocation.equals(configurationUri)) {
						// we have a candidate
						configurations.add(configuration);
					}
				}
			} catch (final CoreException e) {
				// could not load configurations, ignore
			}
		}
		return configurations.toArray(new ILaunchConfiguration[configurations.size()]);
	}

	/**
	 * Launch a resource. Try to launch using a launch configuration. Used for contextual launches
	 *
	 * @param file
	 *            source file
	 * @param mode
	 *            launch mode
	 */
	private void launch(final IResource file, final String mode) {

		if (file instanceof IFile) {
			// try to save dirty editors
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);

			try {
				ILaunchConfiguration[] configurations = getLaunchConfigurations(file, mode);

				if (configurations.length == 0) {
					final ILaunchConfiguration configuration = createLaunchConfiguration(file, mode);

					configurations = new ILaunchConfiguration[] { configuration };
				}

				// launch
				configurations[0].launch(mode, new NullProgressMonitor());

			} catch (final CoreException e) {
				// could not launch configuration, giving up
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Launch Error", "Could not launch \"" + file + "\"");
				Logger.error(Activator.PLUGIN_ID, "Could not launch \"" + file + "\"", e);
			}
		}
	}

	protected abstract ILaunchConfiguration createLaunchConfiguration(IResource file, String mode) throws CoreException;

	protected abstract String getFileLocation(ILaunchConfiguration configuration) throws CoreException;

	protected abstract String getLaunchConfigurationId();
}
