/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ease.IHeaderParser;
import org.eclipse.ease.Logger;

public class ScriptType {

	private static final String NAME = "name";
	private static final String DEFAULT_EXTENSION = "defaultExtension";
	private static final String BINDING = "binding";
	private static final String HEADER_PARSER = "headerParser";
	private static final String CONTENT_TYPE = "contentType";

	private final IConfigurationElement fConfigurationElement;

	public ScriptType(final IConfigurationElement configurationElement) {
		fConfigurationElement = configurationElement;
	}

	public String getName() {
		return fConfigurationElement.getAttribute(NAME);
	}

	public String getDefaultExtension() {
		return fConfigurationElement.getAttribute(DEFAULT_EXTENSION);
	}

	public Collection<String> getContentTypes() {
		Collection<String> result = new HashSet<String>();

		for (IConfigurationElement binding : fConfigurationElement.getChildren(BINDING))
			result.add(binding.getAttribute(CONTENT_TYPE));

		return result;
	}

	public IHeaderParser getHeaderParser() {
		try {
			Object parser = fConfigurationElement.createExecutableExtension(HEADER_PARSER);
			if (parser instanceof IHeaderParser)
				return (IHeaderParser) parser;

		} catch (CoreException e) {
			// could not instantiate class
			Logger.logError("Could not instantiate header parser", e);
		}

		return null;
	}

	/**
	 * Get available engines. Returns available script engine descriptions sorted by priority (highest first).
	 *
	 * @return available engines
	 */
	public List<EngineDescription> getEngines() {
		List<EngineDescription> engines = new ArrayList<EngineDescription>();

		final IScriptService scriptService = ScriptService.getService();
		for (EngineDescription description : scriptService.getEngines()) {
			if (description.getSupportedScriptTypes().contains(this))
				engines.add(description);
		}

		// sort engines to report highest priority first
		Collections.sort(engines, new Comparator<EngineDescription>() {

			@Override
			public int compare(final EngineDescription o1, final EngineDescription o2) {
				return o2.getPriority() - o1.getPriority();
			}
		});

		return engines;
	}
}
