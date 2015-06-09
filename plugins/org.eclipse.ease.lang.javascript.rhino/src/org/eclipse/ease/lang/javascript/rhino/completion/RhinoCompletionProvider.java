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
package org.eclipse.ease.lang.javascript.rhino.completion;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.ModuleCompletionProvider;
import org.eclipse.jface.fieldassist.ContentProposal;

public class RhinoCompletionProvider extends ModuleCompletionProvider {

	private static final Pattern VARIABLES_PATTERN = Pattern.compile(".*?(\\p{Alnum}+)\\s*=\\s*[^=]+");
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("function\\s+(\\p{Alpha}\\p{Alnum}*)\\(");

	@Override
	public void addCode(final String code, final IScriptEngine engine) {
		// extract variables
		Matcher matcher = VARIABLES_PATTERN.matcher(code);
		while (matcher.find()) {
			Object var = engine.getVariable(matcher.group(1));
			if (var != null) {
				setVariable(matcher.group(1), var.getClass());
			} else {
				setVariable(matcher.group(1), null);
			}
		}

		// extract functions
		matcher = FUNCTION_PATTERN.matcher(code);
		while (matcher.find()) {
			Object func = engine.getVariable(matcher.group(1));
			if (func != null && (func instanceof Method)) {
				setFunction(matcher.group(1), (Method) func);
			} else {
				setFunction(matcher.group(1), null);
			}
		}

		super.addCode(code, engine);
	}

	@Override
	protected void modifyProposals(final Collection<ContentProposal> proposals, final String contents) {
		// All handled by ModuleCompletionProvider
	}
}
