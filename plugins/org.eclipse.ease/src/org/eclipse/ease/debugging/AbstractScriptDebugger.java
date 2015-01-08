package org.eclipse.ease.debugging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.events.BreakpointRequest;
import org.eclipse.ease.debugging.events.EngineStartedEvent;
import org.eclipse.ease.debugging.events.EngineTerminatedEvent;
import org.eclipse.ease.debugging.events.GetStackFramesRequest;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.ResumeRequest;
import org.eclipse.ease.debugging.events.ResumedEvent;
import org.eclipse.ease.debugging.events.ScriptReadyEvent;
import org.eclipse.ease.debugging.events.StackFramesEvent;
import org.eclipse.ease.debugging.events.SuspendedEvent;

public abstract class AbstractScriptDebugger implements IEventProcessor, IExecutionListener {
	private EventDispatchJob fDispatcher;

	private IScriptEngine fEngine;

	private boolean fSuspended = false;

	private final Map<Script, List<IBreakpoint>> fBreakpoints = new HashMap<Script, List<IBreakpoint>>();

	private final boolean fShowDynamicCode;

	private int fResumeType;

	private List<IScriptDebugFrame> fStacktrace = new LinkedList<IScriptDebugFrame>();

	private int fResumeStackSize = 0;

	public AbstractScriptDebugger(final IScriptEngine engine, final boolean showDynamicCode) {
		fEngine = engine;
		fShowDynamicCode = showDynamicCode;

		fEngine.addExecutionListener(this);
	}

	/**
	 * Setter method for dispatcher.
	 *
	 * @param dispatcher
	 *            dispatcher for communication between debugger and debug target.
	 */
	public void setDispatcher(final EventDispatchJob dispatcher) {
		fDispatcher = dispatcher;
	}

	/**
	 * Helper method to raise event via dispatcher.
	 *
	 * @param event
	 *            Debug event to be raised.
	 */
	protected void fireDispatchEvent(final IDebugEvent event) {
		synchronized (fDispatcher) {
			if (fDispatcher != null)
				fDispatcher.addEvent(event);
		}
	}

	protected void suspend(final IDebugEvent event) {

		synchronized (fEngine) {
			// need to fire event in synchronized code to avoid getting a resume event too soon
			fSuspended = true;
			fireDispatchEvent(event);

			try {
				while (fSuspended)
					fEngine.wait();

			} catch (final InterruptedException e) {
				fSuspended = false;
			}

			fireDispatchEvent(new ResumedEvent(Thread.currentThread(), getResumeType()));
		}
	}

	protected void resume(final int resumeType) {
		// UNSPECIFIED is sent by the debug target if execution is resumed automatically, so stay with last user resume request
		if (resumeType != DebugEvent.UNSPECIFIED) {
			fResumeType = resumeType;
			fResumeStackSize = getStacktrace().size();
		}

		synchronized (fEngine) {
			fSuspended = false;
			fEngine.notifyAll();
		}
	}

	protected IScriptEngine getEngine() {
		return fEngine;
	}

	/**
	 * Notify function called by Eclipse EASE framework.
	 *
	 * Raises according events depending on status
	 */
	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		switch (status) {
		case ENGINE_START:
			fireDispatchEvent(new EngineStartedEvent());
			break;

		case ENGINE_END:
			fireDispatchEvent(new EngineTerminatedEvent());

			// allow for garbage collection
			fEngine.removeExecutionListener(this);
			fEngine = null;
			break;

		case SCRIPT_START:
			// fall through
		case SCRIPT_INJECTION_START:
			// new script
			if (isTrackedScript(script))
				suspend(new ScriptReadyEvent(script, Thread.currentThread(), fStacktrace.isEmpty()));

			break;

		case SCRIPT_END:
			// fall through
		case SCRIPT_INJECTION_END:
			// TODO remove script from stack
			break;

		default:
			// unknown event
			break;
		}
	}

	@Override
	public void handleEvent(final IDebugEvent event) {
		if (event instanceof ResumeRequest) {
			resume(((ResumeRequest) event).getType());

		} else if (event instanceof BreakpointRequest) {
			final Script script = ((BreakpointRequest) event).getScript();
			if (!fBreakpoints.containsKey(script))
				fBreakpoints.put(script, new ArrayList<IBreakpoint>());

			if (((BreakpointRequest) event).getMode() == BreakpointRequest.Mode.ADD)
				fBreakpoints.get(script).add(((BreakpointRequest) event).getBreakpoint());
			else
				fBreakpoints.get(script).remove(((BreakpointRequest) event).getBreakpoint());

		} else if (event instanceof GetStackFramesRequest) {
			fireDispatchEvent(new StackFramesEvent(getStacktrace(), ((AbstractScriptEngine) fEngine).getThread()));
		}
	}

	/**
	 * Get a breakpoint for a given line in a script.
	 *
	 * @param script
	 *            script to look for
	 * @param lineNumber
	 *            line number within script to check
	 * @return {@link IBreakpoint} instance or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint(final Script script, final int lineNumber) {
		final List<IBreakpoint> breakpoints = fBreakpoints.get(script);
		if (breakpoints != null) {
			for (final IBreakpoint breakpoint : breakpoints) {
				try {
					if (breakpoint.isEnabled()) {
						final int breakLocation = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
						if (lineNumber == breakLocation)
							return breakpoint;
					}
				} catch (final CoreException e) {
					// cannot check enabled state, ignore
				}
			}
		}

		return null;
	}

	protected boolean isTrackedScript(final Script script) {
		return !script.isDynamic() || fShowDynamicCode;
	}

	protected int getResumeType() {
		return fResumeType;
	}

	public List<IScriptDebugFrame> getStacktrace() {
		return fStacktrace;
	}

	protected void setStacktrace(final List<IScriptDebugFrame> stacktrace) {
		fStacktrace = stacktrace;
	}

	/**
	 * Called by the debug instance on a line change. Checks for suspend conditions and suspends if necessary.
	 *
	 * @param script
	 * @param lineNumber
	 */
	protected void processLine(final Script script, final int lineNumber) {

		// check breakpoints
		final IBreakpoint breakpoint = getBreakpoint(script, lineNumber);
		if (breakpoint != null) {
			suspend(new SuspendedEvent(DebugEvent.BREAKPOINT, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));
			return;
		}

		// no breakpoint, check for step events
		switch (getResumeType()) {
		case DebugEvent.STEP_INTO:
			if (fResumeStackSize <= getStacktrace().size())
				suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));

			break;

		case DebugEvent.STEP_OVER:
			if (fResumeStackSize >= getStacktrace().size())
				suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));

			break;

		case DebugEvent.STEP_RETURN:
			if (fResumeStackSize > getStacktrace().size())
				suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));

			break;

		default:
			// either user did not request anything yet or "RESUME" was triggered
		}
	}
}
