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
package org.eclipse.ease.ui.dnd;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

/**
 * DND support for JavaScript shell. DND of plain text, files, resources and IDevices is supported.
 */
public final class ShellDropTarget extends DropTargetAdapter {

	private static final String EXTENSION_DROP_HANDLER_ID = "org.eclipse.ease.ui.dropHandler";
	private static final String DROP_HANDLER = "dropHandler";
	private static final String PARAMETER_CLASS = "class";

	private static Collection<DropTargetListener> getDropTargetListeners() {
		Collection<DropTargetListener> listeners = new HashSet<DropTargetListener>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_DROP_HANDLER_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(DROP_HANDLER)) {
				// drop listener
				try {
					Object executable = e.createExecutableExtension(PARAMETER_CLASS);
					if (executable instanceof DropTargetListener)
						listeners.add((DropTargetListener) executable);

				} catch (CoreException e1) {
					Logger.logError("Invalid drop taret listener detected", e1);
				}
			}
		}

		return listeners;
	}

	/**
	 * JavaScript shell for DND execution.
	 */
	private final IScriptEngineProvider mScriptEngineProvider;

	/**
	 * Add drop support for various objects. A drop will always be interpreted as <i>copy</i>, even if <i>move</i> was requested.
	 *
	 * @param parent
	 *            control accepting drops
	 * @param javaScriptShell
	 *            shell for DND action execution
	 */
	public static void addDropSupport(final Control parent, final IScriptEngineProvider engineProvider) {
		final DropTarget target = new DropTarget(parent, DND.DROP_COPY | DND.DROP_MOVE);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance(), LocalSelectionTransfer.getTransfer() });
		target.addDropListener(new ShellDropTarget(engineProvider));
	}

	/**
	 * Constructor.
	 *
	 * @param scriptShell
	 *            shell for DND action execution
	 */
	private ShellDropTarget(final IScriptEngineProvider provider) {
		mScriptEngineProvider = provider;
	}

	@Override
	public void dragEnter(final DropTargetEvent event) {
		if ((event.detail == DND.DROP_MOVE) || (event.detail == DND.DROP_DEFAULT)) {
			if ((event.operations & DND.DROP_COPY) != 0)
				event.detail = DND.DROP_COPY;
			else
				event.detail = DND.DROP_NONE;
		}
	}

	@Override
	public void drop(final DropTargetEvent event) {
		Collection<DropTargetListener> listeners = getDropTargetListeners();
		if (!listeners.isEmpty()) {
			for (DropTargetListener listener : listeners) {
				try {
					listener.drop(event);
					return;
				} catch (RuntimeException e) {
					// listener does not handle event
				}
			}
		}

		if (event.data instanceof IScript[]) {
			// drop of scripts
			for (final IScript script : (IScript[]) event.data)
				execute(script);

		} else if (event.data instanceof IResource[]) {
			// drop of IResources
			for (final IResource resource : (IResource[]) event.data)
				execute(resource);

		} else if (event.data instanceof String[]) {
			// drop of external files (eg. from explorer)
			for (final String path : (String[]) event.data)
				mScriptEngineProvider.getScriptEngine().executeAsync("include('file:/" + new Path(path).toString() + "');");

		} else if (event.data instanceof String) {
			// drop of plain (multiline) text
			execute(event.data);

		} else if (event.data instanceof Object[]) {
			// drop of generic things
			for (final Object object : (Object[]) event.data)
				execute(object);

		} else if (event.data instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) event.data).toArray())
				execute(element);

		} else if (event.data instanceof Object)
			// don't think this happens as even single objects are dropped as Object[]
			execute(event.data);
	}

	private void execute(final Object element) {
		if (element instanceof IResource)
			mScriptEngineProvider.getScriptEngine().executeAsync("include('workspace:/" + ((IResource) element).getFullPath().toString() + "');");

		else if (element instanceof IScript)
			mScriptEngineProvider.getScriptEngine().executeAsync("include('script:/" + ((IScript) element).getPath() + "');");

		else
			mScriptEngineProvider.getScriptEngine().executeAsync(element);
	}
}
