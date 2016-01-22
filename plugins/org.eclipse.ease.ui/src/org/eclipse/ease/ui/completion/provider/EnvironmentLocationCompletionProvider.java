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

package org.eclipse.ease.ui.completion.provider;

import org.eclipse.ease.ICompletionContext;

public class EnvironmentLocationCompletionProvider extends AbstractFileLocationCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && ((context.getCaller().endsWith("include")) || (context.getCaller().endsWith("loadJar")))
				&& (context.getParameterOffset() == 0);
	}

	@Override
	protected boolean showCandidate(final Object candidate) {
		if (isFile(candidate)) {
			if (getContext().getCaller().endsWith("include"))
				return hasFileExtension(candidate, getContext().getScriptType().getDefaultExtension());

			else if (getContext().getCaller().endsWith("loadJar"))
				return hasFileExtension(candidate, "jar");
		}

		return super.showCandidate(candidate);
	}
}
