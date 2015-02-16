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
package org.eclipse.ease.ui.scripts.repository.impl;

import java.util.HashMap;
import java.util.Map;

public class ParameterDelta {

	private final Map<String, String> fOldParameters;
	private final Map<String, String> fNewParameters;

	public ParameterDelta(final Map<String, String> oldParameters, final Map<String, String> newParameters) {
		fOldParameters = new HashMap<String, String>(oldParameters);
		fNewParameters = new HashMap<String, String>(newParameters);
	}

	public boolean isAffected(final String parameter) {
		final String oldValue = fOldParameters.get(parameter);
		final String newValue = fNewParameters.get(parameter);

		return (oldValue != null) ? !oldValue.equals(newValue) : oldValue != newValue;
	}

	public boolean isRemoved(final String parameter) {
		return fOldParameters.containsKey(parameter) && !fNewParameters.containsKey(parameter);
	}

	public boolean isAdded(final String parameter) {
		return !fOldParameters.containsKey(parameter) && fNewParameters.containsKey(parameter);
	}

	public boolean isModified(final String parameter) {
		return fOldParameters.containsKey(parameter) && fNewParameters.containsKey(parameter)
				&& !fOldParameters.get(parameter).equals(fNewParameters.get(parameter));
	}

	public String getOldParameter(final String parameter) {
		return fOldParameters.get(parameter);
	}

	public void merge(final ParameterDelta delta) {
		for (final String key : delta.fOldParameters.keySet()) {
			if (!delta.fNewParameters.containsKey(key))
				fNewParameters.remove(key);
		}

		fNewParameters.putAll(delta.fNewParameters);
	}
}
