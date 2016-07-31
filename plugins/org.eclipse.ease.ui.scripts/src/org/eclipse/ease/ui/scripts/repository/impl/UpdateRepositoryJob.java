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
package org.eclipse.ease.ui.scripts.repository.impl;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IScriptLocation;
import org.eclipse.ease.ui.scripts.ui.Decorator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class UpdateRepositoryJob extends Job {

	private final RepositoryService fRepositoryService;

	public UpdateRepositoryJob(final RepositoryService repositoryService) {
		super("Updating script repository");

		fRepositoryService = repositoryService;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		final Collection<IScriptLocation> locations = fRepositoryService.getLocations();

		for (final IScriptLocation location : locations) {
			if (location.isUpdatePending()) {
				// mark scripts to be verified
				for (final IScript script : location.getScripts())
					script.setUpdatePending(true);

				// get location base resource
				final Object content = location.getResource();
				if ((content instanceof IResource) && (((IResource) content).exists())) {
					// this is a valid workspace resource
					new WorkspaceParser().parse((IResource) content, location);
					// ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

				} else if ((content instanceof File) && (((File) content).exists())) {
					// this is a valid file system resource
					new FileSystemParser().parse((File) content, location);

				} else if (content instanceof URI) {
					if ("platform".equals(((URI) content).getScheme())) {
						new PluginParser().parse(content, location);

					} else {
						try {
							new HttpParser().parse(((URI) content).toURL().toString(), location);
						} catch (final MalformedURLException e) {
							// not a valid URL, ignore repository
						}
					}

				} else if (content instanceof InputStream) {
					// FIXME can never happen call entry.getInputStream() instead
					// new InputStreamParser(fRepositoryService).parse(stream, entry);
					// entry.setTimestamp(System.currentTimeMillis());
				}

				// remove scripts that were not verified
				for (final IScript script : new HashSet<IScript>(location.getScripts())) {
					if (script.isUpdatePending())
						fRepositoryService.removeScript(script);
				}
			}
		}

		// save new state
		fRepositoryService.save();

		// update decorators
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update(Decorator.SIGN_DECORATOR_ID);
			}
		});

		// re schedule job
		// TODO make this editable by preferences
		schedule(1000 * 60 * 30);

		return Status.OK_STATUS;
	}

	synchronized void update(final IScriptLocation entry) {
		entry.setUpdatePending(true);

		if (getState() != Job.RUNNING)
			cancel();

		schedule(300);
	}

	synchronized void update() {
		for (final IScriptLocation location : fRepositoryService.getLocations())
			location.setUpdatePending(true);

		if (getState() != Job.RUNNING)
			cancel();

		schedule(300);
	}

	// FIXME does not work yet
	synchronized void update(final IScript script) {
		script.setUpdatePending(true);

		if (getState() != Job.RUNNING)
			cancel();

		schedule(300);
	}
}