/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts.repository.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IScriptListener;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class UIIntegrationJob extends UIJob implements IScriptListener {
	private final Map<String, ScriptContributionFactory> fContributionFactories = new HashMap<String, ScriptContributionFactory>();

	private final RepositoryService fRepositoryService;

	private final Collection<IScript> fAddedScripts = new HashSet<IScript>();

	public UIIntegrationJob(final RepositoryService repositoryService) {
		super("Update script UI components");

		fRepositoryService = repositoryService;
		fRepositoryService.addScriptListener(this);
	}

	@Override
	public IStatus runInUIThread(final IProgressMonitor monitor) {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
			// we might get called before the workbench is loaded.
			// in that case delay execution until the workbench is ready
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

				@Override
				public void windowOpened(final IWorkbenchWindow window) {
				}

				@Override
				public void windowDeactivated(final IWorkbenchWindow window) {
				}

				@Override
				public void windowClosed(final IWorkbenchWindow window) {
				}

				@Override
				public void windowActivated(final IWorkbenchWindow window) {
					PlatformUI.getWorkbench().removeWindowListener(this);
					schedule();
				}
			});

		} else {
			Collection<IScript> scriptsToAdd;
			synchronized (this) {
				scriptsToAdd = new HashSet<IScript>(fAddedScripts);
				fAddedScripts.clear();
			}

			// update added scripts
			for (IScript script : scriptsToAdd) {
				updateContributionFactory(script, "toolbar");
				updateContributionFactory(script, "menu");
			}
		}

		if (!fAddedScripts.isEmpty())
			// TODO change fixed delay
			schedule(300);

		return Status.OK_STATUS;
	}

	private void updateContributionFactory(final IScript script, final String type) {
		String location = script.getParameters().get(type);
		if (location != null) {
			// map to a real location
			location = LocationHelper.toLocation(location);
			if (location != null) {
				location = type + ":" + location;

				// get contribution factory for toolbar
				ScriptContributionFactory contributionFactory = fContributionFactories.get(location);
				if (contributionFactory == null) {
					// create a contribution factory
					contributionFactory = new ScriptContributionFactory(location, null);
					fContributionFactories.put(location, contributionFactory);

					final IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
					menuService.addContributionFactory(contributionFactory);
				}

				// add script
				contributionFactory.addScript(script);

				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(location.substring(location.indexOf(":") + 1));
				if (view instanceof ViewPart) {
					if (view.getViewSite() != null) {
						// the view is already rendered, contributions will not be considered anymore
						final IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
						if (view.getViewSite().getActionBars().getToolBarManager() instanceof ContributionManager) {
							view.getViewSite().getActionBars().getToolBarManager().removeAll();
							menuService.populateContributionManager((ContributionManager) view.getViewSite().getActionBars().getToolBarManager(), location);
							view.getViewSite().getActionBars().updateActionBars();
						}
					}
				}

			}
		}
	}

	@Override
	public void notify(final ScriptRepositoryEvent event) {
		switch (event.getType()) {
		case ScriptRepositoryEvent.ADD:
			addScript(event.getScript());
			break;
		}

	}

	public synchronized void addScripts(final Collection<IScript> scripts) {
		fAddedScripts.addAll(scripts);
		schedule();
	}

	private synchronized void addScript(final IScript script) {
		fAddedScripts.add(script);
		schedule();
	}
}
