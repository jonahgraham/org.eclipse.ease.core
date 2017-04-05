/*******************************************************************************
 * Copyright (c) 2014 Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.debugger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.AbstractScriptDebugger;
import org.eclipse.ease.debugging.IEventProcessor;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.TerminateRequest;

/**
 * Debugger class handling communication between Python and Eclipse.
 */
public class PythonDebugger extends AbstractScriptDebugger implements IEventProcessor, IExecutionListener {
	/**
	 * Variable name for {@link PythonDebugger} in Python engine.
	 * <p>
	 * During setup phase set this variable <b>BEFORE</b> calling edb.py
	 */
	public static final String PYTHON_DEBUGGER_VARIABLE = "_pyease_debugger";

	/**
	 * Custom {@link ScriptDebugFrame} parsing the data from {@link IPyFrame} to more usable format.
	 */
	public class PythonDebugFrame extends ScriptDebugFrame implements IScriptDebugFrame {
		/**
		 * Constructor parses information from {@link IPyFrame} to correct parameters for {@link ScriptDebugFrame#ScriptDebugFrame(Script, int, int)}.
		 *
		 * @param frame
		 *            {@link IPyFrame} with information about the current execution frame.
		 */
		public PythonDebugFrame(final IPyFrame frame) {
			super(fScriptRegistry.get(frame.getFilename()), frame.getLineNumber(), TYPE_FILE);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ease.debugging.ScriptDebugFrame#getName()
		 */
		@Override
		public String getName() {
			final Script script = getScript();
			if (script.isDynamic()) {
				// dynamic script
				final String title = getScript().getTitle();
				return (title != null) ? "Dynamic: " + title : "(Dynamic)";

			} else {
				final Object command = getScript().getCommand();
				if (command != null) {
					if (command instanceof IFile)
						return ((IFile) command).getName();

					else if (command instanceof File)
						return ((File) command).getName();

					return command.toString();
				}
			}

			return "(unknown source)";
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.ease.debugging.ScriptDebugFrame#getVariables()
		 */
		@Override
		public Map<String, Object> getVariables() {
			// Check to avoid NPE if shut down
			if (getEngine() != null) {
				return getEngine().getVariables();
			}
			return null;
		}
	}

	/**
	 * {@link ICodeTracer} for communicating with Python implementation.
	 */
	private ICodeTracer fCodeTracer;

	/**
	 * @see AbstractScriptDebugger#AbstractScriptDebugger(IScriptEngine, boolean)
	 */
	public PythonDebugger(final IScriptEngine engine, final boolean showDynamicCode) {
		super(engine, showDynamicCode);
	}

	/**
	 * Sets the {@link ICodeTracer} from the Python implementation.
	 * <p>
	 * This method will be called by edb.py on {@value #PYTHON_DEBUGGER_VARIABLE}.
	 *
	 * @param tracer
	 *            {@link ICodeTracer} for the connection between Eclipse and Python.
	 */
	public void setCodeTracer(final ICodeTracer tracer) {
		fCodeTracer = tracer;
	}

	/**
	 * Function called to handle incoming event.
	 *
	 * Depending on type corresponding handler will be called
	 */
	@Override
	public void handleEvent(final IDebugEvent event) {
		if (event instanceof TerminateRequest) {
			resume(DebugEvent.STEP_END);

		} else
			super.handleEvent(event);
	}

	/**
	 * Utility to check if a frame is part of user code or external library.
	 *
	 * @param frame
	 *            {@link IPyFrame} to check if we are currently in user code.
	 * @return <code>true</code> if we are in user code.
	 */
	private static boolean isUserCode(final IPyFrame frame) {
		return frame.getFilename().startsWith("__ref_");
	}

	/**
	 * Parses given frame for its call stack.
	 *
	 * @param origin
	 *            Top frame of stack.
	 * @return Stack based on given {@link IPyFrame}
	 */
	private List<IScriptDebugFrame> getStacktrace(final IPyFrame origin) {
		final List<IScriptDebugFrame> trace = new ArrayList<>();

		IPyFrame frame = origin;
		while (frame != null) {
			if (isUserCode(frame)) {
				if (isTrackedScript(fScriptRegistry.get(frame.getFilename())))
					trace.add(new PythonDebugFrame(frame));
			}

			frame = frame.getParent();
		}

		return trace;
	}

	/**
	 * Function called from {@link ICodeTracer} whenever a new frame in Python is hit.
	 * <p>
	 * Effectively checks if debugger should supsend or continue.
	 *
	 * @param frame
	 *            {@link IPyFrame} for current execution point.
	 * @param type
	 *            Type of trace step that occured (ignored).
	 */
	public void traceDispatch(final IPyFrame frame, final String type) {
		if (getResumeType() == DebugEvent.STEP_END)
			throw new ExitException("Debug aborted by user");

		if (isUserCode(frame)) {
			final Script script = fScriptRegistry.get(frame.getFilename());

			if (isTrackedScript(script)) {

				// update stacktrace
				setStacktrace(getStacktrace(frame));

				// do not process script load event (line == 0)
				if (frame.getLineNumber() != 0)
					processLine(script, frame.getLineNumber());
			}
		}
	}

	/**
	 * Runs the given {@link Script} using the {@link ICodeTracer}.
	 * <p>
	 * Return values are ignored in debug mode.
	 *
	 * @param script
	 *            Script to be executed.
	 * @return Always <code>null</code>
	 */
	public Object execute(final Script script) {
		fCodeTracer.run(script, registerScript(script));
		return null;
	}

	/**
	 * Map from custom filename to actual {@link Script} for easily identifying different scripts.
	 * <p>
	 * For each new file a unique filename is created using {@link #getHash(Script, Set)}.
	 */
	private final Map<String, Script> fScriptRegistry = new HashMap<>();

	private String registerScript(final Script script) {
		final String reference = getHash(script, fScriptRegistry.keySet());
		fScriptRegistry.put(reference, script);
		return reference;
	}

	/**
	 * Creates a unique filename for the given {@link Script}.
	 *
	 * @param script
	 *            {@link Script} to get unique filename for.
	 * @param existingKeys
	 *            Existing keys to avoid duplicates.
	 * @return Unique filename for given {@link Script}.
	 */
	private static String getHash(final Script script, final Set<String> existingKeys) {
		final StringBuilder buffer = new StringBuilder("__ref_");
		buffer.append(script.isDynamic() ? "dyn" : script.getCommand().toString());
		buffer.append("_");

		for (int index = 0; index < 10; index++)
			buffer.append((char) ('a' + new Random().nextInt(26)));

		if (existingKeys.contains(buffer.toString()))
			return getHash(script, existingKeys);

		return buffer.toString();
	}

}
