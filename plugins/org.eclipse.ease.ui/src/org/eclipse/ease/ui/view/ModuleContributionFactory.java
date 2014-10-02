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
package org.eclipse.ease.ui.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.handler.LoadModule;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.ease.ui.tools.AbstractPopupItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.service.prefs.Preferences;

public final class ModuleContributionFactory extends AbstractContributionFactory {

	private static ModuleContributionFactory fInstance = null;

	/**
	 * Add context menu for these contribution items.
	 */
	public static void addContextMenu() {
		final IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
		menuService.addContributionFactory(getInstance());
	}

	/**
	 * Get instance of this factory.
	 *
	 * @return factory instance
	 */
	private static ModuleContributionFactory getInstance() {
		if (fInstance == null)
			fInstance = new ModuleContributionFactory();

		return fInstance;
	}

	/**
	 * Private constructor.
	 */
	private ModuleContributionFactory() {
		super("menu:" + LoadModule.COMMAND_ID + ".popup", null);
	}

	@Override
	public void createContributionItems(final IServiceLocator serviceLocator, final IContributionRoot additions) {

		IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		Map<String, ModuleDefinition> modules = scriptService.getAvailableModules();

		// read preferences for tree/list layout
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);
		boolean flatMode = prefs.getBoolean(IPreferenceConstants.SHELL_MODULES_AS_LIST, IPreferenceConstants.DEFAULT_SHELL_MODULES_AS_LIST);

		// create root menu (only used as a container while populating)
		HashMap<IPath, ModulePopupMenu> moduleTree = new HashMap<IPath, ModulePopupMenu>();
		ModulePopupMenu menu = new ModulePopupMenu("");
		moduleTree.put(new Path("/"), menu);

		for (final ModuleDefinition definition : modules.values()) {
			if (definition.isVisible()) {
				if (!flatMode) {
					// find correct menu for tree layout
					IPath path = definition.getPath();
					if (!moduleTree.containsKey(path.removeLastSegments(1)))
						createPath(moduleTree, path.removeLastSegments(1));

					menu = moduleTree.get(path.removeLastSegments(1));
				}

				menu.addEntry(new ModulePopupItem(definition));
			}
		}

		// sort all menus
		for (ModulePopupMenu popupMenu : moduleTree.values())
			popupMenu.sortEntries();

		// populate root contributions
		ModulePopupMenu root = moduleTree.get(new Path("/"));
		for (AbstractPopupItem item : root.getEntries())
			additions.addContributionItem(item.getContribution(serviceLocator), null);
	}

	private static ModulePopupMenu createPath(final Map<IPath, ModulePopupMenu> moduleTree, final IPath path) {
		if (!moduleTree.containsKey(path)) {
			ModulePopupMenu parentMenu = createPath(moduleTree, path.removeLastSegments(1));
			ModulePopupMenu menu = new ModulePopupMenu(path.lastSegment());

			parentMenu.addEntry(menu);
			moduleTree.put(path, menu);
			return menu;
		}

		return moduleTree.get(path);
	}
}
