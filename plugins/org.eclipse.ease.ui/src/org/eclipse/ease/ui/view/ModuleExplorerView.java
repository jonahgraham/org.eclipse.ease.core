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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.help.hovers.HoverManager;
import org.eclipse.ease.ui.help.hovers.IHoverContentProvider;
import org.eclipse.ease.ui.help.hovers.ModuleHelp;
import org.eclipse.ease.ui.modules.ui.ModulesComposite;
import org.eclipse.ease.ui.modules.ui.ModulesContentProvider;
import org.eclipse.ease.ui.modules.ui.ModulesFilter;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class ModuleExplorerView extends ViewPart implements IPreferenceChangeListener {

	public static final String VIEW_ID = "org.eclipse.ease.ui.views.modulesExplorer"; //$NON-NLS-1$

	private static final String SEARCH_DEFAULT_TEXT = "<search modules>";

	/**
	 * Job to update the tree filter settings. Decoupled to trigger after some delay.
	 */
	private class UpdateTreeJob extends UIJob {

		/** Default delay until UI update job is triggered. */
		private static final long UI_DELAY_MS = 300L;

		private String fFilterText;

		public UpdateTreeJob() {
			super("Update Modules Explorer");
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			for (final ViewerFilter filter : fModulesComposite.getTreeViewer().getFilters()) {
				if (filter instanceof TextViewerFilter) {
					((TextViewerFilter) filter).setFilter(fFilterText);

					fModulesComposite.getTreeViewer().refresh();

					if ((fFilterText != null) && (!fFilterText.isEmpty()))
						fModulesComposite.getTreeViewer().expandAll();

					return Status.OK_STATUS;
				}
			}

			// filter not set yet
			fModulesComposite.getTreeViewer().addFilter(new TextViewerFilter(fFilterText));

			if ((fFilterText != null) && (!fFilterText.isEmpty()))
				fModulesComposite.getTreeViewer().expandAll();

			return Status.OK_STATUS;
		}

		/**
		 * Called when filter text is to be updated.
		 *
		 * @param filterText
		 *            new filter text
		 */
		public synchronized void update(final String filterText) {
			fFilterText = filterText;

			schedule(UI_DELAY_MS);
		}
	}

	/**
	 * Viewer filter to display only elements that contain a certain text. Also displays parents when they contain a child element that contains the specified
	 * filter text.
	 */
	private class TextViewerFilter extends ViewerFilter {
		private String fFilterText;

		public TextViewerFilter(final String filterText) {
			fFilterText = filterText;
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
			if ((fFilterText == null) || (fFilterText.isEmpty()) || (isVisible(element)))
				return true;

			return isChildVisible(element);
		}

		/**
		 * Detect a child element that contains specified filter text.
		 *
		 * @param element
		 *            parent element
		 * @return <code>true</code> when filtertext is detected
		 */
		private boolean isChildVisible(final Object element) {
			final ITreeContentProvider contentProvider = (ITreeContentProvider) fModulesComposite.getTreeViewer().getContentProvider();
			for (final Object child : contentProvider.getChildren(element)) {
				if (isVisible(child) || isChildVisible(child))
					return true;
			}

			return false;
		}

		/**
		 * Detect filter text in element representation.
		 *
		 * @param element
		 *            element to look at
		 * @return <code>true</code> when filtertext is detected
		 */
		private boolean isVisible(final Object element) {
			final ILabelProvider labelProvider = (ILabelProvider) fModulesComposite.getTreeViewer().getLabelProvider();
			final String label = labelProvider.getText(element);
			return (label.toLowerCase().contains(fFilterText));
		}

		private void setFilter(final String filterText) {
			fFilterText = filterText;
		}
	}

	private ModulesComposite fModulesComposite;
	private Text txtSearch;

	private UpdateTreeJob fUpdateJob = null;

	/**
	 * Create contents of the view part.
	 *
	 * @param parent
	 */
	@Override
	public void createPartControl(final Composite parent) {
		final GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 3;
		gl_parent.marginWidth = 0;
		gl_parent.marginHeight = 0;
		gl_parent.horizontalSpacing = 0;
		parent.setLayout(gl_parent);

		txtSearch = new Text(parent, SWT.BORDER);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent e) {
				if (fUpdateJob == null)
					fUpdateJob = new UpdateTreeJob();

				fUpdateJob.update(txtSearch.getText().toLowerCase());
			}
		});
		txtSearch.setText(SEARCH_DEFAULT_TEXT);
		txtSearch.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(final FocusEvent e) {
				super.focusGained(e);

				if (SEARCH_DEFAULT_TEXT.equals(txtSearch.getText()))
					txtSearch.setText("");
			}

			@Override
			public void focusLost(final FocusEvent e) {
				if (txtSearch.getText().isEmpty())
					txtSearch.setText(SEARCH_DEFAULT_TEXT);

				super.focusLost(e);
			}
		});

		fModulesComposite = new ModulesComposite(parent, SWT.NONE, false);
		fModulesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		fModulesComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

		fModulesComposite.addFilter(ModulesFilter.visible((ModulesContentProvider) fModulesComposite.getTreeViewer().getContentProvider()));

		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(fModulesComposite.getTreeViewer().getTree());
		getSite().registerContextMenu(menuManager, fModulesComposite.getTreeViewer());
		fModulesComposite.getTreeViewer().getTree().setMenu(menu);

		final HoverManager hoverManager = new HoverManager(parent);
		hoverManager.addHover(fModulesComposite.getTreeViewer(), new IHoverContentProvider() {

			@Override
			public void populateToolbar(BrowserInformationControl control, ToolBarManager toolBarManager) {
				// nothing to do
			}

			@Override
			public String getContent(Object origin, Object detail) {
				if (detail instanceof ModuleEntry<?>)
					detail = ((ModuleEntry) detail).getEntry();

				if (detail instanceof ModuleDefinition)
					return ModuleHelp.getModuleHelpTip((ModuleDefinition) detail);

				if (detail instanceof Method)
					return ModuleHelp.getMethodHelpTip((Method) detail);

				if (detail instanceof Field)
					return ModuleHelp.getConstantHelpTip((Field) detail);

				return null;
			}
		});

		getSite().setSelectionProvider(fModulesComposite.getTreeViewer());

		// listen for preference changes on visible modules
		((IEclipsePreferences) InstanceScope.INSTANCE.getNode(org.eclipse.ease.Activator.PLUGIN_ID).node("modules")).addPreferenceChangeListener(this);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		((IEclipsePreferences) InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node("modules")).removePreferenceChangeListener(this);
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent event) {
		if (event.getNode().name().equals("modules"))
			fModulesComposite.refresh();
	}

	public IContentProvider getContentProvider() {
		return fModulesComposite.getTreeViewer().getContentProvider();
	}
}
