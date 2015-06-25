/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Martin Kloesch - extension to parsing logic
 *******************************************************************************/
package org.eclipse.ease.ui.completion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
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

@SuppressWarnings("rawtypes")
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

	/**
	 * List of all currently loaded modules.
	 */
	private final Collection<ModuleDefinition> fLoadedModules = new HashSet<ModuleDefinition>();

	/**
	 * Simple mapping from variable name to type.
	 * 
	 * Use {@link #setVariable(String, Class)} to add a new name/type pair to this map.
	 */
	private final Map<String, Class> mVariables = new TreeMap<String, Class>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Simple mapping from function name to information about corresponding function.
	 * 
	 * Use {@link #setFunction(String, Method) to add a new name/method pair to this map.
	 */
	private final Map<String, Method> mFunctions = new TreeMap<String, Method>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * Adds a new value to the internal map of variables or updates the value for an existing variable.
	 * 
	 * @param name
	 *            Name of variable whose type needs to be stored.
	 * @param type
	 *            Type of variable.
	 */
	protected void setVariable(String name, Class type) {
		mVariables.put(name, type);
	}

	/**
	 * Adds a new method to the internal map or updates the information of existing one.
	 * 
	 * @param name
	 *            Name of function.
	 * @param function
	 *            Information about function.
	 */
	protected void setFunction(String name, Method function) {
		mFunctions.put(name, function);
	}

	public ModuleCompletionProvider() {
		// add environment module
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		fLoadedModules.add(scriptService.getAvailableModules().get(EnvironmentModule.MODULE_NAME));
	}

	/**
	 * Trims everything from the given input String up to the last occurrence of given delimiter.
	 * 
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring after last occurrence of delimiter.
	 */
	protected static String ltrim(String input, String delimiter) {
		if (input.contains(delimiter)) {
			return input.substring(input.lastIndexOf(delimiter) + 1);
		}
		return input;
	}

	/**
	 * Trims everything from the given input String up to the first occurrence of given delimiter.
	 * 
	 * @param input
	 *            String to be trimmed.
	 * @param delimiter
	 *            Delimiter for String trimming.
	 * @return Substring up to first occurrence of delimiter.
	 */
	protected static String rtrim(String input, String delimiter) {
		if (input.contains(delimiter)) {
			return input.substring(0, input.indexOf(delimiter));
		}
		return input;
	}

	/**
	 * List of delimiters that can be ignored for completion.
	 * 
	 * Used by {@link #parseCallChain(String)} to only get necessary information about items in call chain.
	 */
	protected static final List<String> CODE_DELIMITERS = new ArrayList<String>(Arrays.asList("=", "+", "-", "*", "/", "|", "&", "^", ",", " "));

	/**
	 * Splits the given line of code into a list of expressions to be evaluated one after the other.
	 * 
	 * @param code
	 *            Line of code to be split into "call-chain".
	 * @return Queue of all tokens in call chain, <code>null</code> if code contains invalid syntax (more parentheses closed than opened).
	 */
	protected Queue<String> parseCallChain(String code) {

		// Remove everything that is not part of current parentheses
		Stack<Integer> parantheses = new Stack<Integer>();
		for (int i = 0; i < code.length(); i++) {
			switch (code.charAt(i)) {
			case '(':
				parantheses.push(i);
				break;
			case ')':
				try {
					parantheses.pop();
				} catch (EmptyStackException e) {
					// Invalid syntax, no completion possible
					return null;
				}
				break;
			}
		}
		try {
			// Recursively call with part of interest
			return parseCallChain(code.substring(parantheses.pop() + 1));
		} catch (EmptyStackException e) {
			// ignore
		}

		// Check if code ends with a call
		if (code.endsWith(").")) {
			// HACK: works because empty parentheses do not contain CODE_DELIMITERS
			code = rtrim(code, "(") + "().";
		}

		// Remove everything up to all code delimiters
		for (String delimiter : CODE_DELIMITERS) {
			code = ltrim(code, delimiter);
		}

		// In case code ends with '.' add a space to match against everything.
		if (code.endsWith(".")) {
			code += " ";
		}

		// Actually split the code
		Queue<String> chain = new LinkedList<String>();
		for (String current : code.split("\\.")) {
			chain.add(current.trim());
		}
		return chain;
	}

	/**
	 * Checks if a given piece of code contains a method call.
	 * 
	 * @param code
	 *            piece of code to be analyzed.
	 * @return <code>true</code> if code-piece contains method call, <code>false</code> otherwise.
	 */
	protected boolean isCall(String code) {
		// Code has to contain at least one (pair of) bracket
		if (!code.contains("(")) {
			return false;
		}

		int openedBrackets = 0;
		for (Character c : code.toCharArray()) {
			switch (c) {
			case '(':
				openedBrackets++;
				break;
			case ')':
				openedBrackets--;
				break;
			}

			// Negative bracket count means more closed than opened brackets
			if (openedBrackets < 0) {
				return false;
			}
		}

		// Code must not start with opening bracket but must end with closing bracket
		if (code.startsWith("(") || !code.endsWith(")")) {
			return false;
		}

		// Code must contain same amount of opening and closing brackets
		return openedBrackets == 0;
	}

	/**
	 * Parses the method name from a given piece of code. Piece of code must contain call.
	 * 
	 * @param code
	 *            piece of code to get method name of call from.
	 * @return function/method name if valid piece of code given, <code>null</code> otherwise.
	 */
	protected String parseFuncName(String code) {
		if (!isCall(code)) {
			return null;
		} else {
			return code.substring(0, code.indexOf("("));
		}
	}

	/**
	 * Gets the type for a given piece of code. Note that this can be either a call or just an identifier. This method can be used for both identifiers and
	 * function/method calls. With calls the result will be the return type.
	 * 
	 * @param toEvaluate
	 *            piece of code to get the type of.
	 * @return type of result of evaluation of code if successful. <code>null</code> otherwise.
	 * @see #getType(String, Class)
	 */
	protected Class getType(String toEvaluate) {
		return getType(toEvaluate, null);
	}

	/**
	 * Gets the type for a given piece of code. Note that this can be either a call or just an identifier. This method can be used for both identifiers and
	 * function/method calls. With calls the result will be the return type.
	 * 
	 * @param toEvaluate
	 *            piece of code to get the type of.
	 * @param clazz
	 *            class to check fields / methods against. If <code>null</code> given, the currently available variables / functions are evaluated.
	 * @return type of result of evaluation of code if successful. <code>null</code> otherwise.
	 */
	protected Class getType(String toEvaluate, Class clazz) {
		// Calls need to be handled differently.
		if (isCall(toEvaluate)) {
			toEvaluate = parseFuncName(toEvaluate);

			// If no class given check currently loaded modules
			if (clazz == null) {
				for (ModuleDefinition definition : fLoadedModules) {
					for (Method method : definition.getMethods()) {
						if (method.getName().equalsIgnoreCase(toEvaluate)) {
							return method.getReturnType();
						}
					}
				}
				Method local = mFunctions.get(toEvaluate);
				if (local != null) {
					return local.getReturnType();
				} else {
					return null;
				}
			} else {
				for (Method method : clazz.getMethods()) {
					if (method.getName().equalsIgnoreCase(toEvaluate)) {
						return method.getReturnType();
					}
				}
			}
		} else {
			// Again, if no class given check currently loaded modules
			if (clazz == null) {
				for (ModuleDefinition definition : fLoadedModules) {
					for (Field field : definition.getFields()) {
						if (field.getName().equalsIgnoreCase(toEvaluate)) {
							return field.getType();
						}
					}
				}
				return mVariables.get(toEvaluate);
			} else {

				for (Field field : clazz.getFields()) {
					if (field.getName().equalsIgnoreCase(toEvaluate)) {
						return field.getType();
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {

		List<ContentProposal> proposals = new ArrayList<ContentProposal>();

		// Split string into all substrings to be parsed
		// Example: foo.bar() -> [foo, bar()]
		// Example: foo.bar(1 == 2).baz -> [foo, bar(), baz]
		Queue<String> callChain = parseCallChain(contents);

		if (callChain != null) {
			if (callChain.size() > 1) {
				// Iterate over call-chain and get suiting methods/members
				addMatchesFromCallChain(callChain, proposals);
			} else {
				// Just use all local variables/functions + the ones from loaded modules.
				String toMatch = callChain.poll();

				addLocalMatches(toMatch, proposals);
				addModuleMatches(toMatch, proposals);
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

	/**
	 * Adds all matching local functions and variables to the given list of proposals.
	 * 
	 * @param toMatch
	 *            pattern to match functions and variables against.
	 * @param proposals
	 *            List of {@link ContentProposal} to append matches to.
	 */
	protected void addLocalMatches(String toMatch, final List<ContentProposal> proposals) {
		// Show overloads only once
		Set<String> addedVariables = new HashSet<String>();

		// add variables
		for (String variable : mVariables.keySet()) {
			if ((variable.startsWith(toMatch)) && (toMatch.length() < variable.length())) {
				if (!addedVariables.contains(variable)) {
					addedVariables.add(variable);
					proposals.add(new ContentProposal(variable.substring(toMatch.length()), variable, CompletionDescriptionFormatter.format(mVariables
							.get(variable))));
				}
			}
		}

		// add functions
		for (String function : mFunctions.keySet()) {
			if ((function.startsWith(toMatch)) && (toMatch.length() < function.length())) {
				if (!addedVariables.contains(function)) {
					addedVariables.add(function);
					proposals.add(new ContentProposal(function.substring(toMatch.length()), function, CompletionDescriptionFormatter.format(
							mFunctions.get(function), mFunctions.values())));
				}
			}
		}
	}

	/**
	 * Adds all matching methods and members from all loaded modules to the given list of proposals.
	 * 
	 * @param toMatch
	 *            pattern to match methods and members against.
	 * @param proposals
	 *            List of {@link ContentProposal} to append matches to.
	 */
	protected void addModuleMatches(String toMatch, final List<ContentProposal> proposals) {
		// Show overloads only once
		Set<String> addedVariables = new HashSet<String>();

		for (ModuleDefinition definition : fLoadedModules) {
			// add fields from modules
			for (Field field : definition.getFields()) {
				if ((field.getName().startsWith(toMatch)) && (toMatch.length() < field.getName().length())) {
					if (!addedVariables.contains(field.getName())) {
						addedVariables.add(field.getName());
						proposals.add(new ContentProposal(field.getName().substring(toMatch.length()), field.getName(), CompletionDescriptionFormatter.format(
								field, definition)));
					}
				}
			}

			// add methods from modules
			for (Method method : definition.getMethods()) {
				if ((method.getName().startsWith(toMatch)) && (toMatch.length() < method.getName().length())) {
					if (!addedVariables.contains(method.getName())) {
						addedVariables.add(method.getName());
						proposals.add(new ContentProposal(method.getName().substring(toMatch.length()) + "()", method.getName() + "()",
								CompletionDescriptionFormatter.format(method, definition)));
					}
				}
			}
		}
	}

	/**
	 * Adds all methods and members from call chain to the given list of proposals.
	 * 
	 * @param callChain
	 *            pattern to match methods and members against.
	 * @param proposals
	 *            List of {@link ContentProposal} to append matches to.
	 */
	protected void addMatchesFromCallChain(Queue<String> callChain, final List<ContentProposal> proposals) {
		// last evaluated item in call chain
		Class currentChainItem = null;

		// Iterate over call-chain until only 1 item left
		while (callChain.size() > 1) {
			currentChainItem = getType(callChain.poll(), currentChainItem);
		}

		String toMatch = callChain.poll();

		Set<String> addedVariables = new HashSet<String>();
		for (Field field : currentChainItem.getFields()) {
			if ((field.getName().startsWith(toMatch)) && (toMatch.length() < field.getName().length())) {
				if (!addedVariables.contains(field.getName())) {
					addedVariables.add(field.getName());
					proposals.add(new ContentProposal(field.getName().substring(toMatch.length()), field.getName(), CompletionDescriptionFormatter.format(
							field, currentChainItem)));
				}
			}
		}

		for (Method method : currentChainItem.getMethods()) {
			if ((method.getName().startsWith(toMatch)) && (toMatch.length() < method.getName().length())) {
				if (!addedVariables.contains(method.getName())) {
					addedVariables.add(method.getName());
					proposals.add(new ContentProposal(method.getName().substring(toMatch.length()) + "()", method.getName() + "()",
							CompletionDescriptionFormatter.format(method, currentChainItem)));
				}
			}
		}
	}

	/**
	 * Adds all proposals not covered by {@link #getProposals(String, int)} to the list of matching proposals.
	 * 
	 * Developers implementing this method should have a look to {@link #getProposals(String, int)} to avoid redundancy.
	 * 
	 * @param proposals
	 *            Already populated list of proposals.
	 * @param contents
	 *            Contents of line to get completion proposals for.
	 */
	protected abstract void modifyProposals(Collection<ContentProposal> proposals, String contents);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char[] getActivationChars() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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
