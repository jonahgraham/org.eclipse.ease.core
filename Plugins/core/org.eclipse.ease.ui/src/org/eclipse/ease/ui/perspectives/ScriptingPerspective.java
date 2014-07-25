/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * This class is meant to serve as an example for how various contributions are made to a perspective. Note that some of the extension point id's are referred
 * to as API constants while others are hardcoded and may be subject to change.
 */
public class ScriptingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout factory) {

		IFolderLayout left = factory.createFolder("left", IPageLayout.LEFT, 0.25f, factory.getEditorArea());
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		left.addView("org.eclipse.ease.ui.view.ScriptEplorerView");

		IFolderLayout top = factory.createFolder("top", IPageLayout.TOP, 0.3f, factory.getEditorArea());
		top.addView("org.eclipse.ease.views.scriptShell");

		IFolderLayout bottomRight = factory.createFolder("bottom", IPageLayout.BOTTOM, 0.7f, factory.getEditorArea());
		bottomRight.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		bottomRight.addView(IPageLayout.ID_PROBLEM_VIEW);

		factory.addShowViewShortcut("org.eclipse.ease.ui.view.ScriptEplorerView");
		factory.addShowViewShortcut("org.eclipse.ease.views.scriptShell");
		factory.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}
}
