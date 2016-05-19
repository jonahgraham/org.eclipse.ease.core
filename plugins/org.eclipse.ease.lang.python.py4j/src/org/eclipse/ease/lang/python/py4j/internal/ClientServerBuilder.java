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

import java.net.InetAddress;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import py4j.ClientServer;
import py4j.GatewayServer;

/**
 * Helper class to make it easier and self-documentating how to make a {@link ClientServer} which has lots of parameters in its constructor.
 *
 * This has also been contributed to Py4J where it more rightly belongs. Once py4j version is upgraded, this copy can be removed. See
 * https://github.com/bartdag/py4j/pull/204.
 */
public class ClientServerBuilder {
	private int fJavaPort;
	private InetAddress fJavaAddress;
	private int fPythonPort;
	private InetAddress fPythonAddress;
	private int fConnectTimeout;
	private int fReadTimeout;
	private ServerSocketFactory fServerSocketFactory;
	private SocketFactory fSocketFactory;
	private Object fEntryPoint;

	public ClientServerBuilder() {
		this(null);
	}

	public ClientServerBuilder(Object entryPoint) {
		fJavaPort = GatewayServer.DEFAULT_PORT;
		fJavaAddress = GatewayServer.defaultAddress();
		fPythonPort = GatewayServer.DEFAULT_PYTHON_PORT;
		fPythonAddress = GatewayServer.defaultAddress();
		fConnectTimeout = GatewayServer.DEFAULT_CONNECT_TIMEOUT;
		fReadTimeout = GatewayServer.DEFAULT_READ_TIMEOUT;
		fServerSocketFactory = ServerSocketFactory.getDefault();
		fSocketFactory = SocketFactory.getDefault();
		fEntryPoint = entryPoint;
	}

	public ClientServer build() {
		return new ClientServer(fJavaPort, fJavaAddress, fPythonPort, fPythonAddress, fConnectTimeout, fReadTimeout, fServerSocketFactory, fSocketFactory,
				fEntryPoint);
	}

	public ClientServerBuilder javaPort(int javaPort) {
		this.fJavaPort = javaPort;
		return this;
	}

	public ClientServerBuilder javaAddress(InetAddress javaAddress) {
		this.fJavaAddress = javaAddress;
		return this;
	}

	public ClientServerBuilder pythonPort(int pythonPort) {
		this.fPythonPort = pythonPort;
		return this;
	}

	public ClientServerBuilder pythonAddress(InetAddress pythonAddress) {
		this.fPythonAddress = pythonAddress;
		return this;
	}

	public ClientServerBuilder connectTimeout(int connectTimeout) {
		this.fConnectTimeout = connectTimeout;
		return this;
	}

	public ClientServerBuilder readTimeout(int readTimeout) {
		this.fReadTimeout = readTimeout;
		return this;
	}

	public ClientServerBuilder sSocketFactory(ServerSocketFactory serverSocketFactory) {
		this.fServerSocketFactory = serverSocketFactory;
		return this;
	}

	public ClientServerBuilder socketFactory(SocketFactory socketFactory) {
		this.fSocketFactory = socketFactory;
		return this;
	}

	public ClientServerBuilder entryPoint(Object entryPoint) {
		this.fEntryPoint = entryPoint;
		return this;
	}
}
