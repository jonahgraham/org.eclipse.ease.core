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
package org.eclipse.ease.ui.perspectives;

import org.eclipse.ease.ui.view.ModuleExplorerView;
import org.eclipse.ease.ui.view.ScriptExplorerView;
import org.eclipse.ease.ui.view.ScriptShell;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * Default perspective for scripting.
 */
public class ScriptingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout factory) {

		IFolderLayout left = factory.createFolder("left", IPageLayout.LEFT, 0.25f, factory.getEditorArea());
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		left.addView(ScriptExplorerView.VIEW_ID);

		IFolderLayout top = factory.createFolder("top", IPageLayout.TOP, 0.3f, factory.getEditorArea());
		top.addView(ScriptShell.VIEW_ID);

		IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.RIGHT, 0.8f, ScriptShell.VIEW_ID);
		topRight.addView(ModuleExplorerView.VIEW_ID);

		IFolderLayout bottomRight = factory.createFolder("bottom", IPageLayout.BOTTOM, 0.7f, factory.getEditorArea());
		bottomRight.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		bottomRight.addView(IPageLayout.ID_PROBLEM_VIEW);

		factory.addShowViewShortcut(ScriptExplorerView.VIEW_ID);
		factory.addShowViewShortcut(ScriptShell.VIEW_ID);
		factory.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}
}
