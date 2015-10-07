/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *     Christian Pontesegger - refactoring, added tracing
 *******************************************************************************/
package org.eclipse.ease;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Global Logger implementation for EASE. Provides means to log errors, warnings, infos and to create trace output.
 */
public class Logger {

	/**
	 * Log an error.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            error message
	 */
	public static void error(final String pluginID, final String message) {
		error(pluginID, message, null);
	}

	/**
	 * Log an error.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            error message
	 * @param throwable
	 *            throwable to be added
	 */
	public static void error(final String pluginID, final String message, final Throwable throwable) {
		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, pluginID, message, throwable));
	}

	/**
	 * Log a warning.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            warning message
	 */
	public static void warning(final String pluginID, final String message) {
		error(pluginID, message, null);
	}

	/**
	 * Log a warning.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            warning message
	 * @param throwable
	 *            throwable to be added
	 */
	public static void warning(final String pluginID, final String message, final Throwable throwable) {
		Activator.getDefault().getLog().log(new Status(IStatus.WARNING, pluginID, message, throwable));
	}

	/**
	 * Log an info.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            info message
	 */
	public static void info(final String pluginID, final String message) {
		error(pluginID, message, null);
	}

	/**
	 * Log an info.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param message
	 *            info message
	 * @param throwable
	 *            throwable to be added
	 */
	public static void info(final String pluginID, final String message, final Throwable throwable) {
		Activator.getDefault().getLog().log(new Status(IStatus.INFO, pluginID, message, throwable));
	}

	/**
	 * Create trace output.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param enabled
	 *            enablement flag for tracing, typically points to a trace option flag
	 * @param title
	 *            trace message title
	 */
	public static void trace(final String pluginID, final boolean enabled, final String title) {
		trace(pluginID, enabled, title, null);
	}

	/**
	 * Create trace output.
	 *
	 * @param pluginID
	 *            origin plug-in ID
	 * @param enabled
	 *            enablement flag for tracing, typically points to a trace option flag
	 * @param title
	 *            trace message title
	 * @param details
	 *            detailed message, will be indented for better readability
	 */
	public static void trace(final String pluginID, final boolean enabled, final String title, final String details) {
		if (enabled) {
			System.out.println(pluginID + ": " + title);

			if (details != null)
				// indent detail description
				System.out.println(details.replace("\n", "\n\t"));
		}
	}
}
