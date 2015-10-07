/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class Logger {

	/** Trace enablement for the script service. */
	public static final boolean TRACE_SCRIPT_SERVICE = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/scriptService"));

	/** Trace enablement for script engines. */
	public static final boolean TRACE_SCRIPT_ENGINE = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/scriptEngine"));

	/** Trace enablement for module wrappers. */
	public static final boolean TRACE_MODULE_WRAPPER = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/scriptService"));

	public static void logError(final String message) {
		logError(message, Activator.PLUGIN_ID);
	}

	public static void logError(final String message, final Throwable exception) {
		logError(message, Activator.PLUGIN_ID, exception);
	}

	public static void logError(final String message, final String pluginID) {
		Activator.getDefault().getLog().log(createErrorStatus(message, pluginID));
	}

	public static void logError(final String message, final String pluginID, final Throwable exception) {
		Activator.getDefault().getLog().log(createErrorStatus(message, pluginID, exception));
	}

	public static IStatus createErrorStatus(final String message, final String pluginID, final Throwable exception) {
		return createStatus(IStatus.ERROR, message, pluginID, exception);
	}

	public static IStatus createErrorStatus(final String message, final String pluginID) {
		return createStatus(IStatus.ERROR, message, pluginID, null);
	}

	public static IStatus createStatus(final int statusError, final String message, final String pluginID, final Throwable exception) {
		if (exception != null) {
			return new Status(statusError, pluginID, message, exception);
		} else {
			return new Status(statusError, pluginID, message);
		}
	}

	public static IStatus createWarningStatus(final String message, final String pluginID) {
		return createStatus(IStatus.WARNING, message, pluginID, null);
	}

	public static IStatus createWarningStatus(final String message, final String pluginID, final Throwable exception) {
		return createStatus(IStatus.WARNING, message, pluginID, exception);
	}

	public static void logWarning(final String message) {
		logWarning(message, Activator.PLUGIN_ID);
	}

	public static void logWarning(final String message, final String pluginID) {
		Activator.getDefault().getLog().log(createWarningStatus(message, pluginID));
	}

	public static void trace(final boolean enabled, final String message) {
		if (enabled)
			System.out.println(Activator.PLUGIN_ID + ": " + message);
	}
}
