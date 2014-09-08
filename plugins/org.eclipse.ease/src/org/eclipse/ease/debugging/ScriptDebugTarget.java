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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.events.BreakpointRequest;
import org.eclipse.ease.debugging.events.EngineStartedEvent;
import org.eclipse.ease.debugging.events.EngineTerminatedEvent;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.ResumeRequest;
import org.eclipse.ease.debugging.events.ResumedEvent;
import org.eclipse.ease.debugging.events.ScriptReadyEvent;
import org.eclipse.ease.debugging.events.StackFramesEvent;
import org.eclipse.ease.debugging.events.SuspendedEvent;
import org.eclipse.ease.debugging.events.TerminateRequest;

public abstract class ScriptDebugTarget extends ScriptDebugElement implements IDebugTarget, IEventProcessor {

	private EventDispatchJob fDispatcher;

	private ScriptDebugProcess fProcess = null;

	private final List<ScriptDebugThread> fThreads = new ArrayList<ScriptDebugThread>();

	private final ILaunch fLaunch;

	private State fState = State.NOT_STARTED;

	private final boolean fSuspendOnStartup;

	private final boolean fSuspendOnScriptLoad;

	public ScriptDebugTarget(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad) {
		super(null);
		fLaunch = launch;
		fSuspendOnStartup = suspendOnStartup;
		fSuspendOnScriptLoad = suspendOnScriptLoad;

		fireCreationEvent();
	}

	@Override
	public String getName() throws DebugException {
		return "EASE Debugger";
	}

	@Override
	public ScriptDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public IProcess getProcess() {
		return fProcess;
	}

	@Override
	public ScriptDebugThread[] getThreads() {
		return fThreads.toArray(new ScriptDebugThread[fThreads.size()]);
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return !fThreads.isEmpty();
	}

	@Override
	public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDispatcher(final EventDispatchJob dispatcher) {
		fDispatcher = dispatcher;
	}

	protected void fireDispatchEvent(final IDebugEvent event) {
		fDispatcher.addEvent(event);
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************

	@Override
	public void handleEvent(final IDebugEvent event) {
		if (event instanceof EngineStartedEvent) {
			fProcess = new ScriptDebugProcess(this);
			fProcess.fireCreationEvent();

		} else if (event instanceof ScriptReadyEvent) {
			// find existing DebugThread
			ScriptDebugThread debugThread = findDebugThread(((ScriptReadyEvent) event).getThread());

			if (debugThread == null) {
				// thread does not exist, create new one
				debugThread = new ScriptDebugThread(getDebugTarget(), ((ScriptReadyEvent) event).getThread());
				fThreads.add(debugThread);

				debugThread.fireCreationEvent();
			}

			// add new stack frame
			debugThread.fireChangeEvent(DebugEvent.CONTENT);

			// set deferred breakpoints
			setDeferredBreakpoints(((ScriptReadyEvent) event).getScript());

			// tell framework we are suspended
			fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
			debugThread.setSuspended(DebugEvent.CLIENT_REQUEST);

			if (fSuspendOnScriptLoad)
				// suspend on any script load event
				fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_INTO, debugThread.getThread()));

			else if ((((ScriptReadyEvent) event).isRoot()) && (fSuspendOnStartup))
				// suspend on script startup event
				fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_INTO, debugThread.getThread()));

			else
				// immediately resume execution
				fireDispatchEvent(new ResumeRequest(DebugEvent.RESUME, debugThread.getThread()));

		} else if (event instanceof StackFramesEvent) {
			// stackframe refresh
			final ScriptDebugThread debugThread = findDebugThread(((StackFramesEvent) event).getThread());
			debugThread.setStackFrames(((StackFramesEvent) event).getDebugFrames());

		} else if (event instanceof ResumedEvent) {
			final ScriptDebugThread debugThread = findDebugThread(((ResumedEvent) event).getThread());
			debugThread.setResumed(((ResumedEvent) event).getType());

		} else if (event instanceof SuspendedEvent) {
			final ScriptDebugThread debugThread = findDebugThread(((SuspendedEvent) event).getThread());
			debugThread.setStackFrames(((SuspendedEvent) event).getDebugFrames());
			debugThread.setSuspended(((SuspendedEvent) event).getType());

		} else if (event instanceof EngineTerminatedEvent) {
			fState = State.TERMINATED;
			fireTerminateEvent();
			for (final ScriptDebugThread thread : getThreads())
				thread.setTerminated();
		}
	}

	private ScriptDebugThread findDebugThread(final Thread thread) {
		for (final ScriptDebugThread debugThread : getThreads()) {
			if (thread.equals(debugThread.getThread()))
				return debugThread;
		}

		return null;
	}

	private void setDeferredBreakpoints(final Script script) {

		final Object file = script.getFile();
		if (file instanceof IResource) {
			final IBreakpoint[] breakpoints = getBreakpoints(script);

			for (final IBreakpoint breakpoint : breakpoints) {
				if (file.equals(breakpoint.getMarker().getResource()))
					fireDispatchEvent(new BreakpointRequest(script, breakpoint));
			}
		}
	}

	protected abstract IBreakpoint[] getBreakpoints(Script script);

	// ************************************************************
	// IBreakpointListener
	// ************************************************************

	@Override
	public void breakpointAdded(final IBreakpoint breakpoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	// ************************************************************
	// IMemoryBlockRetrieval
	// ************************************************************

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
		// FIXME add correct plugin id
		throw new DebugException(new Status(IStatus.ERROR, "Activator.PLUGIN_ID", "getMemoryBlock() not supported by " + getName()));
	}

	// ************************************************************
	// ITerminate
	// ************************************************************

	@Override
	public void terminate() throws DebugException {
		fireDispatchEvent(new TerminateRequest());
	}

	@Override
	public boolean isTerminated() {
		return State.TERMINATED == fState;
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public boolean isSuspended() {
		final ScriptDebugThread[] threads = getThreads();
		if (threads.length == 1)
			threads[0].isSuspended();

		return false;
	}

	@Override
	public void resume() throws DebugException {
		final ScriptDebugThread[] threads = getThreads();
		if (threads.length == 1)
			fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, threads[0].getThread()));
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public void disconnect() throws DebugException {
		// TODO remove all breakpoints

		fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, null));
		fState = State.TERMINATED;
		fireTerminateEvent();
	}

	// ************************************************************
	// IStep
	// ************************************************************

	@Override
	public boolean isStepping() {
		final ScriptDebugThread[] threads = getThreads();
		if (threads.length == 1)
			threads[0].isStepping();

		return false;
	}
}
