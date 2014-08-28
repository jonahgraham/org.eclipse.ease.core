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
package org.eclipse.ease.ui.scripts.repository;

import java.util.Collection;

import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.repository.IScriptLocation;

/**
 * Global service to register user scripts and to query for registered scripts. To get the service instance use
 *
 * <pre>
 * final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
 * </pre>
 */
public interface IRepositoryService {

	/**
	 * Trigger an immediate refresh of all script sources and contained scripts.
	 */
	void update(boolean force);

	/**
	 * Get all scripts registered with this service
	 * 
	 * @return collection of scripts
	 */
	Collection<IScript> getScripts();

	/**
	 * Add listener to get notified on script events. Events are triggered for scripts added, deleted or modified.
	 * 
	 * @param listener
	 *            listener to be registered
	 */
	void addScriptListener(IScriptListener listener);

	/**
	 * Remove a script event listener.
	 * 
	 * @param listener
	 *            listener to be removed
	 */
	void removeScriptListener(IScriptListener listener);

	/**
	 * Get a script by providing its full name.
	 *
	 * @param name
	 *            full name of script (including path)
	 * @return script instance
	 */
	IScript getScript(String name);

	Collection<IScriptLocation> getLocations();

	void updateLocations();

	IScriptLocation getDefaultLocation();
}
