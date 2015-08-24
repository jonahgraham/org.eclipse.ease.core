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
package org.eclipse.ease.ui.scripts.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.StringTools;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.ease.ui.scripts.ScriptEditorInput;
import org.eclipse.ease.ui.scripts.ScriptStorage;
import org.eclipse.ease.ui.scripts.dialogs.SelectScriptStorageDialog;
import org.eclipse.ease.ui.scripts.preferences.PreferencesHelper;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.tools.ToggleHandler;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.menus.UIElement;

/**
 * Toggle script recording command. Start/stop script recording.
 */
public class ToggleScriptRecording extends ToggleHandler implements IHandler, IElementUpdater, IExecutionListener {

	private boolean fChecked = false;

	private static final Map<IScriptEngine, StringBuffer> fRecordings = new HashMap<IScriptEngine, StringBuffer>();

	@Override
	protected final void executeToggle(final ExecutionEvent event, final boolean checked) {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof IScriptEngineProvider) {
			final IScriptEngine engine = ((IScriptEngineProvider) part).getScriptEngine();

			if (engine != null) {
				if (checked) {
					// start recording, eventually overrides a running recording
					// of the same provider
					fRecordings.put(engine, new StringBuffer());
					engine.addExecutionListener(this);

				} else {
					// stop recording
					final StringBuffer buffer = fRecordings.get(engine);

					if (buffer.length() > 0) {
						// script data is available
						String name = "recorded script";

						final ScriptStorage storage = getStorage();
						if (storage != null) {
							// ask for script name
							final InputDialog dialog = new InputDialog(HandlerUtil.getActiveShell(event),
									"Save Script", "Enter a unique name for your script (use '/' as path delimiter)",
									"", new IInputValidator() {

								@Override
								public String isValid(final String name) {
									if (storage.exists(name))
										return "Script name <" + name
														+ "> is already in use. Choose a different one.";

									return null;
								}
							});

							if (dialog.open() == Window.OK)
								name = dialog.getValue();
						}

						EngineDescription description = engine.getDescription();
						ScriptType scriptType = description.getSupportedScriptTypes().iterator().next();

						String fileName = name + "." + scriptType.getDefaultExtension();

						// write script header
						Map<String, String> header = new HashMap<String, String>();
						header.put("name", new Path(name).makeRelative().toString());
						header.put("description", "Script recorded by user.");
						header.put("script-type", scriptType.getName());
						header.put("author", System.getProperty("user.name"));
						header.put("date-recorded", new SimpleDateFormat("yyyy-MM-dd, HH:mm").format(new Date()));

						buffer.insert(0, "\n");
						buffer.insert(0, scriptType.getCodeParser().createHeader(header));

						if (storage != null) {
							// store script
							if (!storage.store(fileName, buffer.toString()))
								// could not store script
								MessageDialog.openError(HandlerUtil.getActiveShell(event), "Save error",
										"Could not store script data");

							// TODO update script repository

						} else {
							// we do not have a storage, open script in editor
							// and let user decide, what to do
							IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								IEditorDescriptor editor = IDE
										.getDefaultEditor(ResourcesPlugin.getWorkspace().getRoot()
												.getFile(new Path("/sample/foo." + scriptType.getDefaultExtension())));
								IEditorPart openEditor = IDE.openEditor(page,
										new ScriptEditorInput(name, buffer.toString()), editor.getId());
								// the editor starts indicating it is not dirty,
								// so ask the user to perform a save as action
								openEditor.doSaveAs();

							} catch (PartInitException e) {
								Logger.logError("Could not open editor for recorded script.", e);
							}
						}
					}
				}
			}
		}

		fChecked = checked;
	}

	private ScriptStorage getStorage() {
		// if no default storage is selected, ask the user for the correct
		// location
		if (PreferencesHelper.getUserScriptStorageLocation() == null) {

			// user did not select a storage yet, ask for location
			SelectScriptStorageDialog dialog = new SelectScriptStorageDialog(Display.getDefault().getActiveShell());
			if (dialog.open() == Window.OK) {
				final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(
						IRepositoryService.class);
				repositoryService.addLocation(dialog.getLocation(), true, true);
			}

			else
				return null;
		}

		// FIXME seems awkward! what are we doing here if we return a const
		// anyway
		return ScriptStorage.createStorage();
	}

	@Override
	public final void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
		super.updateElement(element, parameters);

		if (fChecked)
			element.setIcon(org.eclipse.ease.ui.Activator.getImageDescriptor(Activator.PLUGIN_ID,
					"icons/elcl16/stop_script_recording.png"));

		else
			element.setIcon(org.eclipse.ease.ui.Activator.getImageDescriptor(Activator.PLUGIN_ID,
					"icons/elcl16/start_script_recording.png"));
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		if (IExecutionListener.SCRIPT_END == status) {
			try {
				final StringBuffer buffer = fRecordings.get(engine);
				if (buffer != null) {
					// TODO add support to add trailing returns and ;
					buffer.append(script.getCode());
					buffer.append(StringTools.LINE_DELIMITER);
				} else
					engine.removeExecutionListener(this);

			} catch (final FileNotFoundException e) {
				// cannot record / execute macro when file is not found
			} catch (final CoreException e) {
				// cannot record / execute macro when file is not found
			} catch (final IOException e) {
				// cannot extract string from getCode()
			} catch (final Exception e) {
				// TODO handle this exception (but for now, at least know it
				// happened)
				throw new RuntimeException(e);
			}
		}
	}
}
