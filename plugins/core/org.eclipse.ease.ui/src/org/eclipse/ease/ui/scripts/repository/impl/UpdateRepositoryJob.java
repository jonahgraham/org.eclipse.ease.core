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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

	private final Map<String, ScriptContributionFactory> fContributionFactories = new HashMap<String, ScriptContributionFactory>();

	public UpdateRepositoryJob(final RepositoryService repositoryService) {
		super("Updating script repository");

		fRepositoryService = repositoryService;
		// fRepositoryService.addScriptListener(this);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		// mark script to be verified
		for (IScript script : fRepositoryService.getScripts())
			script.setUpdatePending(true);

		// get locations to be updated
		Collection<IScriptLocation> entries = new HashSet<IScriptLocation>();
		synchronized (fEntriesforUpdate) {
			if (fEntriesforUpdate.isEmpty())
				entries.addAll(fRepositoryService.getRepository().getEntries());
			else {
				entries.addAll(fEntriesforUpdate);
				fEntriesforUpdate.clear();
			}
		}

		// update locations
		for (IScriptLocation entry : entries) {

			Object content = entry.getResource();
			if ((content instanceof IResource) && (((IResource) content).exists())) {
				// this is a valid workspace resource
				new WorkspaceParser(fRepositoryService).parse((IResource) content, entry);
				ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

			} else if ((content instanceof File) && (((File) content).exists())) {
				// this is a valid file system resource
				new FileSystemParser(fRepositoryService).parse((File) content, entry);

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

	private synchronized void update(final IScriptLocation entry) {
		fEntriesforUpdate.add(entry);
		scheduleUpdate(0);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		// TODO handle resource changes
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {

				@Override
				public boolean visit(final IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					String location = "workspace:/" + resource.getFullPath();
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

	// @Override
	// public void notify(ScriptRepositoryEvent event) {
	// switch (event.getType()) {
	// case ScriptRepositoryEvent.ADD:
	// if ((event.getScript().getParameters().get("menu") != null) || (event.getScript().getParameters().get("toolbar") != null)) {
	// // to be added
	//
	// }
	// break;
	//
	// case ScriptRepositoryEvent.DELETE:
	// if ((event.getScript().getParameters().get("menu") != null) || (event.getScript().getParameters().get("toolbar") != null)) {
	// // to be deleted
	// }
	// break;
	//
	// case ScriptRepositoryEvent.PARAMETER_CHANGE:
	// Map<String, String> parameters = (Map<String, String>) event.getEventData();
	// if ((parameters.containsKey("menu")) || (parameters.containsKey("toolbar"))) {
	// // to be changed
	// }
	// break;
	//
	// default:
	// break;
	// }
	// }
}