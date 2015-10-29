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
package org.eclipse.ease.ui.console;

import java.io.IOException;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Script;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.themes.IThemeManager;

public class ScriptConsole extends IOConsole implements IExecutionListener, IScriptEngineProvider, IPropertyChangeListener {

	private static final String TITLE_TERMINATED = " [terminated]";
	public static final String CONSOLE_ACTIVE = "ACTIVE";

	public static ScriptConsole create(final String title, final IScriptEngine engine) {
		final ScriptConsole console = new ScriptConsole(title, engine);

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);

		return console;
	}

	public static ScriptConsole create(final IScriptEngine engine) {
		return create(engine.getName(), engine);
	}

	private IOConsoleOutputStream mOutputStream = null;
	private IOConsoleOutputStream mErrorStream = null;
	private IScriptEngine mEngine = null;
	private ILaunch mLaunch = null;
	private ScriptConsolePageParticipant mScriptConsolePageParticipant;

	private ScriptConsole(final String name, final IScriptEngine engine) {
		this(name, getConsoleType(), null, engine);
	}

	private ScriptConsole(final String name, final String consoleType, final ImageDescriptor imageDescriptor, final IScriptEngine engine) {
		super(name, consoleType, imageDescriptor, true);

		setScriptEngine(engine);

		initializeStreams();

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	private void initializeStreams() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		final IOConsoleOutputStream outputStream = getOutputStream();
		outputStream.setActivateOnWrite(store.getBoolean(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_OUT));

		final IOConsoleOutputStream errorStream = getErrorStream();
		errorStream.setActivateOnWrite(store.getBoolean(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_ERR));

		// set error stream color
		final IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		final ColorRegistry colorRegistry = themeManager.getCurrentTheme().getColorRegistry();
		errorStream.setColor(colorRegistry.get("ERROR_COLOR"));

		// set input stream color
		getInputStream().setColor(colorRegistry.get("CONSOLE_INPUT_COLOR"));
	}

	public static String getConsoleType() {
		return "Text console type";
	}

	public IOConsoleOutputStream getErrorStream() {
		if ((mErrorStream == null) || (mErrorStream.isClosed()))
			mErrorStream = newOutputStream();

		return mErrorStream;
	}

	public IOConsoleOutputStream getOutputStream() {
		if ((mOutputStream == null) || (mOutputStream.isClosed()))
			mOutputStream = newOutputStream();

		return mOutputStream;
	}

	@Override
	protected void dispose() {
		final Activator activator = Activator.getDefault();
		if (activator != null)
			activator.getPreferenceStore().removePropertyChangeListener(this);

		setScriptEngine(null);

		super.dispose();
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		// do not react on engines that are no longer tracked by this console
		if (engine.equals(getScriptEngine())) {
			switch (status) {
			case ENGINE_END:
				terminate();
				break;
			}
		}
	}

	public synchronized void terminate() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				setName(getName() + TITLE_TERMINATED);
			}
		});

		setScriptEngine(null);
		closeStreams();
	}

	private void closeStreams() {
		// close streams
		try {
			if (mOutputStream != null)
				mOutputStream.close();
		} catch (final IOException e) {

		}
		try {
			if (mErrorStream != null)
				mErrorStream.close();
		} catch (final IOException e) {
		}
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return mEngine;
	}

	public void setLaunch(final ILaunch launch) {
		mLaunch = launch;
	}

	public ILaunch getLaunch() {
		return mLaunch;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		final String property = event.getProperty();

		if (property.equals(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_OUT))
			getOutputStream().setActivateOnWrite((Boolean) event.getNewValue());

		else if (property.equals(IPreferenceConstants.CONSOLE_BASE + "." + getName() + "." + IPreferenceConstants.CONSOLE_OPEN_ON_ERR))
			getErrorStream().setActivateOnWrite((Boolean) event.getNewValue());
	}

	public synchronized void setScriptEngine(final IScriptEngine scriptEngine) {
		if ((scriptEngine == null) || (!scriptEngine.equals(mEngine))) {
			// new engine detected

			if (mEngine != null) {
				mEngine.removeExecutionListener(this);
				closeStreams();
			}

			mEngine = scriptEngine;

			if (mEngine != null)
				mEngine.addExecutionListener(this);

			if (mScriptConsolePageParticipant != null)
				mScriptConsolePageParticipant.engineChanged();
		}
	}

	public void setPageParticipant(final ScriptConsolePageParticipant scriptConsolePageParticipant) {
		mScriptConsolePageParticipant = scriptConsolePageParticipant;
	}
}
