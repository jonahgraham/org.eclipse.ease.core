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

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.ease.ui.tools.AbstractLaunchDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Quick launcher for EASE script files.
 */
public class EaseLaunchDelegate extends AbstractLaunchDelegate {

	private static final String LAUNCH_CONFIGURATION_ID = "org.eclipse.ease.launchConfigurationType";

	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch, final IProgressMonitor monitor) throws CoreException {

		final Object resource = ResourceTools.resolveFile(getFileLocation(configuration), null, true);

		// create engine
		final String engineID = configuration.getAttribute(LaunchConstants.SCRIPT_ENGINE, "");
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		EngineDescription engineDescription = scriptService.getEngineByID(engineID);
		if ((ILaunchManager.DEBUG_MODE.equals(mode)) && (!engineDescription.supportsDebugging())) {
			// we are trying to debug using an engine that does not support debugging
			engineDescription = null;

			// try to find an engine that supports debugging
			final ScriptType scriptType = scriptService.getScriptType(ResourceTools.toAbsoluteLocation(resource, null));
			final List<EngineDescription> engines = scriptService.getEngines(scriptType.getName());
			for (final EngineDescription description : engines) {
				if (description.supportsDebugging()) {
					// matching debug engine found
					engineDescription = description;
					break;
				}
			}

			if (engineDescription != null) {
				// ask user if he wants to change the engine: once, permanently or not
				final RunnableWithResult<Boolean> runnable = new RunnableWithResult<Boolean>() {
					@Override
					public void run() {
						final boolean confirmEngineSwitch = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Configuration change needed",
								"The currently selected script engine does not support debugging. However an alternative engine is available. Do you want to debug your script using that alternative engine?");

						setResult(confirmEngineSwitch);
					}
				};

				Display.getDefault().syncExec(runnable);

				if (!runnable.getResult())
					// user does not want to switch engine
					return;

			} else {
				// giving up
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Launch error", "No debug engine available for \"" + resource + "\"");
					}
				});
				return;
			}
		}

		final IScriptEngine engine = engineDescription.createEngine();
		engine.setTerminateOnIdle(true);

		// initialize console
		final ScriptConsole console = ScriptConsole.create(engine.getName() + ": " + resource, engine);
		engine.setOutputStream(console.getOutputStream());
		engine.setErrorStream(console.getErrorStream());
		engine.setInputStream(console.getInputStream());

		// setup debugger
		if (ILaunchManager.DEBUG_MODE.equals(mode)) {

			setupDebugger(engine, configuration, launch);
		}

		// set startup parameters
		final String parameterString = configuration.getAttribute(LaunchConstants.STARTUP_PARAMETERS, "").trim();
		String[] parameters;
		if (!parameterString.isEmpty()) {
			parameters = parameterString.split("\\s+");

		} else
			parameters = new String[0];

		engine.setVariable("argv", parameters);

		// execute resource
		engine.executeAsync(resource);

		// start engine
		engine.schedule();
	}

	@Override
	protected ILaunchConfiguration createLaunchConfiguration(final IResource file, final String mode) throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_ID);

		final ILaunchConfigurationWorkingCopy configuration = type.newInstance(null, file.getName());
		configuration.setAttribute(LaunchConstants.FILE_LOCATION, ResourceTools.toAbsoluteLocation(file, null));

		// find a valid engine
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final Collection<EngineDescription> engines = scriptService.getScriptType(ResourceTools.toAbsoluteLocation(file, null)).getEngines();
		if (engines.isEmpty())
			// TODO use a better way to bail out and use the direct file launch
			throw new CoreException(Status.CANCEL_STATUS);

		configuration.setAttribute(LaunchConstants.SCRIPT_ENGINE, engines.iterator().next().getID());

		// by default a debug configuration should suspend on script startup
		if (ILaunchManager.DEBUG_MODE.equals(mode))
			configuration.setAttribute(LaunchConstants.SUSPEND_ON_STARTUP, true);

		// save and return new configuration
		configuration.doSave();

		return configuration;
	}

	@Override
	protected String getFileLocation(final ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LaunchConstants.FILE_LOCATION, "");
	}

	@Override
	protected String getLaunchConfigurationId() {
		return LAUNCH_CONFIGURATION_ID;
	}

	private void setupDebugger(final IScriptEngine engine, final ILaunchConfiguration configuration, final ILaunch launch) {
		if (engine instanceof IDebugEngine) {
			boolean suspendOnStartup = false;
			try {
				suspendOnStartup = configuration.getAttribute(LaunchConstants.SUSPEND_ON_STARTUP, false);
			} catch (final CoreException e) {
			}

			boolean suspendOnScriptLoad = false;
			try {
				suspendOnScriptLoad = configuration.getAttribute(LaunchConstants.SUSPEND_ON_SCRIPT_LOAD, false);
			} catch (final CoreException e) {
			}

			boolean showDynamicCode = false;
			try {
				showDynamicCode = configuration.getAttribute(LaunchConstants.DISPLAY_DYNAMIC_CODE, false);
			} catch (final CoreException e) {
			}

			((IDebugEngine) engine).setupDebugger(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		}
	}
}
