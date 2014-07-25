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
package org.eclipse.ease.debug;

import org.eclipse.core.runtime.Platform;

/**
 * Store constant used to display or not trace
 */
public interface ITracingConstant {

	/**
	 * If true trace stuff about module wrapping
	 */
	public static final boolean MODULE_WRAPPER_TRACING = org.eclipse.ease.Activator.getDefault().isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/moduleWrapper"));

	/**
	 * If true trace stuff about Environment Module
	 */
	public static final boolean ENVIRONEMENT_MODULE_WRAPPER_TRACING = org.eclipse.ease.Activator.getDefault().isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/EnvironementmoduleWrapper"));
}
