/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.AbstractCodeParser;
import org.eclipse.jface.text.Position;

public abstract class AbstractCompletionParser extends AbstractCodeParser {

	public static List<Position> findInvocations(final String call, final String code) {
		List<Position> result = new ArrayList<Position>();

		String methodName = (call.contains("(")) ? call.substring(0, call.indexOf('(')) : call;

		// group 1: full call
		// group 2: parameters
		Pattern methodPattern = Pattern.compile("^.*(" + methodName + "\\s*\\((.*)\\))\\s*;?\\s*$", Pattern.MULTILINE);
		Matcher matcher = methodPattern.matcher(code);

		while (matcher.find()) {
			// pattern found
			result.add(new Position(matcher.start(1), matcher.end(1) - matcher.start(1)));
		}

		return result;
	}

	public static String[] getParameters(final String call) {
		int start = call.indexOf('(');
		int end = call.lastIndexOf(')');
		if ((start > 0) && (end > start))
			return call.substring(start + 1, end).split(",");

		return new String[0];
	}
}
