/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.CompletionContext;

public class PythonCompletionContext extends CompletionContext {

	/**
	 * @param scriptEngine
	 * @param scriptType
	 */
	public PythonCompletionContext(IScriptEngine scriptEngine) {
		super(scriptEngine, PythonHelper.getScriptType());
	}

	@Override
	protected boolean isLiteral(final char candidate) {
		return "'\"".indexOf(candidate) != -1;
	}
}
