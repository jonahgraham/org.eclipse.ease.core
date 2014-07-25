/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.lang.javascript.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;

public class ModuleProposalCalculator {

	private static final Pattern LOAD_MODULE_PATTERN = Pattern.compile("loadModule\\([\"'](.*)[\"']\\)");
	private static final Pattern LINE_DATA_PATTERN = Pattern.compile(".*?([^\\p{Alnum}]?)(\\p{Alnum}*)$");

	final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

	/**
	 * Get a collection of module definitions for loadModule commands in code. Parses the source code before the current cursor position for <i>loadModule</i>
	 * commands. Afterwards extracts module definitions for detected modules. Currently does not follow include files.
	 * 
	 * @param context
	 *            content assist context
	 * @return collection of module definitions relevant for context assist
	 */
	protected Collection<ModuleDefinition> getLoadedModules(final ContentAssistInvocationContext context) {
		Collection<ModuleDefinition> modules = new HashSet<ModuleDefinition>();

		// environment module is always present
		modules.add(scriptService.getAvailableModules().get("/System/Environment"));

		try {
			// extract text before cursor
			IDocument document = context.getDocument();
			String prefix = document.get(0, context.getInvocationOffset());

			for (String name : getModuleNames(prefix)) {
				String fullName = ModuleHelper.resolveName(name);
				ModuleDefinition definition = scriptService.getAvailableModules().get(fullName);
				if (definition != null)
					modules.add(definition);
			}

		} catch (BadLocationException e1) {
			e1.printStackTrace();
			// ignore
		}

		return modules;
	}

	/**
	 * Extract names of loaded modules within provided code. Will only detect string literals, so if the loadModule parameter is not a single string, extraction
	 * will fail.
	 * 
	 * @param code
	 *            code to parse
	 * @return collection of module names
	 */
	private Collection<String> getModuleNames(final String code) {
		Collection<String> modules = new HashSet<String>();

		Matcher matcher = LOAD_MODULE_PATTERN.matcher(code);
		while (matcher.find())
			modules.add(matcher.group(1));

		return modules;
	}

	/**
	 * Extract context relevant information from current line. The returned matcher locates the last alphanumeric word in the line and an optional non
	 * alphanumeric character right before that word. result.group(1) contains the last non-alphanumeric token (eg a dot, brackets, arithmetic operators, ...),
	 * result.group(2) contains the alphanumeric text. This text can be used to filter content assist proposals.
	 * 
	 * @param context
	 *            content assist context
	 * @return matcher containing content assist information
	 * @throws BadLocationException
	 */
	protected Matcher matchLastToken(final ContentAssistInvocationContext context) throws BadLocationException {
		String data = getCurrentLine(context);
		return LINE_DATA_PATTERN.matcher(data);
	}

	/**
	 * Extract text from current line up to the cursor position
	 * 
	 * @param context
	 *            content assist context
	 * @return current line data
	 * @throws BadLocationException
	 */
	protected String getCurrentLine(final ContentAssistInvocationContext context) throws BadLocationException {
		IDocument document = context.getDocument();
		int lineNumber = document.getLineOfOffset(context.getInvocationOffset());
		IRegion lineInformation = document.getLineInformation(lineNumber);

		return document.get(lineInformation.getOffset(), context.getInvocationOffset() - lineInformation.getOffset());
	}
}
