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
 * Base class to be used for modules. Handles retrieval of script engine and environment module.
 */
public abstract class AbstractScriptModule implements IScriptModule {

	/** Script engine instance. */
	private IScriptEngine mEngine = null;

	/** Environment module instance. */
	private IEnvironment mEnvironment = null;

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		mEngine = engine;
		mEnvironment = environment;
	}

	/**
	 * Get the current script engine.
	 * 
	 * @return script engine
	 */
	public IScriptEngine getScriptEngine() {
		return mEngine;
	}

	/**
	 * Get the current environment module.
	 * 
	 * @return environment module
	 */
	protected IEnvironment getEnvironment() {
		return mEnvironment;
	}
}
