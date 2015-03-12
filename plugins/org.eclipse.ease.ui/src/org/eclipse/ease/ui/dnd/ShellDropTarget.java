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
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

/**
 * DND support for JavaScript shell. DND of plain text, files, resources and
 * IDevices is supported.
 */
public final class ShellDropTarget extends DropTargetAdapter {

	private static final String EXTENSION_DROP_HANDLER_ID = "org.eclipse.ease.ui.shell";
	private static final String DROP_HANDLER = "dropHandler";
	private static final String PARAMETER_CLASS = "class";

	private static Collection<IShellDropHandler> getDropTargetListeners() {
		Collection<IShellDropHandler> listeners = new HashSet<IShellDropHandler>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_DROP_HANDLER_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(DROP_HANDLER)) {
				// drop listener
				try {
					Object executable = e.createExecutableExtension(PARAMETER_CLASS);
					if (executable instanceof IShellDropHandler)
						listeners.add((IShellDropHandler) executable);

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
	private final IScriptEngineProvider fScriptEngineProvider;

	/**
	 * Add drop support for various objects. A drop will always be interpreted
	 * as <i>copy</i>, even if <i>move</i> was requested.
	 *
	 * @param parent
	 *            control accepting drops
	 * @param javaScriptShell
	 *            shell for DND action execution
	 */
	public static void addDropSupport(final Control parent, final IScriptEngineProvider engineProvider) {
		final DropTarget target = new DropTarget(parent, DND.DROP_COPY | DND.DROP_MOVE);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		target.addDropListener(new ShellDropTarget(engineProvider));
	}

	/**
	 * Constructor.
	 *
	 * @param scriptShell
	 *            shell for DND action execution
	 */
	private ShellDropTarget(final IScriptEngineProvider provider) {
		fScriptEngineProvider = provider;
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
		Object element = event.data;

		// unpack selections
		if (element instanceof IStructuredSelection)
			element = ((IStructuredSelection) event.data).toArray();

		// unpack arrays with a single element
		if ((element instanceof Object[]) && (((Object[]) element).length == 1))
			element = ((Object[]) element)[0];

		// unpack collections with a single element
		if ((element instanceof Collection<?>) && (((Collection<?>) element).size() == 1))
			element = ((Collection<?>) element).iterator().next();

		// first ask registered drop handlers
		Collection<IShellDropHandler> listeners = getDropTargetListeners();
		if (!listeners.isEmpty()) {
			for (IShellDropHandler listener : listeners) {
				if (listener.accepts(element)) {
					listener.performDrop(fScriptEngineProvider.getScriptEngine(), element);
					return;
				}
			}
		}

		// no drop processor found, try generic approaches
		if (element instanceof IResource[]) {
			// drop of IResources
			for (final IResource resource : (IResource[]) element)
				execute(resource);

		} else if (element instanceof String[]) {
			// drop of external files (eg. from explorer)
			for (final String path : (String[]) element)
				fScriptEngineProvider.getScriptEngine().executeAsync(
						"include('file:/" + new Path(path).toString() + "');");

		} else if (element instanceof String) {
			// drop of plain (multiline) text
			execute(element);

		} else if (element instanceof Object[]) {
			// drop of generic things
			for (final Object object : (Object[]) element)
				execute(object);

		} else if (element instanceof IStructuredSelection) {
			for (Object arrayElement : ((IStructuredSelection) element).toArray())
				execute(arrayElement);

		} else if (element instanceof Object)
			// don't think this happens as even single objects are dropped as
			// Object[]
			execute(element);
	}

	private void execute(final Object element) {
		if (element instanceof IResource)
			fScriptEngineProvider.getScriptEngine().executeAsync(
					"include('workspace:/" + ((IResource) element).getFullPath().toString() + "');");

		else
			fScriptEngineProvider.getScriptEngine().executeAsync(element);
	}
}
