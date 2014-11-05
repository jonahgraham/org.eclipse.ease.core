package org.eclipse.ease.debugging;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.debugging.events.IDebugEvent;

public abstract class AbstractScriptDebugger implements IEventProcessor, IExecutionListener {
	private EventDispatchJob fDispatcher;

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

}
