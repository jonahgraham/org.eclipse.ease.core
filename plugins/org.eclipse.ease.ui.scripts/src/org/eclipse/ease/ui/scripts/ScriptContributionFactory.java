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
package org.eclipse.ease.ui.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.scripts.ui.ScriptPopup;
import org.eclipse.ease.ui.scripts.ui.ScriptPopupMenu;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Factory adding scripts to dynamically populated menu.
 */
public final class ScriptContributionFactory extends CompoundContributionItem implements IWorkbenchContribution
//, IScriptListener 
{

	private IServiceLocator fServiceLocator;
	private boolean fDirty = true;

	@Override
	public void initialize(final IServiceLocator serviceLocator) {
		fServiceLocator = serviceLocator;
//		FIXME needs replacement
//		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
//		if (repositoryService != null)
//			repositoryService.addScriptListener(this);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> contributions = new ArrayList<IContributionItem>();

		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);

		if (repositoryService != null) {
			final List<IScript> scripts = new ArrayList<IScript>(repositoryService.getScripts());

			Collections.sort(scripts, new Comparator<IScript>() {
				@Override
				public int compare(final IScript o1, final IScript o2) {
					IPath path1 = o1.getPath();
					IPath path2 = o2.getPath();
					if (path1.isEmpty() && !path2.isEmpty())
						return 1;

					else if (!path1.isEmpty() && path2.isEmpty())
						return -1;

					else
						return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});

			final Map<IPath, ScriptPopupMenu> dynamicMenus = new HashMap<IPath, ScriptPopupMenu>();

			List<IContributionItem> rootItems = new ArrayList<IContributionItem>();
			for (final IScript script : scripts) {
				final ScriptPopup popup = new ScriptPopup(script);

				final IPath path = script.getPath().removeLastSegments(1);
				if (path.lastSegment() == null) {
					// script in root folder
					rootItems.add(popup.getContribution(fServiceLocator));

				} else {
					// script in sub menu
					ScriptPopupMenu menu = registerPath(dynamicMenus, path);
					menu.addItem(popup);
				}
			}

			// add root menus to additions
			for (Entry<IPath, ScriptPopupMenu> element : dynamicMenus.entrySet()) {
				if (element.getKey().segmentCount() == 1)
					contributions.add(element.getValue().getContribution(fServiceLocator));
			}

			// add root elements to additions
			for (IContributionItem item : rootItems)
				contributions.add(item);

			fDirty = false;
		}

		return contributions.toArray(new IContributionItem[contributions.size()]);
	}

	@Override
	public boolean isDirty() {
		// FIXME seems that root scripts on the first run will not be populated correctly. While added to the contributions they are not visible for some reason
		// until fixed, caching is disabled
		return true;
	}

	@Override
	public void dispose() {

//		FIXME needs replacement
//		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
//		if (repositoryService != null)
//			repositoryService.removeScriptListener(this);

		fDirty = false;

		super.dispose();
	}

//		FIXME needs replacement
//	@Override
//	public void notify(final ScriptEvent event) {
//		fDirty = true;
//	}

	private static ScriptPopupMenu registerPath(final Map<IPath, ScriptPopupMenu> dynamicMenus, final IPath path) {
		if (!dynamicMenus.containsKey(path)) {
			dynamicMenus.put(path, new ScriptPopupMenu(path.lastSegment()));

			if (path.segmentCount() > 1) {
				IPath parent = path.removeLastSegments(1);
				ScriptPopupMenu parentMenu = registerPath(dynamicMenus, parent);
				parentMenu.addItem(dynamicMenus.get(path));
			}
		}

		return dynamicMenus.get(path);
	}
}
