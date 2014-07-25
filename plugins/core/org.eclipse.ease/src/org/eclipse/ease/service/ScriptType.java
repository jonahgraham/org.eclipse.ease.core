/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.service;

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

import com.google.common.base.Function;

public class ScriptType {

	public static class ToScriptType implements Function<ScriptType, String> {

		@Override
		public String apply(final ScriptType arg0) {
			return arg0.getName();
		}

	}

	public static class ToExtensionFile implements Function<ScriptType, String> {

		@Override
		public String apply(final ScriptType arg0) {
			return arg0.getDefaultExtension();
		}

	}

	// protected String scritpType;

	// protected String extension;

	private static final String NAME = "name";
	private static final String DEFAULT_EXTENSION = "defaultExtension";
	private static final String BINDING = "binding";
	private static final String HEADER_PARSER = "headerParser";
	private static final String CONTENT_TYPE = "contentType";

	private final IConfigurationElement fConfigurationElement;

	public ScriptType(final IConfigurationElement configurationElement) {
		fConfigurationElement = configurationElement;
	}

	// public static ScriptType createScriptType(String scriptType, String extension) {
	// ScriptType result = new ScriptType();
	// result.setExtension(extension);
	// result.setScritpType(scriptType);
	// return result;
	// }

	// /**
	// * @return the scritpType
	// */
	// public String getScritpType() {
	// return scritpType;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#hashCode()
	// */
	// @Override
	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = (prime * result) + ((extension == null) ? 0 : extension.hashCode());
	// result = (prime * result) + ((scritpType == null) ? 0 : scritpType.hashCode());
	// return result;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (getClass() != obj.getClass())
	// return false;
	// ScriptType other = (ScriptType) obj;
	// if (extension == null) {
	// if (other.extension != null)
	// return false;
	// } else if (!extension.equals(other.extension))
	// return false;
	// if (scritpType == null) {
	// if (other.scritpType != null)
	// return false;
	// } else if (!scritpType.equals(other.scritpType))
	// return false;
	// return true;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see java.lang.Object#toString()
	// */
	// @Override
	// public String toString() {
	// return "ScriptType [scritpType=" + scritpType + ", extension=" + extension + "]";
	// }
	//
	// /**
	// * @param scritpType
	// * the scritpType to set
	// */
	// public void setScritpType(String scritpType) {
	// this.scritpType = scritpType;
	// }
	//
	// /**
	// * @param extension
	// * the extension to set
	// */
	// public void setExtension(String extension) {
	// this.extension = extension;
	// }

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
