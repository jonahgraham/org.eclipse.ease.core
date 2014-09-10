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
package org.eclipse.ease.applications;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;

public class RunHeadlessScript implements IApplication {

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		final Object object = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (object instanceof String[]) {
			final Map<String, Object> parameters = extractInputParameters((String[]) object);

			if (parameters != null) {

				// create workspace
				if (parameters.containsKey("workspace")) {

					final Location location = Platform.getInstanceLocation();
					// stick to the deprecated method as file.toURI().toURL() will not work on paths containing spaces
					final URL workspaceURL = new File(parameters.get("workspace").toString()).toURL();

					// check if workspace location has not been set yet (can be set only once!)
					if (!location.isSet()) {
						location.release();
						location.set(workspaceURL, true);

					} else if (!location.getURL().equals(workspaceURL))
						System.err.println("WARNING: Could not set the workspace as it is already set to \"" + location.getURL() + "\"");
				}

				// execute script
				if (parameters.containsKey("script")) {
					// find script engine
					EngineDescription engineDescription = null;
					final IScriptService scriptService = ScriptService.getInstance();

					if (parameters.containsKey("engine"))
						// locate engine by ID
						engineDescription = scriptService.getEngineByID(parameters.get("engine").toString());

					else {
						// locate engine by file extension
						final int pos = parameters.get("script").toString().lastIndexOf('.');
						if (pos != -1) {
							final String extension = parameters.get("script").toString().substring(pos + 1);
							final ScriptType scriptType = scriptService.getScriptType(extension);
							if (scriptType != null)
								engineDescription = scriptService.getEngine(scriptType.getName());
						}
					}

					if (engineDescription != null) {
						// create engine
						final IScriptEngine engine = engineDescription.createEngine();
						engine.setVariable("argv", ((List) parameters.get("args")).toArray(new String[0]));

						Object scriptObject = ResourceTools.resolveFile(parameters.get("script"), null, true);
						if (scriptObject == null)
							// no file available, try to include to resolve URIs
							scriptObject = "include(\"" + parameters.get("script") + "\")";

						final ScriptResult scriptResult = engine.executeAsync(scriptObject);
						engine.schedule();

						synchronized (scriptResult) {
							if (!scriptResult.isReady())
								scriptResult.wait();
						}

						if (scriptResult.hasException())
							return -1;

						final Object result = scriptResult.getResult();
						if (result != null) {
							try {
								return Integer.parseInt(result.toString());
							} catch (final Exception e) {
								// no integer
							}

							try {
								return new Double(Double.parseDouble(result.toString())).intValue();
							} catch (final Exception e) {
								// no double
							}

							try {
								return Boolean.parseBoolean(result.toString()) ? 0 : -1;
							} catch (final Exception e) {
								// no boolean
							}

							// we do not know the return type, but typically parseBoolean() will deal with anything you throw at it
						} else
							return 0;
					}
				}

				System.err.println("ERROR: Could not access file \"" + parameters.get("script") + "\"");

			} else
				// could not extract parameters
				printUsage();
		}

		return -1;
	}

	private static Map<String, Object> extractInputParameters(final String[] arguments) {
		final Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("args", new ArrayList<String>());

		for (int index = 0; index < arguments.length; index++) {
			if (parameters.containsKey("script")) {
				// script arguments
				((List) parameters.get("args")).add(arguments[index]);

			} else if ("-script".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("script", arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: script name is missing");
					return null;
				}

			} else if ("-workspace".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("workspace", arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: workspace location is missing");
					return null;
				}

			} else if ("-engine".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("engine", arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: workspace location is missing");
					return null;
				}

			} else if ("-help".equals(arguments[index])) {
				return null;

			} else {
				System.out.println("ERROR: invalid args (" + arguments[index] + ")");
				return null;
			}
		}

		return parameters;
	}

	private static void printUsage() {
		System.out.println("SYNTAX: [-workspace <workspace location>] [-engine <engineID>]-script <script name> <script parameters>");
		System.out.println("");
		System.out.println("\t\t<script name> is a path like 'file://C/myfolder/myscript.js'");
		System.out.println("\t\t<engineID> provides a dedicated script engine ID. Use org.eclipse.ease.listEngines application.");
		System.out.println("\t\t<workspace location> is a file system path like 'C:\\somefolder\\myworkspace'");
		System.out.println("\t\t\tif you provide a workspace you can use workspace:// identifiers in your scripts");
		System.out.println("\t\t<script parameters> will be passed to the script as String[] in the variable 'argv'");
	}

	@Override
	public void stop() {
	}
}
