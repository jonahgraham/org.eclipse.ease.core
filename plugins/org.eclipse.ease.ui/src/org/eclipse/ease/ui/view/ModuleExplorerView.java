/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesComposite;
import org.eclipse.ease.ui.modules.ui.ModulesFilter;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ModuleExplorerView extends ViewPart implements
		IPreferenceChangeListener {

	public static final String ID = "org.eclipse.ease.ui.view.ModulesExplorerView"; //$NON-NLS-1$
	private ModulesComposite fModulesComposite;

	public ModuleExplorerView() {

	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		fModulesComposite = new ModulesComposite(parent, SWT.NONE, false);
		fModulesComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		fModulesComposite.addFilter(ModulesFilter.visible(fModulesComposite));

		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(fModulesComposite
				.getTreeViewer().getTree());
		getSite().registerContextMenu(menuManager,
				fModulesComposite.getTreeViewer());
		fModulesComposite.getTreeViewer().getTree().setMenu(menu);

		getSite().setSelectionProvider(fModulesComposite.getTreeViewer());

		((IEclipsePreferences) InstanceScope.INSTANCE.getNode(
				org.eclipse.ease.Activator.PLUGIN_ID).node("modules"))
				.addPreferenceChangeListener(this);

		final IScriptService scriptService = (IScriptService) PlatformUI
				.getWorkbench().getService(IScriptService.class);
		List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(
				scriptService.getAvailableModules().values());
		fModulesComposite.setInput(modules);

	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void dispose() {

		((IEclipsePreferences) InstanceScope.INSTANCE.getNode(
				Activator.PLUGIN_ID).node("modules"))
				.removePreferenceChangeListener(this);

	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getNode().name().equals("modules")) {
			fModulesComposite.refresh();

		}

	}

	public IContentProvider getContentProvider() {
		return fModulesComposite.getTreeViewer().getContentProvider();

	}

}
