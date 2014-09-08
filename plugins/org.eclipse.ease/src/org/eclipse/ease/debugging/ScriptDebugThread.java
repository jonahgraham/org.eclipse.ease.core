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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ease.debugging.events.ResumeRequest;

public class ScriptDebugThread extends ScriptDebugElement implements IThread {

	private final Thread fThread;

	private State fState = State.NOT_STARTED;

	private List<ScriptDebugStackFrame> fStackFrames = new ArrayList<ScriptDebugStackFrame>();

	public ScriptDebugThread(final ScriptDebugTarget target, final Thread thread) {
		super(target);

		fThread = thread;
	}

	@Override
	public String getName() throws DebugException {
		return "Thread: " + fThread.getName();
	}

	@Override
	public synchronized IStackFrame[] getStackFrames() {
		return fStackFrames.toArray(new IStackFrame[fStackFrames.size()]);
	}

	@Override
	public synchronized boolean hasStackFrames() {
		return getStackFrames().length > 0;
	}

	@Override
	public synchronized ScriptDebugStackFrame getTopStackFrame() {
		if(hasStackFrames())
			return fStackFrames.get(0);

		return null;
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		return new IBreakpoint[0];
	}

	public Thread getThread() {
		return fThread;
	}

	@Override
	public boolean isTerminated() {
		return State.TERMINATED == fState;
	}

	@Override
	public boolean isSuspended() {
		return State.SUSPENDED == fState;
	}

	@Override
	public boolean isStepping() {
		return State.STEPPING == fState;
	}

	protected void setTerminated() {
		fState = State.TERMINATED;
		fireTerminateEvent();
	}

	protected void setSuspended(final int type) {
		fState = State.SUSPENDED;
		fireSuspendEvent(type);
	}

	protected void setResumed(final int type) {
		fState = State.RESUMED;
		fireResumeEvent(type);
	}

	public synchronized void setStackFrames(final List<IScriptDebugFrame> debugFrames) {
		// update stack frames
		final List<ScriptDebugStackFrame> newStackFrames = new ArrayList<ScriptDebugStackFrame>(debugFrames.size());
		for(final IScriptDebugFrame debugFrame : debugFrames) {
			// find existing StackFrame
			ScriptDebugStackFrame stackFrame = null;
			for(final ScriptDebugStackFrame oldStackFrame : fStackFrames) {
				if(debugFrame.equals(oldStackFrame.getDebugFrame())) {
					stackFrame = oldStackFrame;
					stackFrame.setDirty();
					break;
				}
			}

			if(stackFrame == null)
				stackFrame = new ScriptDebugStackFrame(this, debugFrame);

			newStackFrames.add(stackFrame);
		}

		fStackFrames = newStackFrames;
		fireChangeEvent(DebugEvent.CHANGE);
	}

	// ************************************************************
	// IStep
	// ************************************************************

	@Override
	public boolean canStepInto() {
		return isSuspended();
	}

	@Override
	public boolean canStepOver() {
		return isSuspended();
	}

	@Override
	public boolean canStepReturn() {
		return isSuspended();
	}

	@Override
	public void stepInto() throws DebugException {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_INTO, getThread()));
	}

	@Override
	public void stepOver() throws DebugException {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_OVER, getThread()));
	}

	@Override
	public void stepReturn() throws DebugException {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_RETURN, getThread()));
	}

}
