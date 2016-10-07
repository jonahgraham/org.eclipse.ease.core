/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.tools;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Helper class to run a job when the workbench window is started. If the workbench is not ready yet, execution will be delayed until the workbench is ready.
 */
public abstract class AbstractWorkbenchRunnable implements Runnable, IWindowListener {

	/**
	 * Launches the run() method as soon as the workbench is ready. Execution will always be moved to the UI thread and is done asynchronously. This call simply
	 * registers the execution task and returns immediately.
	 */
	public void launch() {
		if ((PlatformUI.isWorkbenchRunning()) && (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null))
			Display.getDefault().asyncExec(this);

		else if ((PlatformUI.isWorkbenchRunning()) && (Display.getCurrent() != null)) {
			// this is the display thread, but the workbench is not loaded yet
			PlatformUI.getWorkbench().addWindowListener(this);

		} else {
			// we were not running in display thread or workbench is not ready yet, delegate to display thread and try again
			Display.getDefault().asyncExec(() -> {
				launch();
			});
		}
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		PlatformUI.getWorkbench().removeWindowListener(this);

		run();
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		// nothing to do
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		// nothing to do
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		// nothing to do
	}
}
