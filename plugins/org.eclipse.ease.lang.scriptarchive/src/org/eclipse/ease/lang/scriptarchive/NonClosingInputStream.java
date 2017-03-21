/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.scriptarchive;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream delegating all tasks to a base stream. The only method not forwarded is close().
 */
public class NonClosingInputStream extends InputStream {

	private final InputStream fBaseStream;

	public NonClosingInputStream(InputStream baseStream) {
		fBaseStream = baseStream;
	}

	@Override
	public int read() throws IOException {
		return fBaseStream.read();
	}

	@Override
	public int available() throws IOException {
		return fBaseStream.available();
	}

	@Override
	public long skip(long n) throws IOException {
		return fBaseStream.skip(n);
	}

	@Override
	public boolean markSupported() {
		return fBaseStream.markSupported();
	}

	@Override
	public synchronized void mark(int readlimit) {
		fBaseStream.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		fBaseStream.reset();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return fBaseStream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return fBaseStream.read(b);
	}
}
