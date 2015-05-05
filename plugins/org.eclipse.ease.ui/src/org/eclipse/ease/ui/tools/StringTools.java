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
package org.eclipse.ease.ui.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.Platform;

/**
 * Helper class for string manipulations.
 */
public final class StringTools {

	// TODO move to o.e.ease.core
	/** Default line break character. */
	public static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	public static final String toString(final InputStream stream) throws IOException {
		return toString(new InputStreamReader(stream));
	}

	public static String toString(final Reader reader) throws IOException {
		if (!(reader instanceof BufferedReader))
			return toString(new BufferedReader(reader));

		char[] buffer = new char[1024];

		StringBuilder result = new StringBuilder();
		int read;
		do {
			read = reader.read(buffer);
			if (read > 0)
				result.append(new String(buffer, 0, read));

		} while (read != -1);

		return result.toString();
	}
}
