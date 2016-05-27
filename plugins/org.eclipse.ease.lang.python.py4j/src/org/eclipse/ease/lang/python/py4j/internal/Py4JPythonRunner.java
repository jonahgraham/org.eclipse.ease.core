/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.py4j.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;


public class Py4JPythonRunner {

	/**
	 * Path within this plug-in to the main python file.
	 */
	private static final String PYSRC_EASE_PY4J_MAIN_PY = "/pysrc/ease_py4j_main.py";

	/**
	 * The ID of the py4j sources plug-in, needs to match the name of the dependent plug-in.
	 */
	private static final String PY4J_PYTHON_BUNDLE_ID = "py4j-python";

	private int fJavaListeningPort;

	private boolean fRunning;

	private Process fProcess;

	/**
	 *
	 */
	public Py4JPythonRunner(int javaListeningPort) {
		fJavaListeningPort = javaListeningPort;
	}

	public void run() throws MalformedURLException, IOException, URISyntaxException {
		if (fRunning) {
			throw new IllegalStateException();
		}
		fRunning = true;

		fProcess = startPythonProcess();
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return fProcess;
	}

	/**
	 * @return the javaListeningPort
	 */
	protected int getJavaListeningPort() {
		return fJavaListeningPort;
	}

	protected void setPythonPathInEnvironment(Map<String, String> environment) throws IOException {
		environment.put("PYTHONPATH", getPy4jPythonSrc().toString());
	}

	private Process startPythonProcess() throws IOException, MalformedURLException, URISyntaxException {
		ProcessBuilder pb = new ProcessBuilder();

		setPythonPathInEnvironment(pb.environment());
		pb.command().add("python" /*TODO*/);
		pb.command().add("-u");
		pb.command().add(getPy4jEaseMainPy().toString());
		pb.command().add(Integer.toString(getJavaListeningPort()));

		Process start = pb.start();
		return start;
	}

	private File getPy4jPythonSrc() throws IOException {
		File py4jPythonBundleFile = FileLocator.getBundleFile(Platform.getBundle(PY4J_PYTHON_BUNDLE_ID));
		File py4jPythonSrc = new File(py4jPythonBundleFile, "/src");
		File py4j = new File(py4jPythonSrc, "py4j");
		if (!py4j.exists() || !py4j.isDirectory()) {
			throw new IOException("Failed to find py4j python directory, expected it here: " + py4j);
		}
		return py4jPythonSrc;
	}

	private File getPy4jEaseMainPy() throws MalformedURLException, IOException, URISyntaxException {
		URL url = new URL("platform:/plugin/" + Activator.PLUGIN_ID + PYSRC_EASE_PY4J_MAIN_PY);
		URL fileURL = FileLocator.toFileURL(url);
		File py4jEaseMain = new File(fileURL.toURI());
		if (!py4jEaseMain.exists()) {
			throw new IOException("Failed to find " + PYSRC_EASE_PY4J_MAIN_PY + ", expected it here: " + py4jEaseMain);
		}
		return py4jEaseMain;
	}

}
