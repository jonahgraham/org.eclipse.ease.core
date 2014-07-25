/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts.repository.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.IHeaderParser;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ui.PlatformUI;

public class InputStreamParser {

	/** Maximum amount of lines to scan for content type. */
	private static final int MAX_LINES = 50;

	/** Regex pattern to detect content-type keywords. */
	private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(".*script-type:\\s*(.*)", Pattern.CASE_INSENSITIVE);

	private final RepositoryService fRepositoryService;

	public InputStreamParser(RepositoryService repositoryService) {
		fRepositoryService = repositoryService;
	}

	protected RepositoryService getRepositoryService() {
		return fRepositoryService;
	}

	protected IScript getScriptByLocation(final String location) {
		for (IScript script : getRepositoryService().getScripts()) {
			if (script.getLocation().equals(location))
				return script;
		}

		return null;
	}

	protected Map<String, String> extractParameters(ScriptType type, InputStream stream) {
		IHeaderParser parser = type.getHeaderParser();
		if (parser != null)
			return parser.parser(stream);

		return Collections.emptyMap();
	}

	protected ScriptType getScriptType(InputStream contents) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(contents));

		// read MAX_LINES looking for a pattern content-type: <some content>
		try {
			String line = reader.readLine();
			int lineCount = MAX_LINES;
			while ((line != null) && (lineCount-- > 0)) {
				final Matcher matcher = CONTENT_TYPE_PATTERN.matcher(line);
				if (matcher.matches()) {
					// we found a content type
					final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
					ScriptType scriptType = scriptService.getAvailableScriptTypes().get(matcher.group(1));

					if (scriptType != null)
						return scriptType;
				}

			}
		} catch (IOException e) {
			// cannot read data, giving up
		}

		return null;
	}
}
