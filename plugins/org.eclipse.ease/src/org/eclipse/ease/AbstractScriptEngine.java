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
package org.eclipse.ease;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.Preferences;

/**
 * Base implementation for a script engine. Handles Job implementation of script engine, adding script code for execution, module loading support and a basic
 * online help system.
 */
public abstract class AbstractScriptEngine extends Job implements IScriptEngine {

	/** List of code junks to be executed. */
	private final List<Script> fCodePieces = Collections.synchronizedList(new ArrayList<Script>());

	private final ListenerList fExecutionListeners = new ListenerList();

	/** Indicator to terminate once this Job gets IDLE. */
	private volatile boolean fTerminateOnIdle = true;

	private PrintStream fOutputStream = null;

	private PrintStream fErrorStream = null;

	private InputStream fInputStream = null;

	private final List<IScriptDebugFrame> fStackTrace = new LinkedList<IScriptDebugFrame>();

	private EngineDescription fDescription;

	private boolean fSetupDone = false;

	/** Variables tried to set before engine was started. */
	private final Map<String, Object> fBufferedVariables = new HashMap<String, Object>();

	private boolean fCloseStreamsOnTerminate;

	private boolean fTerminated = false;

	/**
	 * Constructor. Sets the name for the underlying job.
	 *
	 * @param name
	 *            name of script engine job
	 */
	public AbstractScriptEngine(final String name) {
		super(name);

		// make this a system job (not visible to the user)
		setSystem(true);
	}

	@Override
	public EngineDescription getDescription() {
		return fDescription;
	}

	@Override
	public final ScriptResult executeAsync(final Object content) {
		final Script piece;
		if (content instanceof Script)
			piece = (Script) content;
		else
			piece = new Script(content);

		fCodePieces.add(piece);
		synchronized (this) {
			notifyAll();
		}

		return piece.getResult();
	}

	@Override
	public final ScriptResult executeSync(final Object content) throws InterruptedException {

		if (getState() == NONE)
			// automatically schedule engine as it is not started yet
			schedule();

		final ScriptResult result = executeAsync(content);

		synchronized (result) {
			while (!result.isReady())
				result.wait();
		}

		return result;
	}

	@Override
	public final Object inject(final Object content) {
		return internalInject(content, false);
	}

	@Override
	public final Object injectUI(final Object content) {
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);
		final boolean allowUIAccess = prefs.getBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, Activator.DEFAULT_SCRIPTS_ALLOW_UI_ACCESS);
		if (!allowUIAccess)
			throw new RuntimeException("Script UI access disabled by user preferences.");

		return internalInject(content, true);
	}

	private final Object internalInject(final Object content, final boolean uiThread) {
		// injected code shall not trigger a new event, therefore notifyListerners needs to be false
		ScriptResult result;
		if (content instanceof Script)
			result = inject((Script) content, false, uiThread);
		else
			result = inject(new Script(content), false, uiThread);

		if (result.hasException()) {
			// re-throw previous exception

			if (result.getException() instanceof RuntimeException)
				throw (RuntimeException) result.getException();

			throw new RuntimeException(result.getException().getMessage(), result.getException());
		}

		return result.getResult();
	}

	/**
	 * Inject script code to the script engine. Injected code is processed synchronous by the current thread unless <i>uiThread</i> is set to <code>true</code>.
	 * Nevertheless this is a blocking call.
	 *
	 * @param script
	 *            script to be executed
	 * @param notifyListeners
	 *            <code>true</code> when listeners should be informed of code fragment
	 * @param uiThread
	 *            when set to <code>true</code> run injected code in UI thread
	 * @return script execution result
	 */
	private ScriptResult inject(final Script script, final boolean notifyListeners, final boolean uiThread) {

		synchronized (script.getResult()) {

			try {
				Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Executing script (" + script.getTitle() + "):", script.getCode());

				fStackTrace.add(0, new ScriptDebugFrame(script, 0, IScriptDebugFrame.TYPE_FILE));

				// execution
				if (notifyListeners)
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_START);
				else
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_INJECTION_START);

				script.setResult(execute(script, script.getFile(), fStackTrace.get(0).getName(), uiThread));

			} catch (final ExitException e) {
				script.setResult(e.getCondition());

			} catch (final BreakException e) {
				script.setResult(e.getCondition());

			} catch (final Throwable e) {
				script.setException(e);

				// only do the printing if this is the last script on the stack
				// otherwise we will print multiple times for each rethrow
				if (fStackTrace.size() == 1)
					e.printStackTrace(getErrorStream());

			} finally {
				if (notifyListeners)
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_END);
				else
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_INJECTION_END);

				fStackTrace.remove(0);
			}
		}

		return script.getResult();
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine started: " + getName());
		final boolean setup = setupEngine();
		if (setup) {
			fSetupDone = true;

			// engine is initialized, set buffered variables
			for (final Entry<String, Object> entry : fBufferedVariables.entrySet()) {
				setVariable(entry.getKey(), entry.getValue());
			}

			fBufferedVariables.clear();

			// setup new trace
			fStackTrace.clear();

			notifyExecutionListeners(null, IExecutionListener.ENGINE_START);

			// main loop
			while ((!monitor.isCanceled()) && (!isTerminated())) {

				// execute code
				if (!fCodePieces.isEmpty()) {
					final Script piece = fCodePieces.remove(0);
					inject(piece, true, false);

				} else {

					synchronized (this) {
						if (!isTerminated()) {
							try {
								Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine idle: " + getName());
								wait();
							} catch (final InterruptedException e) {
							}
						}
					}
				}
			}

			// discard pending code pieces
			synchronized (fCodePieces) {
				for (final Script script : fCodePieces)
					script.setException(new ExitException());
			}

			fCodePieces.clear();

			notifyExecutionListeners(null, IExecutionListener.ENGINE_END);

			teardownEngine();
			fTerminated = true;
			synchronized (this) {
				notifyAll();
			}
		}

		closeStreams();

		Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine terminated: " + getName());

		if (!setup)
			throw new RuntimeException("Could not setup script engine, terminating");

		if (isTerminated())
			return Status.OK_STATUS;

		return Status.CANCEL_STATUS;
	}

	private void closeStreams() {
		if (fCloseStreamsOnTerminate) {
			// gracefully close I/O streams
			try {
				if ((getInputStream() != null) && (!System.in.equals(getInputStream())))
					getInputStream().close();
			} catch (final IOException e) {
			}
			try {
				if ((getOutputStream() != null) && (!System.out.equals(getOutputStream())))
					getOutputStream().close();
			} catch (final Exception e) {
			}
			try {
				if ((getErrorStream() != null) && (!System.err.equals(getErrorStream())))
					getErrorStream().close();
			} catch (final Exception e) {
			}
		}

		fOutputStream = null;
		fErrorStream = null;
		fInputStream = null;
	}

	@Override
	public void setCloseStreamsOnTerminate(final boolean closeStreams) {
		fCloseStreamsOnTerminate = closeStreams;
	}

	@Override
	public PrintStream getOutputStream() {
		return (fOutputStream != null) ? fOutputStream : System.out;
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		if (outputStream instanceof PrintStream)
			fOutputStream = (PrintStream) outputStream;

		else
			fOutputStream = new PrintStream(outputStream);
	}

	@Override
	public InputStream getInputStream() {
		return (fInputStream != null) ? fInputStream : System.in;
	}

	@Override
	public void setInputStream(final InputStream inputStream) {
		fInputStream = inputStream;
	}

	@Override
	public PrintStream getErrorStream() {
		return (fErrorStream != null) ? fErrorStream : System.err;
	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		if (errorStream instanceof PrintStream)
			fErrorStream = (PrintStream) errorStream;

		else
			fErrorStream = new PrintStream(errorStream);
	}

	@Override
	public final void setTerminateOnIdle(final boolean terminate) {
		fTerminateOnIdle = terminate;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public boolean getTerminateOnIdle() {
		return fTerminateOnIdle;
	}

	/**
	 * Get termination status of the interpreter. A terminated interpreter cannot be restarted.
	 *
	 * @return true if interpreter is terminated.
	 */
	private boolean isTerminated() {
		return fTerminateOnIdle && fCodePieces.isEmpty();
	}

	/**
	 * Get idle status of the interpreter. The interpreter is IDLE if there are no pending execution requests and the interpreter is not terminated.
	 *
	 * @return true if interpreter is IDLE
	 */
	@Override
	public boolean isIdle() {
		return fCodePieces.isEmpty();
	}

	@Override
	public void addExecutionListener(final IExecutionListener listener) {
		fExecutionListeners.add(listener);
	}

	@Override
	public void removeExecutionListener(final IExecutionListener listener) {
		fExecutionListeners.remove(listener);
	}

	protected void notifyExecutionListeners(final Script script, final int status) {
		for (final Object listener : fExecutionListeners.getListeners())
			((IExecutionListener) listener).notify(this, script, status);
	}

	@Override
	public void terminate() {
		setTerminateOnIdle(true);
		fCodePieces.clear();
		terminateCurrent();

		// ask thread to terminate
		cancel();
		if (getThread() != null)
			getThread().interrupt();
	}

	@Override
	public void reset() {
		// make sure that everybody gets notified that script engine got a reset
		for (final Script script : fCodePieces)
			script.setException(new ExitException("Script engine got resetted."));

		fCodePieces.clear();

		// re-enable launch extensions to register themselves
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		for (final IScriptEngineLaunchExtension extension : scriptService.getLaunchExtensions(getDescription().getID()))
			extension.createEngine(this);
	}

	public List<IScriptDebugFrame> getStackTrace() {
		return fStackTrace;
	}

	@Override
	public Object getExecutedFile() {
		for (final IScriptDebugFrame trace : getStackTrace()) {
			if (trace.getType() == IScriptDebugFrame.TYPE_FILE) {
				final Object file = trace.getScript().getFile();
				if (file != null)
					return file;
			}
		}

		return null;
	}

	@Override
	public void setEngineDescription(final EngineDescription description) {
		fDescription = description;
	}

	@Override
	public void setVariable(final String name, final Object content) {
		if (fSetupDone)
			internalSetVariable(name, content);

		else
			fBufferedVariables.put(name, content);
	}

	@Override
	public Object getVariable(final String name) {
		if (fSetupDone)
			return internalGetVariable(name);

		return fBufferedVariables.get(name);
	}

	@Override
	public boolean hasVariable(final String name) {
		if (fSetupDone)
			return internalHasVariable(name);

		return fBufferedVariables.containsKey(name);
	}

	@Override
	public Object removeVariable(final String name) {
		if (fSetupDone)
			return internalRemoveVariable(name);

		return fBufferedVariables.remove(name);
	}

	@Override
	public Map<String, Object> getVariables() {
		if (fSetupDone)
			return internalGetVariables();

		return Collections.unmodifiableMap(fBufferedVariables);
	}

	/**
	 * Split a string with comma separated arguments.
	 *
	 * @param arguments
	 *            comma separated arguments
	 * @return trimmed list of arguments
	 */
	public static final String[] extractArguments(final String arguments) {
		final ArrayList<String> args = new ArrayList<String>();
		if (arguments != null) {

			String[] tokens = arguments.split(",");
			for (String token : tokens) {
				if (!token.trim().isEmpty())
					args.add(token.trim());
			}
		}

		return args.toArray(new String[args.size()]);
	}

	@Override
	public boolean isFinished() {
		return fTerminated;
	}

	@Override
	public void join(final long timeout) throws InterruptedException {
		synchronized (this) {
			if (!isFinished())
				wait(timeout);
		}
	}

	/**
	 * Internal version of {@link #getVariable(String)}. Only called after script engine was initialized successfully.
	 */
	protected abstract Object internalGetVariable(String name);

	/**
	 * Internal version of {@link #getVariables()}. Only called after script engine was initialized successfully.
	 */
	protected abstract Map<String, Object> internalGetVariables();

	/**
	 * Internal version of {@link #hasVariable(String)}. Only called after script engine was initialized successfully.
	 */
	protected abstract boolean internalHasVariable(String name);

	/**
	 * Internal version of {@link #setVariable(String, Object)}. Only called after script engine was initialized successfully.
	 */
	protected abstract void internalSetVariable(String name, Object content);

	/**
	 * Internal version of {@link #removeVariable(String)}. Only called after script engine was initialized successfully.
	 */
	protected abstract Object internalRemoveVariable(String name);

	/**
	 * Setup method for script engine. Run directly after the engine is activated. Needs to return <code>true</code>. Otherwise the engine will terminate
	 * instantly.
	 *
	 * @return <code>true</code> when setup succeeds
	 */
	protected abstract boolean setupEngine();

	/**
	 * Teardown engine. Called immediately before the engine terminates. This method is not called when {@link #setupEngine()} fails.
	 *
	 * @return teardown result
	 */
	protected abstract boolean teardownEngine();

	/**
	 * Execute script code.
	 *
	 * @param fileName
	 *            name of file executed
	 * @param uiThread
	 * @param reader
	 *            reader for script data to be executed
	 * @param uiThread
	 *            when set to <code>true</code> run code in UI thread
	 * @return execution result
	 * @throws Throwable
	 *             any exception thrown during script execution
	 */
	protected abstract Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable;
}
