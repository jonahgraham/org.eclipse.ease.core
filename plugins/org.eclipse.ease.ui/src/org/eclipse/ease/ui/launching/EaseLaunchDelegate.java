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
package org.eclipse.ease.ui.launching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Quick launcher for EASE script files.
 */
public class EaseLaunchDelegate implements ILaunchShortcut, ILaunchShortcut2, ILaunchConfigurationDelegate {

	private static final String LAUNCH_CONFIGURATION_ID = "org.eclipse.ease.launchConfigurationType";

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
			}
		}

		return null;
	}

	@Override
	public final ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editorpart) {
		return getLaunchConfgurations(getLaunchableResource(editorpart), ILaunchManager.RUN_MODE);
	}

	@Override
	public final ILaunchConfiguration[] getLaunchConfigurations(final ISelection selection) {
		return getLaunchConfgurations(getLaunchableResource(selection), ILaunchManager.RUN_MODE);
	}

	// **********************************************************************
	// ILaunchConfigurationDelegate
	// **********************************************************************

	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		Object resource = ResourceTools.getResource(configuration.getAttribute(LaunchConstants.FILE_LOCATION, ""));
		;
		if (resource != null) {
			// we have a valid script, lets feed it to the script engine
			launch(resource, configuration, mode, launch, monitor);
		}
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
	private ILaunchConfiguration[] getLaunchConfgurations(final IResource resource, final String mode) {
		final List<ILaunchConfiguration> configurations = new ArrayList<ILaunchConfiguration>();

		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_ID);

		// try to find existing configurations using the same file
		String resourceLocation = ResourceTools.toAbsoluteLocation(resource, null);
		try {
			for (final ILaunchConfiguration configuration : manager.getLaunchConfigurations(type)) {
				try {
					String configurationUri = configuration.getAttribute(LaunchConstants.FILE_LOCATION, "");
					if (resourceLocation.equals(configurationUri)) {
						// we have a candidate
						if (ILaunchManager.DEBUG_MODE.equals(mode)) {
							String engineID = configuration.getAttribute(LaunchConstants.SCRIPT_ENGINE, "");
							final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
							EngineDescription engineDescription = scriptService.getEngineByID(engineID);
							if (engineDescription.supportsDebugging())
								configurations.add(configuration);

						} else
							configurations.add(configuration);
					}

				} catch (final CoreException e) {
					// could not read configuration, ignore
				}
			}
		} catch (final CoreException e) {
			// could not load configurations, ignore
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
				ILaunchConfiguration[] configurations = getLaunchConfgurations(file, mode);

				if (configurations.length == 0) {
					// no configuration found, create new one
					final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
					final ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_ID);

					final ILaunchConfigurationWorkingCopy configuration = type.newInstance(null, file.getName());
					configuration.setAttribute(LaunchConstants.FILE_LOCATION, ResourceTools.toAbsoluteLocation(file, null));

					// find a valid engine
					final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
					Collection<EngineDescription> engines = scriptService.getScriptType(ResourceTools.toAbsoluteLocation(file, null)).getEngines();
					if (engines.isEmpty())
						// TODO use a better way to bail out and use the direct file launch
						throw new CoreException(Status.CANCEL_STATUS);

					configuration.setAttribute(LaunchConstants.SCRIPT_ENGINE, engines.iterator().next().getID());

					// save and return new configuration
					configuration.doSave();

					configurations = new ILaunchConfiguration[] { configuration };
				}

				// launch
				configurations[0].launch(mode, new NullProgressMonitor());

			} catch (final CoreException e) {
				// could not create launch configuration, run file directly
				launch(file, null, mode, null, new NullProgressMonitor());
			}
		}
	}

	/**
	 * Execute script code from an {@link IFile}.
	 *
	 * @param resource
	 *            resource to execute
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode
	 * @param launch
	 * @param monitor
	 */
	private void launch(final Object resource, final ILaunchConfiguration configuration, final String mode, final ILaunch launch, final IProgressMonitor monitor) {

		try {
			String engineID = configuration.getAttribute(LaunchConstants.SCRIPT_ENGINE, "");
			final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
			final IScriptEngine engine = scriptService.getEngineByID(engineID).createEngine();

			final ScriptConsole console = ScriptConsole.create(engine.getName() + ": " + resource, engine);
			engine.setOutputStream(console.getOutputStream());
			engine.setErrorStream(console.getErrorStream());

			engine.setTerminateOnIdle(true);

			if (ILaunchManager.DEBUG_MODE.equals(mode))
				setupDebugger(engine, configuration, launch);

			// set startup parameters
			String parameterString = configuration.getAttribute(LaunchConstants.STARTUP_PARAMETERS, "").trim();
			String[] parameters;
			if (!parameterString.isEmpty()) {
				parameters = parameterString.split("\\s+");

			} else
				parameters = new String[0];

			engine.setVariable("argv", parameters);

			engine.executeAsync(resource);

			engine.schedule();

		} catch (CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}
	}

	private void setupDebugger(final IScriptEngine engine, final ILaunchConfiguration configuration, final ILaunch launch) {
		if (engine instanceof IDebugEngine) {
			boolean suspendOnStartup = false;
			try {
				suspendOnStartup = configuration.getAttribute(LaunchConstants.SUSPEND_ON_STARTUP, false);
			} catch (CoreException e) {
			}

			boolean suspendOnScriptLoad = false;
			try {
				suspendOnScriptLoad = configuration.getAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, false);
			} catch (CoreException e) {
			}

			boolean showDynamicCode = false;
			try {
				showDynamicCode = configuration.getAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, false);
			} catch (CoreException e) {
			}

			((IDebugEngine) engine).setupDebugger(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		}
	}
}
