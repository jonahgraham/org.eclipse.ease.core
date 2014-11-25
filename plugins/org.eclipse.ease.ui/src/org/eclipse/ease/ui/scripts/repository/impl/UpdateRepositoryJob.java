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
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.repository.IScriptLocation;

public class UpdateRepositoryJob extends Job implements IResourceChangeListener
// , IScriptListener
{

	private final RepositoryService fRepositoryService;

	private final Collection<IScriptLocation> fEntriesforUpdate = new HashSet<IScriptLocation>();

	public UpdateRepositoryJob(final RepositoryService repositoryService) {
		super("Updating script repository");

		fRepositoryService = repositoryService;
		// fRepositoryService.addScriptListener(this);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		// get locations to be updated
		Collection<IScriptLocation> locations = new HashSet<IScriptLocation>();
		synchronized (fEntriesforUpdate) {
			if (fEntriesforUpdate.isEmpty())
				locations.addAll(fRepositoryService.getRepository().getEntries());
			else {
				locations.addAll(fEntriesforUpdate);
				fEntriesforUpdate.clear();
			}
		}

		// mark scripts to be verified
		for (IScriptLocation location : locations) {
			for (IScript script : location.getScripts())
				script.setUpdatePending(true);
		}

		// update locations
		for (IScriptLocation location : locations) {

			Object content = location.getResource();
			if ((content instanceof IResource) && (((IResource) content).exists())) {
				// this is a valid workspace resource
				new WorkspaceParser(fRepositoryService).parse((IResource) content, location);
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

			} else if ((content instanceof File) && (((File) content).exists())) {
				// this is a valid file system resource
				new FileSystemParser(fRepositoryService).parse((File) content, location);

			} else if (content instanceof InputStream) {
				// FIXME can never happen call entry.getInputStream() instead
				// new InputStreamParser(fRepositoryService).parse(stream, entry);
				// entry.setTimestamp(System.currentTimeMillis());
			}
		}

		// remove scripts that were not verified
		for (IScript script : new HashSet<IScript>(fRepositoryService.getScripts())) {
			if (script.isUpdatePending())
				fRepositoryService.removeScript(script);
		}

		// save new state
		fRepositoryService.save();

		// re schedule job
		// TODO make this editable by preferences
		schedule(1000 * 60 * 30);
		return Status.OK_STATUS;
	}

	public void scheduleUpdate(final long delay) {
		cancel();
		schedule(delay);
	}

	synchronized void update(final IScriptLocation entry) {
		fEntriesforUpdate.add(entry);
		scheduleUpdate(300);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		System.out.println("Base: " + ((event.getResource() != null) ? event.getResource().getName() : "null"));
		// TODO handle resource changes
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {

				@Override
				public boolean visit(final IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					String location = "workspace:/" + resource.getFullPath();
					System.out.println(location + ": " + delta.getKind());
					for (IScriptLocation entry : fRepositoryService.getLocations()) {
						if (entry.getLocation().equals(location)) {
							update(entry);
							return false;
						}
					}

					return true;
				}
			});
		} catch (CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}
	}
}