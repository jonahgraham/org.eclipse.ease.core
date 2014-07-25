/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.engine.javascript.rhino.completion;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.ui.completion.ModuleCompletionProvider;
import org.eclipse.jface.fieldassist.ContentProposal;

public class RhinoCompletionProvider extends ModuleCompletionProvider {

	private static final Pattern VARIABLES_PATTERN = Pattern.compile(".*?(\\p{Alnum}+)\\s*=\\s*[^=]+");
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("function\\s+(\\p{Alpha}\\p{Alnum}*)\\(");

	private final Collection<String> fVariables = new HashSet<String>();
	private final Collection<String> fFunctions = new HashSet<String>();

	@Override
	public void addCode(final String code) {
		// extract variables
		Matcher matcher = VARIABLES_PATTERN.matcher(code);
		while (matcher.find())
			fVariables.add(matcher.group(1));

		// extract functions
		matcher = FUNCTION_PATTERN.matcher(code);
		while (matcher.find())
			fFunctions.add(matcher.group(1));

		super.addCode(code);
	}

	@Override
	protected void modifyProposals(final Collection<ContentProposal> proposals, final String contents) {

		Matcher matcher = matchLastToken(contents);
		if (matcher.matches()) {
			if (".".equals(matcher.group(1))) {
				// code tries to call a class method, not a function
				// do nothing
			} else {
				// add variables
				for (String variable : fVariables) {
					if ((variable.startsWith(matcher.group(2))) && (matcher.group(2).length() < variable.length()))
						proposals.add(new ContentProposal(variable.substring(matcher.group(2).length()), variable, "local variable"));
				}

				// add functions
				for (String function : fFunctions)
					if ((function.startsWith(matcher.group(2))) && (matcher.group(2).length() < function.length()))
						proposals.add(new ContentProposal(function.substring(matcher.group(2).length()), function, "local function"));
			}
		}
	}
}
