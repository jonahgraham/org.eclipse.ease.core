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
package org.eclipse.ease.debugging;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.IDebuggerEvent;
import org.eclipse.ease.debugging.events.IModelRequest;

public class EventDispatchJob extends Job {

	private final List<IDebugEvent> fEvents = new ArrayList<IDebugEvent>();

	private boolean fTerminated = false;

	private final IEventProcessor fHost;

	private final IEventProcessor fDebugger;

	public EventDispatchJob(final IEventProcessor host, final IEventProcessor debugger) {
		super(debugger + " event dispatcher");

		fHost = host;
		fDebugger = debugger;

		setSystem(true);
	}

	public void addEvent(final IDebugEvent event) {
		synchronized (fEvents) {
			if (!fEvents.contains(event)) {
				// TODO use tracing for these sysouts
				// DEBUG print events
				if (event instanceof IDebuggerEvent)
					System.out.println("Debugger ---> " + event);

				else if (event instanceof IModelRequest)
					System.out.println("Target   ---> " + event);
				// end DEBUG

				fEvents.add(event);
				fEvents.notifyAll();
			}
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		while (!fTerminated) {
			// handle event
			if (!monitor.isCanceled()) {

				IDebugEvent event = null;
				synchronized (fEvents) {
					if (!fEvents.isEmpty())
						event = fEvents.remove(0);
				}

				if (event != null)
					handleEvent(event);

			} else
				terminate();

			// wait for new events
			// do this after handling events as we might get terminated during wait()
			synchronized (fEvents) {
				if (fEvents.isEmpty()) {
					try {
						fEvents.wait();
					} catch (final InterruptedException e) {
					}
				}
			}
		}

		return Status.OK_STATUS;
	}

	private void handleEvent(final IDebugEvent event) {

		// TODO use tracing for these sysouts
		// DEBUG print events
		if (event instanceof IDebuggerEvent)
			System.out.println("\t\t! Target  : " + event);

		else if (event instanceof IModelRequest)
			System.out.println("\t\t! Debugger: " + event);
		// end DEBUG

		// forward event handling to target
		if (event instanceof IDebuggerEvent)
			fHost.handleEvent(event);

		else if (event instanceof IModelRequest)
			fDebugger.handleEvent(event);

		else
			throw new RuntimeException("Unknown event detected: " + event);
	}

	public void terminate() {
		fTerminated = true;

		// wake up job
		synchronized (this) {
			notifyAll();
		}
	}
}
