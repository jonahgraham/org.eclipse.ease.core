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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.ui.LocationImageDescriptor;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IScriptListener;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class UIIntegrationJob extends UIJob implements IScriptListener {
	private static final String KEYWORD_POPUP = "popup";

	private static final String KEYWORD_MENU = "menu";

	private static final String KEYWORD_TOOLBAR = "toolbar";

	public static final String POPUP_LOCATION = "org.eclipse.ui.popup.any?after=additions";

	private class LocationDescription {
		public String fScheme;
		public String fViewID;
		public final String fName;

		public LocationDescription(final String scheme, final String entry) {
			fScheme = scheme;

			if (KEYWORD_POPUP.equals(fScheme)) {
				// general popup menu
				fViewID = "";
				fName = null;

			} else {
				// menu or toolbar
				String locationID;
				if (entry.contains("|")) {
					fName = entry.substring(entry.indexOf('|') + 1).trim();
					locationID = entry.substring(0, entry.indexOf('|')).trim();

				} else {
					fName = null;
					locationID = entry.trim();
				}

				// try to find a view with matching ID or matching title
				final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.views");
				for (final IConfigurationElement e : config) {
					if ("view".equals(e.getName())) {
						String id = e.getAttribute("id");
						if (id.equals(locationID)) {
							fViewID = id;
							return;
						}

						String name = e.getAttribute("name");
						if (name.equals(locationID)) {
							fViewID = id;
							return;
						}
					}
				}
			}
		}

		public String getId() {
			return fScheme + ":" + ((KEYWORD_POPUP.equals(fScheme)) ? POPUP_LOCATION : fViewID);
		}
	}

	private final Map<String, ScriptContributionFactory> fContributionFactories = new HashMap<String, ScriptContributionFactory>();

	private final Collection<IScript> fAddedScripts = Collections.synchronizedCollection(new HashSet<IScript>());
	private final Collection<IScript> fRemovedScripts = Collections.synchronizedCollection(new HashSet<IScript>());
	private final Map<IScript, ParameterDelta> fChangedScripts = Collections.synchronizedMap(new HashMap<IScript, ParameterDelta>());

	public UIIntegrationJob(final RepositoryService repositoryService) {
		super("Update script UI components");

		fAddedScripts.addAll(repositoryService.getScripts());
		repositoryService.addScriptListener(this);

		if (!fAddedScripts.isEmpty())
			// TODO change fixed delay
			schedule(300);
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

			// add new scripts to UI
			while (!fAddedScripts.isEmpty()) {
				IScript script = fAddedScripts.iterator().next();
				fAddedScripts.remove(script);

				handleAdditions(script);
			}

			// remove scripts from UI
			while (!fRemovedScripts.isEmpty()) {
				IScript script = fRemovedScripts.iterator().next();
				fRemovedScripts.remove(script);

				handleRemoval(script);
			}

			// // update scripts in UI
			while (!fChangedScripts.isEmpty()) {
				Entry<IScript, ParameterDelta> pair = fChangedScripts.entrySet().iterator().next();
				fChangedScripts.remove(pair.getKey());

				handleParameterChange(pair.getKey(), pair.getValue());
			}
		}

		return Status.OK_STATUS;
	}

	private void handleAdditions(final IScript script) {
		for (String scheme : new String[] { KEYWORD_TOOLBAR, KEYWORD_MENU, KEYWORD_POPUP }) {
			if (script.getParameters().containsKey(scheme)) {
				LocationDescription location = new LocationDescription(scheme, script.getParameters().get(scheme));
				addViewContribution(location, script);
			}
		}
	}

	private void handleRemoval(final IScript script) {
		for (String scheme : new String[] { KEYWORD_TOOLBAR, KEYWORD_MENU, KEYWORD_POPUP }) {
			if (script.getParameters().containsKey(scheme)) {
				LocationDescription location = new LocationDescription(scheme, script.getParameters().get(scheme));
				removeViewContribution(location, script);
			}
		}
	}

	private void handleParameterChange(final IScript script, final ParameterDelta parameterDelta) {
		for (String scheme : new String[] { KEYWORD_TOOLBAR, KEYWORD_MENU, KEYWORD_POPUP }) {
			modifyViewContribution(scheme, script, parameterDelta);
		}
	}

	private void modifyViewContribution(final String scheme, final IScript script, final ParameterDelta parameterDelta) {

		if (parameterDelta.isRemoved(scheme) || parameterDelta.isModified(scheme)) {
			// remove from old contribution
			LocationDescription oldLocation = new LocationDescription(scheme, parameterDelta.getOldParameter(scheme));
			removeViewContribution(oldLocation, script);
		}

		if (parameterDelta.isAdded(scheme) || parameterDelta.isModified(scheme)) {
			// add to new contribution
			LocationDescription newLocation = new LocationDescription(scheme, script.getParameters().get(scheme));
			addViewContribution(newLocation, script);

		} else if (script.getParameters().containsKey(scheme) && (parameterDelta.isAffected("name") || parameterDelta.isAffected("image"))) {
			// possibly name changed (depends on specific name element for toolbar entry)
			LocationDescription location = new LocationDescription(scheme, script.getParameters().get(scheme));
			if (location.fName == null) {
				// name derived from script property "name", refresh label

				IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(location.fViewID);
				if ((view instanceof ViewPart) && (view.getViewSite() != null)) {
					// view is already rendered

					IContributionItem[] contributions;
					if (KEYWORD_TOOLBAR.equals(location.fScheme))
						contributions = view.getViewSite().getActionBars().getToolBarManager().getItems();
					else if (KEYWORD_MENU.equals(location.fScheme))
						contributions = view.getViewSite().getActionBars().getMenuManager().getItems();
					else
						contributions = new IContributionItem[0];

					for (IContributionItem item : contributions) {
						if ((item instanceof ScriptContributionItem) && (item.getId().equals(script.getLocation()))) {
							((ScriptContributionItem) item).setLabel(script.getParameters().get("name"));
							((ScriptContributionItem) item).setIcon(LocationImageDescriptor.createFromLocation(script.getParameters().get("image")));
						}
					}

					view.getViewSite().getActionBars().updateActionBars();
				}
			}
		}
	}

	private void addViewContribution(final LocationDescription location, final IScript script) {

		// update contribution
		getContributionFactory(location.getId()).addScript(script);

		if (location.fViewID != null) {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(location.fViewID);

			if ((view instanceof ViewPart) && (view.getViewSite() != null)) {
				// the view is already rendered, contributions will not be considered anymore so add item directly
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=452203 for details
				if (location.fScheme.equals(KEYWORD_TOOLBAR)) {
					getContributionFactory(location.getId()).setAffectedContribution(view.getViewSite().getActionBars().getToolBarManager());
					view.getViewSite().getActionBars().getToolBarManager().add(new ScriptContributionItem(script));
				} else if (location.fScheme.equals(KEYWORD_MENU)) {
					getContributionFactory(location.getId()).setAffectedContribution(view.getViewSite().getActionBars().getMenuManager());
					view.getViewSite().getActionBars().getMenuManager().add(new ScriptContributionItem(script));
				}

				view.getViewSite().getActionBars().updateActionBars();
			}
		}
	}

	private void removeViewContribution(final LocationDescription location, final IScript script) {

		// update contribution
		getContributionFactory(location.getId()).removeScript(script);

		if (location.fViewID != null) {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(location.fViewID);
			if ((view instanceof ViewPart) && (view.getViewSite() != null)) {
				// the view is already rendered, contributions will not be considered anymore so remove item directly
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=452203 for details
				if (location.fScheme.equals(KEYWORD_TOOLBAR))
					view.getViewSite().getActionBars().getToolBarManager().remove(script.getLocation());
				else if (location.fScheme.equals(KEYWORD_MENU))
					view.getViewSite().getActionBars().getMenuManager().remove(script.getLocation());

				view.getViewSite().getActionBars().updateActionBars();
			}
		}
	}

	private ScriptContributionFactory getContributionFactory(final String location) {
		if (!fContributionFactories.containsKey(location))
			fContributionFactories.put(location, new ScriptContributionFactory(location, null));

		return fContributionFactories.get(location);
	}

	@Override
	public void notify(final ScriptRepositoryEvent event) {
		Map<String, String> parameters = event.getScript().getParameters();
		if (parameters.containsKey(KEYWORD_MENU) || parameters.containsKey(KEYWORD_TOOLBAR) || parameters.containsKey(KEYWORD_POPUP)
				|| (event.getType() == ScriptRepositoryEvent.PARAMETER_CHANGE)) {
			// script with UI integration
			switch (event.getType()) {
			case ScriptRepositoryEvent.ADD:
				fAddedScripts.add(event.getScript());
				schedule(300);
				break;

			case ScriptRepositoryEvent.DELETE:
				fRemovedScripts.add(event.getScript());
				schedule(300);
				break;

			case ScriptRepositoryEvent.PARAMETER_CHANGE:
				ParameterDelta delta = (ParameterDelta) event.getEventData();
				if (delta.isAffected(KEYWORD_TOOLBAR) || delta.isAffected(KEYWORD_MENU) || delta.isAffected(KEYWORD_POPUP) || delta.isAffected("name")
						|| delta.isAffected("image")) {
					// we need to adapt the appearance
					fChangedScripts.put(event.getScript(), delta);
					schedule(300);
				}
				break;
			}
		}
	}
}
