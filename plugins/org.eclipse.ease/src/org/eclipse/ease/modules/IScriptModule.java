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
package org.eclipse.ease.modules;

import org.eclipse.ease.IScriptEngine;

/**
 * Interface which may be implemented by script modules. If a module implements this interface, the script engine and environment will register themselves.
 * Implementing this interface is optional for a module.
 */
public interface IScriptModule {
	/**
	 * Provides script engine and environment instances.
	 * 
	 * @param engine
	 *            script engine this module is loaded in
	 * @param environment
	 *            environment module that tracks this module
	 */
	void initialize(IScriptEngine engine, IEnvironment environment);
}
