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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.completion.ModuleCompletionProvider;
import org.eclipse.jface.fieldassist.ContentProposal;

@SuppressWarnings("rawtypes")
public class RhinoCompletionProvider extends ModuleCompletionProvider {

	private static final Pattern VARIABLES_PATTERN = Pattern.compile(".*?(\\p{Alnum}+)\\s*=\\s*[^=]+");
	private static final Pattern FUNCTION_PATTERN = Pattern.compile("function\\s+(\\p{Alpha}\\p{Alnum}*)\\(");

	protected static final List<String> CODE_DELIMITERS = new ArrayList<String>(Arrays.asList(")", "+", "-", "*", "/", "|", "&", "^", ","));

	private final Map<String, Class> mVariables = new TreeMap<String, Class>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, Class> mFunctions = new TreeMap<String, Class>(String.CASE_INSENSITIVE_ORDER);

	@Override
	public void addCode(final String code, final IScriptEngine engine) {
		// extract variables
		Matcher matcher = VARIABLES_PATTERN.matcher(code);
		while (matcher.find()) {
			Object var = engine.getVariable(matcher.group(1));
			if (var != null) {
				mVariables.put(matcher.group(1), var.getClass());
			} else {
				mVariables.put(matcher.group(1), null);
			}
		}

		// extract functions
		matcher = FUNCTION_PATTERN.matcher(code);
		while (matcher.find()) {
			Object func = engine.getVariable(matcher.group(1));
			if (func != null && (func instanceof Method)) {
				mFunctions.put(matcher.group(1), ((Method) func).getReturnType());
			} else {
				mFunctions.put(matcher.group(1), null);
			}
		}

		super.addCode(code, engine);
	}

	/**
	 * Splits the given line of code into a list of expressions to be evaluated one after the other.
	 * 
	 * TODO: refactor !!!
	 * 
	 * @param code
	 *            Line of code to be split into "call-chain".
	 * @return List of all tokens in call chain.
	 */
	protected List<String> splitChain(String code) {
		// Remove everything before equals sign (can be ignored)
		code = ltrim(code, "=");

		// Remove everything that is not part of current parentheses
		Stack<Integer> parantheses = new Stack<Integer>();
		for (int i = 0; i < code.length(); i++) {
			switch (code.charAt(i)) {
			case '(':
				parantheses.push(i);
				break;
			case ')':
				parantheses.pop();
				break;
			}
		}
		try {
			// Recursively call with part of interest
			return splitChain(code.substring(parantheses.pop() + 1));
		} catch (EmptyStackException e) {
			// ignore
		}

		// Remove everything after all code delimiters
//		for (String delimiter : CODE_DELIMITERS) {
//			code = ltrim(code, delimiter);
//		}

		if (code.endsWith(".")) {
			code += " ";
		}
		String[] chain = code.split("\\.");
		for (int i = 0; i < chain.length; i++) {
			chain[i] = chain[i].trim();
		}

		return new ArrayList<String>(Arrays.asList(chain));

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
			return input.substring(input.lastIndexOf(delimiter));
		}
		return input;
	}

	/**
	 * Checks if a given piece of code contains a method call.
	 * 
	 * TODO: add better checks, this is ridiculous.
	 * 
	 * @param code
	 *            piece of code to be analyzed.
	 * @return <code>true</code> if code-piece contains method call, <code>false</code> otherwise.
	 */
	protected boolean isCall(String code) {
		int openingParenthesesCount = code.length() - code.replace("(", "").length();
		if (openingParenthesesCount == 0) {
			return false;
		}

		int closingParenthesesCount = code.length() - code.replace(")", "").length();
		if (openingParenthesesCount != closingParenthesesCount) {
			return false;
		}

		if (code.length() < 3) {
			return false;
		}

		if (code.indexOf("(") >= code.indexOf(")")) {
			return false;
		}

		if (code.startsWith("(")) {
			return false;
		}

		return code.endsWith(")");
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

			if (clazz == null) {
				for (ModuleDefinition definition : fLoadedModules) {
					for (Method method : definition.getMethods()) {
						if (method.getName().equalsIgnoreCase(toEvaluate)) {
							return method.getReturnType();
						}
					}
				}
				return mFunctions.get(toEvaluate);
			}

			for (Method method : clazz.getMethods()) {
				if (method.getName().equalsIgnoreCase(toEvaluate)) {
					return method.getReturnType();
				}
			}
		} else {
			if (clazz == null) {
				for (ModuleDefinition definition : fLoadedModules) {
					for (Field field : definition.getFields()) {
						if (field.getName().equalsIgnoreCase(toEvaluate)) {
							return field.getType();
						}
					}
				}
				return mVariables.get(toEvaluate);
			}

			for (Field field : clazz.getFields()) {
				if (field.getName().equalsIgnoreCase(toEvaluate)) {
					return field.getType();
				}
			}
		}
		return null;
	}

	/**
	 * Gets human readable description of method for completion UI.
	 * 
	 * @param method
	 *            Method to get description from.
	 * @param clazz
	 *            Class for method.
	 * @return Human readable description of method.
	 */
	protected String getDescription(Method method, Class clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("Public method of class ");
		sb.append(clazz.getName());
		sb.append(".");
		sb.append("\n");
		sb.append("Signature and Overloads:\n");
		for (Method overload : clazz.getMethods()) {
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
	protected String getDescription(Field field, Class clazz) {
		// Get field modifiers.
		StringBuilder sb = new StringBuilder();
		sb.append("Public ");
		sb.append("member of class ");
		sb.append(clazz.getName());
		sb.append(" of type ");
		sb.append(field.getType().getName());
		sb.append(".");
		return sb.toString();
	}

	/**
	 * Gets human readable description of variable for completion UI.
	 * 
	 * @param variable
	 *            Variable to get description for.
	 * @return Human readable description of variable.
	 */
	protected String getDescription(Class variable) {
		StringBuilder sb = new StringBuilder();
		sb.append("Local variable of type ");
		sb.append(variable.getName());
		sb.append(".");
		return sb.toString();
	}

	@Override
	protected void modifyProposals(final Collection<ContentProposal> proposals, final String contents) {
		// Split string into all substrings to be parsed
		List<String> callChain = splitChain(contents);
		if (callChain.size() > 1) {
			// last evaluated item in call chain
			Class currentChainItem = null;

			// Iterate over call chain (last needs extra parsing
			for (int i = 0; i < (callChain.size() - 1); i++) {
				currentChainItem = getType(callChain.get(i), currentChainItem);
				if (currentChainItem == null) {
					break;
				}
			}

			// Finally get all matching fields and members for last item in call-chain
			if (currentChainItem != null) {
				String toMatch = callChain.get(callChain.size() - 1);
				Set<String> addedVariables = new HashSet<String>();
				for (Field field : currentChainItem.getFields()) {
					if ((field.getName().startsWith(toMatch)) && (toMatch.length() < field.getName().length())) {
						if (!addedVariables.contains(field.getName())) {
							addedVariables.add(field.getName());
							proposals.add(new ContentProposal(field.getName().substring(toMatch.length()), field.getName(), getDescription(field,
									currentChainItem)));
						}
					}
				}

				for (Method method : currentChainItem.getMethods()) {
					if ((method.getName().startsWith(toMatch)) && (toMatch.length() < method.getName().length())) {
						if (!addedVariables.contains(method.getName())) {
							addedVariables.add(method.getName());
							proposals.add(new ContentProposal(method.getName().substring(toMatch.length()) + "()", method.getName() + "()", getDescription(
									method, currentChainItem)));
						}
					}
				}
			}
		} else {
			String toEvaluate = callChain.get(0);
			// add variables
			for (String variable : mVariables.keySet()) {
				if ((variable.startsWith(toEvaluate)) && (toEvaluate.length() < variable.length())) {
					proposals.add(new ContentProposal(variable.substring(toEvaluate.length()), variable, getDescription(mVariables.get(variable))));
				}
			}

			// add functions
			for (String function : mFunctions.keySet()) {
				if ((function.startsWith(toEvaluate)) && (toEvaluate.length() < function.length())) {
					proposals.add(new ContentProposal(function.substring(toEvaluate.length()), function, getDescription(mFunctions.get(function))));
				}
			}
		}
	}
}
