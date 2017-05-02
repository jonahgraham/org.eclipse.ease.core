/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     IBM Corporation - some methods of this class were taken from org.eclipse.pde.internal.core.util.ManifestUtils
 *******************************************************************************/

package org.eclipse.ease.lang.scriptarchive.ui.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.osgi.util.ManifestElement;

public class ManifestOutputStream extends OutputStream {

	/**
	 * Manifest header for the syntax version of the jar manifest. Not part of the OSGi specification. Must be the first header in the manifest. Typically set
	 * to '1.0'.
	 */
	public static final String MANIFEST_VERSION = "Manifest-Version"; //$NON-NLS-1$
	public static final String MANIFEST_LIST_SEPARATOR = ",\n "; //$NON-NLS-1$
	public static final String MANIFEST_LINE_SEPARATOR = "\n "; //$NON-NLS-1$
	private static int MANIFEST_MAXLINE = 511;

	private final OutputStream fBaseStream;

	public ManifestOutputStream(OutputStream baseStream) {
		fBaseStream = baseStream;
	}

	public void writeManifest(Set<Entry<Object, Object>> entries) throws IOException {
		// replaces any eventual existing file
		final Hashtable<String, String> manifestToWrite = new Hashtable<>();
		for (final Entry<Object, Object> entry : entries)
			manifestToWrite.put(entry.getKey().toString(), entry.getValue().toString());

		// make sure we have a manifest version
		if (!manifestToWrite.containsKey(MANIFEST_VERSION))
			manifestToWrite.put(MANIFEST_VERSION, "1.0");

		// The manifest-version header is not used by OSGi but must be the first header according to the JDK Jar specification
		writeEntry(MANIFEST_VERSION, manifestToWrite.remove(MANIFEST_VERSION));

		final Iterator<?> keys = manifestToWrite.keySet().iterator();
		while (keys.hasNext()) {
			final String key = (String) keys.next();
			writeEntry(key, manifestToWrite.get(key));
		}
		flush();
	}

	private void writeEntry(String key, String value) throws IOException {
		if ((value != null) && (value.length() > 0)) {
			write(splitOnComma(key + ": " + value).getBytes()); //$NON-NLS-1$
			write('\n');
		}
	}

	private static String splitOnComma(String value) {
		if ((value.length() < MANIFEST_MAXLINE) || (value.indexOf(MANIFEST_LINE_SEPARATOR) >= 0))
			return value; // assume the line is already split
		final String[] values = ManifestElement.getArrayFromList(value);
		if ((values == null) || (values.length == 0))
			return value;
		final StringBuffer sb = new StringBuffer(value.length() + ((values.length - 1) * MANIFEST_LIST_SEPARATOR.length()));
		for (int i = 0; i < (values.length - 1); i++)
			sb.append(values[i]).append(MANIFEST_LIST_SEPARATOR);
		sb.append(values[values.length - 1]);
		return sb.toString();
	}

	@Override
	public void write(int b) throws IOException {
		fBaseStream.write(b);
	}

	@Override
	public void flush() throws IOException {
		super.flush();

		fBaseStream.flush();
	}

	@Override
	public void close() throws IOException {
		super.close();

		fBaseStream.close();
	}
}
