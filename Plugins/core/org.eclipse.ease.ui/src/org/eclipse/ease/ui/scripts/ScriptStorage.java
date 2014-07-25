/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ui.preferences.PreferencesHelper;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ui.PlatformUI;

public abstract class ScriptStorage {

	public static ScriptStorage createStorage() {
		String location = PreferencesHelper.getScriptStorageLocation();

		if (location.startsWith("workspace://"))
			return new WorkspaceScriptStorage(location);

		return new FileScriptStorage(location);
	}

	private final String fLocation;

	protected ScriptStorage(final String location) {
		fLocation = location;
	}

	public boolean exists(final String name) {
		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
		return repositoryService.getScript(name) != null;
	}

	public boolean store(final String name, final String content) {
		Path path = new Path(name);
		if (createPath(path.removeLastSegments(1))) {
			if (createFile(path, content)) {
				// trigger repository update
				// TODO trigger update on changed location only
				final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
				repositoryService.update(false);

				return true;
			}
		}

		return false;
	}

	public String getLocation() {
		return fLocation;
	}

	protected abstract boolean createFile(Path path, String content);

	protected abstract boolean createPath(IPath path);
}
