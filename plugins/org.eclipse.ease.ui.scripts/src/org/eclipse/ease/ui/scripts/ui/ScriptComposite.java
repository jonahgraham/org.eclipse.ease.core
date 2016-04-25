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
package org.eclipse.ease.ui.scripts.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * SWT Composite that displays available macros. Implemented as a tree viewer.
 */
public class ScriptComposite extends Composite implements EventHandler {
	private final TreeViewer treeViewer;

	private IDoubleClickListener fDoubleClickListener = new IDoubleClickListener() {

		@Override
		public void doubleClick(final DoubleClickEvent event) {

			final Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();

			if ((element instanceof IScript) && (fEngineProvider != null)) {
				final IScriptEngine scriptEngine = fEngineProvider.getScriptEngine();
				if (scriptEngine != null)
					scriptEngine.executeAsync("include('script:/" + ((IScript) element).getPath() + "');");
			}
		}
	};

	private IScriptEngineProvider fEngineProvider = null;

	/**
	 * Constructor creating the script tree viewer.
	 *
	 * @param engineProvider
	 *            component providing script support
	 * @param site
	 *            site to implement this component on
	 * @param parent
	 *            parent SWT element
	 * @param style
	 *            composite style flags
	 */
	public ScriptComposite(final IScriptEngineProvider engineProvider, final IWorkbenchPartSite site, final Composite parent, final int style) {
		super(parent, style);
		fEngineProvider = engineProvider;

		setLayout(new FillLayout(SWT.HORIZONTAL));

		treeViewer = new TreeViewer(this, SWT.BORDER);

		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IPath)
					return ((IPath) element).lastSegment();

				if (element instanceof IScript)
					return ((IScript) element).getName();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				if (element instanceof IPath)
					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/folder.png", true);

				if (element instanceof IScript)
					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/script.png", true);

				return super.getImage(element);
			}
		});
		treeViewer.setContentProvider(new ScriptContentProvider());

		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(final Object element) {
				return (element instanceof IPath) ? 0 : 1;
			}
		});

		final IRepositoryService repositoryService = PlatformUI.getWorkbench().getService(IRepositoryService.class);
		treeViewer.setInput(repositoryService);

		if (fDoubleClickListener != null)
			treeViewer.addDoubleClickListener(fDoubleClickListener);

		// add context menu support
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);
		site.registerContextMenu(menuManager, treeViewer);
		site.setSelectionProvider(treeViewer);

		// add dynamic context menu entries
		final IMenuService menuService = PlatformUI.getWorkbench().getService(IMenuService.class);
		ScriptContextMenuEntries popupContributionFactory = new ScriptContextMenuEntries("popup:" + site.getId());
		menuService.addContributionFactory(popupContributionFactory);
		menuManager.setRemoveAllWhenShown(true);

		treeViewer.addSelectionChangedListener(popupContributionFactory);

		// add DND support
		ScriptDragSource.addDragSupport(treeViewer);

		// add listener for script additions/removals/renames
		IEventBroker fEventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		fEventBroker.subscribe(IRepositoryService.BROKER_CHANNEL_SCRIPTS_NEW, this);
		fEventBroker.subscribe(IRepositoryService.BROKER_CHANNEL_SCRIPTS_REMOVED, this);
		fEventBroker.subscribe(IRepositoryService.BROKER_CHANNEL_SCRIPT_KEYWORDS + "name", this);
	}

	// TODO change this filter to scripttype
	public void setEngine(final String engineID) {
		treeViewer.setFilters(new ViewerFilter[] { new ScriptEngineFilter(engineID) });
	}

	@Override
	public void dispose() {
		IEventBroker fEventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		fEventBroker.unsubscribe(this);

		super.dispose();
	}

	public void setDoubleClickListener(final IDoubleClickListener doubleClickListener) {
		if ((fDoubleClickListener != null) && (treeViewer != null))
			treeViewer.removeDoubleClickListener(fDoubleClickListener);

		fDoubleClickListener = doubleClickListener;

		if ((fDoubleClickListener != null) && (treeViewer != null))
			treeViewer.addDoubleClickListener(fDoubleClickListener);
	}

	@Override
	public void handleEvent(final Event event) {
		// FIXME needs some performance improvements on multiple script
		// updates
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				treeViewer.refresh();
			}
		});
	}
}
