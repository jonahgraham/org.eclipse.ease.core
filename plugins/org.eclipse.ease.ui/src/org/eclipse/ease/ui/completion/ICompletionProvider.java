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
package org.eclipse.ease.ui.completion;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public interface ICompletionProvider extends IContentProposalProvider {

	char[] getActivationChars();

	/**
	 * Adds code to the auto completion stack. Code can be parsed for variables, modules, etc which might be used for the next content assist request.
	 *
	 * @param code
	 *            executed code from script engine
	 * @param engine
	 *            Script engine to get further information about variables / functions.
	 */
	void addCode(String code, IScriptEngine engine);
}
