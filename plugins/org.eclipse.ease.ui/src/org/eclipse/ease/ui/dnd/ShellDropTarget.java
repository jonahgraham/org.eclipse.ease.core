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

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
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
import org.eclipse.ui.part.ResourceTransfer;

/**
 * DND support for JavaScript shell. DND of plain text, files, resources and IDevices is supported.
 */
public final class ShellDropTarget extends DropTargetAdapter {

	private static final String EXTENSION_DROP_HANDLER_ID = "org.eclipse.ease.ui.shell";
	private static final String DROP_HANDLER = "dropHandler";
	private static final String PARAMETER_CLASS = "class";
	protected static final String ATTRIBUTE_PRIORITY = "priority";

	private static Collection<IShellDropHandler> getDropTargetListeners() {

		final List<AbstractMap.SimpleEntry<Integer, IShellDropHandler>> candidates = new ArrayList<AbstractMap.SimpleEntry<Integer, IShellDropHandler>>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_DROP_HANDLER_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(DROP_HANDLER)) {
				// drop handler
				// candidates.add(e);
				try {
					final Object executable = e.createExecutableExtension(PARAMETER_CLASS);
					if (executable instanceof IShellDropHandler) {
						int priority = 0;
						try {
							priority = Integer.parseInt(e.getAttribute(ATTRIBUTE_PRIORITY));
						} catch (final NumberFormatException e1) {
						} catch (final NullPointerException e1) {
						}

						candidates.add(new AbstractMap.SimpleEntry<Integer, IShellDropHandler>(priority, (IShellDropHandler) executable));
					}

				} catch (final CoreException e1) {

					Logger.error(Activator.PLUGIN_ID, "Invalid drop target listener detected in plugin \"" + e.getContributor().getName() + "\"", e1);
				}
			}
		}

		// sort handler by priority
		Collections.sort(candidates, new Comparator<AbstractMap.SimpleEntry<Integer, IShellDropHandler>>() {

			@Override
			public int compare(final AbstractMap.SimpleEntry<Integer, IShellDropHandler> e1, final AbstractMap.SimpleEntry<Integer, IShellDropHandler> e2) {
				return e2.getKey() - e1.getKey();
			}
		});

		final Collection<IShellDropHandler> handler = new ArrayList<IShellDropHandler>(candidates.size());
		for (final AbstractMap.SimpleEntry<Integer, IShellDropHandler> candidate : candidates)
			handler.add(candidate.getValue());

		return handler;
	}

	/**
	 * JavaScript shell for DND execution.
	 */
	private final IScriptEngineProvider fScriptEngineProvider;

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
		target.setTransfer(new Transfer[] { FileTransfer.getInstance(), ResourceTransfer.getInstance(), LocalSelectionTransfer.getTransfer(),
				TextTransfer.getInstance() });
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

		final Object element = unpackElement(event.data);

		// first ask registered drop handlers
		final Collection<IShellDropHandler> listeners = getDropTargetListeners();
		if (!listeners.isEmpty()) {

			for (final IShellDropHandler listener : listeners) {
				// 1st pass: try to drop unpacked dropped element and pass it to drop handler
				if (listener.accepts(fScriptEngineProvider.getScriptEngine(), element)) {
					listener.performDrop(fScriptEngineProvider.getScriptEngine(), element);
					return;
				}

				if (!event.data.equals(element)) {
					// 2nd pass: no listener found for unwrapped object, try with original object
					if (listener.accepts(fScriptEngineProvider.getScriptEngine(), event.data)) {
						listener.performDrop(fScriptEngineProvider.getScriptEngine(), event.data);
						return;
					}
				}
			}
		}

		// no drop processor found, try generic approaches
		fScriptEngineProvider.getScriptEngine().executeAsync(element);
	}

	private Object unpackElement(Object element) {
		// look for file system files
		if (element instanceof String[]) {
			// drop of external files (eg. from explorer)
			final File[] files = new File[((String[]) element).length];
			for (int i = 0; i < files.length; i++)
				files[i] = new File(((String[]) element)[i]);

			element = files;
		}

		// unpack selections
		if (element instanceof IStructuredSelection)
			element = ((IStructuredSelection) element).toArray();

		// unpack arrays with a single element
		if ((element instanceof Object[]) && (((Object[]) element).length == 1))
			element = ((Object[]) element)[0];

		// unpack collections with a single element
		if ((element instanceof Collection<?>) && (((Collection<?>) element).size() == 1))
			element = ((Collection<?>) element).iterator().next();

		return element;
	}
}
