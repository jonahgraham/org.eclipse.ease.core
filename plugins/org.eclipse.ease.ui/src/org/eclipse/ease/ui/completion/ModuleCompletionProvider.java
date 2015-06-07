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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.ui.PlatformUI;

public abstract class ModuleCompletionProvider implements ICompletionProvider {

	private static final String EXTENSION_PROCESSOR_ID = "org.eclipse.ease.ui.completionProcessor";
	private static final String EXTENSION_PROCESSOR = "completionProcessor";
	private static final String PARAMETER_ENGINE_ID = "engineID";
	private static final String PARAMETER_CLASS = "class";

	private static final Pattern LOAD_MODULE_PATTERN = Pattern.compile("loadModule\\([\"'](.*)[\"']\\)");
	private static final Pattern LINE_DATA_PATTERN = Pattern.compile(".*?([^\\p{Alnum}]?)(\\p{Alnum}*)$");

	public static ICompletionProvider getCompletionProvider(final EngineDescription engineDescription) {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_PROCESSOR_ID);
		for (final IConfigurationElement e : config) {
			if (e.getName().equals(EXTENSION_PROCESSOR)) {
				// completion processor detected
				if (engineDescription.getID().equals(e.getAttribute(PARAMETER_ENGINE_ID))) {
					try {
						Object executable = e.createExecutableExtension(PARAMETER_CLASS);
						if (executable instanceof ICompletionProvider)
							return (ICompletionProvider) executable;
					} catch (CoreException e1) {
						Logger.logError("Invalid completion processor detected for engine " + engineDescription.getID(), e1);
					}
				}
			}
		}

		return null;
	}

	protected final Collection<ModuleDefinition> fLoadedModules = new HashSet<ModuleDefinition>();

	public ModuleCompletionProvider() {
		// add environment module
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		fLoadedModules.add(scriptService.getAvailableModules().get(EnvironmentModule.MODULE_NAME));
	}
	
	/**
	 * Gets human readable description of method for completion UI.
	 * 
	 * @param method
	 *            Method to get description from.
	 * @param module
	 *            Module for method.
	 * @return Human readable description of method.
	 */
	protected String getDescription(Method method, ModuleDefinition module) {
		StringBuilder sb = new StringBuilder();
		sb.append("Public method of module ");
		sb.append(module.getName());
		sb.append(".");
		sb.append("\n");
		sb.append("Signature and Overloads:\n");
		for (Method overload : module.getMethods()) {
			if (overload.getName().equals(method.getName())) {
				sb.append(overload.toGenericString());
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Gets human readable description of member for completion UI.
	 * 
	 * @param field
	 *            Field to get description from.
	 * @param clazz
	 *            Class for method.
	 * @return Human readable description of field.
	 */
	protected String getDescription(Field field, ModuleDefinition module) {
		// Get field modifiers.
		StringBuilder sb = new StringBuilder();
		sb.append("Public ");
		sb.append("member of class ");
		sb.append(module.getName());
		sb.append(" of type ");
		sb.append(field.getType().getName());
		sb.append(".");
		return sb.toString();
	}

	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {

		List<ContentProposal> proposals = new ArrayList<ContentProposal>();

		Matcher matcher = matchLastToken(contents);
		if (matcher.matches()) {
			if (".".equals(matcher.group(1))) {
				// code tries to call a class method, not a function
				// do nothing
			} else {
				for (ModuleDefinition definition : fLoadedModules) {
					// add fields from modules
					for (Field field : definition.getFields()) {
						if ((field.getName().startsWith(matcher.group(2))) && (matcher.group(2).length() < field.getName().length()))
							proposals.add(new ContentProposal(field.getName().substring(matcher.group(2).length()), field.getName(), getDescription(field, definition)));
					}

					// add methods from modules
					for (Method method : definition.getMethods()) {
						if ((method.getName().startsWith(matcher.group(2))) && (matcher.group(2).length() < method.getName().length()))
							proposals.add(new ContentProposal(method.getName().substring(matcher.group(2).length()) + "()", method.getName() + "()", getDescription(method, definition)));
					}
				}
			}
		}

		// allow implementers to modify proposal list
		modifyProposals(proposals, contents);

		// sort proposals
		Collections.sort(proposals, new Comparator<ContentProposal>() {

			@Override
			public int compare(final ContentProposal arg0, final ContentProposal arg1) {
				return arg0.getLabel().compareTo(arg1.getLabel());
			}
		});

		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

	protected abstract void modifyProposals(Collection<ContentProposal> proposals, String contents);

	@Override
	public char[] getActivationChars() {
		return null;
	}

	@Override
	public void addCode(final String code, IScriptEngine engine) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		Collection<String> modules = getModuleNames(code);
		while (!modules.isEmpty()) {
			String candidate = modules.iterator().next();
			modules.remove(candidate);

			String fullName = ModuleHelper.resolveName(candidate);
			ModuleDefinition definition = scriptService.getAvailableModules().get(fullName);
			if (definition != null) {
				fLoadedModules.add(definition);

				// add dependencies to list
				for (String moduleID : definition.getDependencies())
					modules.add(scriptService.getModuleDefinition(moduleID).getPath().toString());
			}
		}
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
	 * @param data
	 *            current line of code left from cursor
	 * @return matcher containing content assist information
	 */
	protected Matcher matchLastToken(final String data) {
		return LINE_DATA_PATTERN.matcher(data);
	}
}
