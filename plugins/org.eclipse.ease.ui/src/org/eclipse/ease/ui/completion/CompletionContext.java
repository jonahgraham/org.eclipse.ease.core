/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *     Christian Pontesegger - rewrite of implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.text.Position;

/**
 * The context evaluates and stores information on the code fragment at a given cursor position.
 */
public abstract class CompletionContext implements ICompletionContext {

	public static class Bracket {

		private int fStart = -1;
		private int fEnd = -1;

		public Bracket(final int start, final int end) {
			fStart = start;
			fEnd = end;
		}
	}

	private static final Pattern JAVA_PACKAGE_PATTERN = Pattern.compile("([A-Za-z]+\\.?)+");

	private final IScriptEngine fScriptEngine;
	private Object fResource;
	private ScriptType fScriptType;
	private String fOriginalCode = "";

	private final Map<Object, String> fIncludes = new HashMap<Object, String>();
	private Collection<ModuleDefinition> fLoadedModules = null;
	private Class<? extends Object> fReferredClazz;
	private String fFilter = "";
	private Type fType = Type.UNKNOWN;
	private String fPackage;
	private String fCaller = "";
	private int fParameterOffset = -1;

	private int fOffset;
	private int fSelectionRange;

	/**
	 * Context constructor. A context is bound to a given script engine or script type.
	 *
	 * @param scriptEngine
	 *            script engine to evaluate
	 * @param scriptType
	 *            script type to evaluate
	 */
	public CompletionContext(final IScriptEngine scriptEngine, final ScriptType scriptType) {
		fScriptEngine = scriptEngine;
		fScriptType = scriptType;

		if ((fScriptType == null) && (fScriptEngine != null))
			fScriptType = fScriptEngine.getDescription().getSupportedScriptTypes().get(0);
	}

	@Override
	public Type getType() {
		return fType;
	}

	@Override
	public Class<? extends Object> getReferredClazz() {
		return fReferredClazz;
	}

	/**
	 * Calculate a context over a given code fragment.
	 *
	 * @param resource
	 *            base resource (eg. edited file)
	 * @param code
	 *            code fragment to evaluate
	 * @param offset
	 *            the offset within the provided document (usually code.length())
	 * @param selectionRange
	 *            amount of selected characters
	 */
	public void calculateContext(final Object resource, String code, final int offset, final int selectionRange) {
		fOffset = offset;
		fSelectionRange = selectionRange;
		fIncludes.clear();
		fLoadedModules = null;

		fResource = resource;
		fOriginalCode = code;
		fReferredClazz = null;
		fFilter = "";

		// process include() calls
		addInclude(getOriginalCode());

		// remove irrelevant parts
		code = simplifyCode();

		parseCode(code);
	}

	/**
	 * Try to evaluate the calling method or class.
	 *
	 * @param code
	 *            code fragment to parse
	 */
	protected void parseCode(final String code) {

		// sometimes simplifyCode() already detects the type correctly, no need for further parsing
		if (getType() == Type.UNKNOWN) {
			final int dotDelimiter = code.lastIndexOf('.');
			if (dotDelimiter == -1) {
				fReferredClazz = null;
				fType = code.endsWith(")") ? Type.UNKNOWN : Type.NONE;
				fFilter = code;

			} else {
				fFilter = code.substring(dotDelimiter + 1);
				fReferredClazz = getClazz(code.substring(0, dotDelimiter).trim());

				if (fReferredClazz == null) {
					// maybe we have a package
					if (JAVA_PACKAGE_PATTERN.matcher(code.subSequence(0, dotDelimiter)).matches()) {
						fType = Type.PACKAGE;
						fPackage = code.subSequence(0, dotDelimiter).toString();
					}
				}
			}
		}
	}

	/**
	 * Try to remove unnecessary information from code fragment to simplify parsing.
	 *
	 * @return simplified code fragment
	 */
	protected String simplifyCode() {
		// only operate on last line
		final int lineFeedPosition = getOriginalCode().lastIndexOf('\n');
		String code = (lineFeedPosition > 0) ? getOriginalCode().substring(lineFeedPosition) : getOriginalCode();
		code = code.trim();

		// remove all literals with dummies for simpler parsing: "some 'literal'" -> ""
		code = replaceStringLiterals(code);
		if (fType == Type.STRING_LITERAL) {
			// we are within a string literal, cannot simplify further

			// try to detect calling method
			if (!code.isEmpty()) {
				final int openingBracket = findMatchingBracket(code + ")", code.length());
				if (openingBracket != -1) {
					// caller found
					fCaller = code.substring(0, openingBracket);

					String callerParameters = code.substring(openingBracket + 1);
					callerParameters = removeMethodCalls(callerParameters);
					fParameterOffset = callerParameters.split(",").length - 1;
				}
			}

			return code;
		}

		// if we find an opening bracket with no closing bracket, we can forget about everything left from it
		final Collection<Bracket> brackets = matchBrackets(code, '(', ')');

		int truncatePosition = -1;
		for (final Bracket bracket : brackets) {
			if ((bracket.fStart >= 0) && (bracket.fEnd == -1)) {
				// found an open bracket
				truncatePosition = Math.max(truncatePosition, bracket.fStart + 1);
			}
		}

		// try to truncate parameters
		for (int pos = code.length() - 1; pos >= 0; pos--) {
			final char c = code.charAt(pos);
			if ((c == ' ') || (c == '\t') || (c == ',') || (c == '!') || (c == '=') || (c == '<') || (c == '>') || (c == '+') || (c == '-') || (c == '*')
					|| (c == '/') || (c == '%') || (c == '&') || (c == '|') || (c == '^')) {

				// we have a separation character (operator, comma)
				if (getBracket(brackets, pos) == null) {
					// outside of a closed bracket, therefore we can truncate here
					truncatePosition = Math.max(truncatePosition, pos + 1);

					// parsing further to the left is pointless as truncatePosition cannot get bigger anymore
					break;
				}
			}
		}

		if (truncatePosition != -1)
			code = code.substring(truncatePosition);

		return code;
	}

	private static int countOccurrence(final String string, final char character) {
		int count = 0;
		for (final char c : string.toCharArray()) {
			if (c == character)
				count++;
		}

		return count;
	}

	/**
	 * Remove all brackets from method calls along with their content. Eg. transforms "some(call() + 3, test()), another(4)" to "some, another".
	 *
	 * @param code
	 *            string to parse
	 * @return transformed string
	 */
	private static String removeMethodCalls(String code) {
		int closingBracket = code.lastIndexOf(')');
		while (closingBracket != -1) {
			final int openingBracket = findMatchingBracket(code, closingBracket);
			if (openingBracket != -1)
				code = code.substring(0, openingBracket) + code.substring(closingBracket + 1);
			else
				// error, no opening bracket, giving up
				return code;

			// find next location
			closingBracket = code.lastIndexOf(')');
		}

		return code;
	}

	/**
	 * Try to evaluate the class returned from a function or an object.
	 *
	 * @param code
	 *            code to evaluate
	 * @return class or <code>null</code>
	 */
	private Class<? extends Object> getClazz(String code) {
		code = code.trim();
		String parameters = null;
		if (code.endsWith(")")) {
			final int bracketOpenPosition = findMatchingBracket(code, code.length() - 1);

			// extract parameters in case we have multiple candidates
			parameters = code.substring(bracketOpenPosition + 1, code.length() - 1);
			code = code.substring(0, bracketOpenPosition);
		}

		// lets try: invoke class
		try {
			final Class<?> clazz = CompletionContext.class.getClassLoader().loadClass(code);
			fType = (parameters != null) ? Type.CLASS_INSTANCE : Type.STATIC_CLASS;
			return clazz;

		} catch (final ClassNotFoundException e) {
			// did not work, we need to dig deeper
		}

		final int dotDelimiter = code.lastIndexOf('.');
		if (dotDelimiter == -1) {

			if (parameters != null) {
				// maybe a function call
				final Method method = getMethodDefinition(code);
				if (method != null) {
					fType = Type.CLASS_INSTANCE;
					return method.getReturnType();
				}

				// giving up
				return null;

			} else {
				// maybe a field
				final Field field = getFieldDefinition(code);
				if (field != null) {
					fType = Type.CLASS_INSTANCE;
					return field.getType();
				}

				// must be a script variable
				Class<? extends Object> clazz = getVariableClazz(code);
				if (clazz != null) {
					fType = Type.CLASS_INSTANCE;
					return clazz;
				}

				// maybe a variable and we find a definition somewhere in the previous code
				clazz = parseVariableType(code);
				if (clazz != null) {
					fType = Type.CLASS_INSTANCE;
					return clazz;
				}

				// giving up
				return null;
			}
		}

		final String keyWord = code.substring(dotDelimiter + 1);
		code = code.substring(0, dotDelimiter).trim();

		if (!code.isEmpty()) {
			final Class<? extends Object> clazz = getClazz(code);
			if (clazz != null) {
				if (parameters != null) {
					// searching for a method
					for (final Method method : clazz.getMethods()) {
						if (method.getName().matches(keyWord)) {
							fType = Type.CLASS_INSTANCE;
							return method.getReturnType();
						}
					}

				} else {
					// searching for a field
					for (final Field field : clazz.getFields()) {
						if (field.getName().matches(keyWord)) {
							fType = Type.CLASS_INSTANCE;
							return field.getType();
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Parse source code for a variable type definition. Type definitions are comments right before a variable definition in the form: // @type java.lang.String
	 *
	 * @param name
	 *            variable name to look up
	 * @return variable class type
	 */
	protected Class<? extends Object> parseVariableType(final String name) {

		final List<String> sources = new ArrayList<String>();
		sources.add(getOriginalCode());
		sources.addAll(getIncludedResources().values());

		final Pattern pattern = Pattern.compile("@type\\s([a-zA-Z\\.]+)\\s*$\\s*.*?" + Pattern.quote(name) + "\\s*=", Pattern.MULTILINE);

		for (final String source : sources) {
			final Matcher matcher = pattern.matcher(source);
			if (matcher.find()) {
				try {
					return CompletionContext.class.getClassLoader().loadClass(matcher.group(1));
				} catch (final ClassNotFoundException e) {
					// did not work, invalid definition, giving up
					return null;
				}
			}
		}

		return null;
	}

	/**
	 * Retrieve a method definition from loaded modules.
	 *
	 * @param code
	 *            method call to look up
	 * @return method definition or <code>null</code>
	 */
	private Method getMethodDefinition(final String code) {
		for (final ModuleDefinition definition : getLoadedModules()) {
			for (final Method method : definition.getMethods()) {
				if (method.getName().equals(code))
					return method;
			}
		}

		return null;
	}

	/**
	 * Retrieve a field definition from loaded modules.
	 *
	 * @param code
	 *            field to look up
	 * @return field definition or <code>null</code>
	 */
	private Field getFieldDefinition(final String code) {
		for (final ModuleDefinition definition : getLoadedModules()) {
			for (final Field field : definition.getFields()) {
				if (field.getName().equals(code))
					return field;
			}
		}

		return null;
	}

	/**
	 * Retrieve a variable from a running script engine.
	 *
	 * @param name
	 *            variable to look up
	 * @return variable class or <code>null</code>
	 */
	protected Class<? extends Object> getVariableClazz(final String name) {
		if (getScriptEngine() != null) {
			final Object variable = getScriptEngine().getVariable(name);
			if (variable != null)
				return variable.getClass();
		}

		return null;
	}

	/**
	 * Find the corresponding opening bracket to a closing bracket. Therefore we search the string to the left.
	 *
	 * @param code
	 *            code fragment to parse
	 * @param offset
	 *            offset position of the closing bracket
	 * @return offset of the corresponding opening bracket or -1
	 */
	private static int findMatchingBracket(final String string, int offset) {
		int openBrackets = 0;
		do {
			if (string.charAt(offset) == ')')
				openBrackets++;
			else if (string.charAt(offset) == '(')
				openBrackets--;

			offset--;

		} while ((openBrackets > 0) && (offset >= 0));

		return (openBrackets > 0) ? -1 : offset + 1;
	}

	/**
	 * Remove all string literal content and keep empty literals.
	 *
	 * @param code
	 *            code fragment to parse
	 * @return code fragment with empty string literals
	 */
	public String replaceStringLiterals(final String code) {
		final StringBuilder simplifiedString = new StringBuilder();
		final StringBuilder literalContent = new StringBuilder();

		Character currentLiteral = null;
		for (int index = 0; index < code.length(); index++) {
			if (isLiteral(code.charAt(index))) {
				if ((index == 0) || (code.charAt(index - 1) != '\\')) {
					// not escaped

					if (currentLiteral == null) {
						// start new literal
						currentLiteral = code.charAt(index);

					} else if (currentLiteral == code.charAt(index)) {
						// close literal
						literalContent.delete(0, literalContent.length());

						simplifiedString.append(currentLiteral);
						simplifiedString.append(currentLiteral);

						currentLiteral = null;
					}

					continue;
				}
			}

			// process character
			if (currentLiteral == null)
				simplifiedString.append(code.charAt(index));
			else
				literalContent.append(code.charAt(index));
		}

		if (currentLiteral != null) {
			fFilter = literalContent.toString();
			fType = Type.STRING_LITERAL;
		}

		return simplifiedString.toString();
	}

	/**
	 * See if a character matches a string literal token.
	 *
	 * @param candidate
	 *            character to test
	 * @return <code>true</code> when character is a string literal token
	 */
	protected abstract boolean isLiteral(final char candidate);

	private void addLoadedModules(final String code) {
		final List<Position> modulePositions = AbstractCompletionParser.findInvocations("loadModule(java.lang.String)", code);

		for (final Position position : modulePositions) {
			final String call = code.substring(position.getOffset(), position.getOffset() + position.getLength());
			final String[] parameters = AbstractCompletionParser.getParameters(call);
			if (parameters.length > 0) {
				final String candidate = parameters[0].trim();

				if (candidate.charAt(0) == candidate.charAt(candidate.length() - 1)) {
					// TODO add string literal characters lookup method
					if ((candidate.charAt(0) == '"') || (candidate.charAt(0) == '\'')) {
						// found loadModule, try to resolve
						final String moduleName = ModuleHelper.resolveName(candidate.substring(1, candidate.length() - 1));

						final IScriptService scriptService = ScriptService.getInstance();
						final ModuleDefinition moduleDefinition = scriptService.getAvailableModules().get(moduleName);
						if (moduleDefinition != null)
							addLoadedModule(moduleDefinition);
					}
				}
			}
		}
	}

	/**
	 * Add a module definition to the list of loaded modules. Will also add module dependencies to the list.
	 *
	 * @param definition
	 *            module definition to add
	 */
	private void addLoadedModule(final ModuleDefinition definition) {
		fLoadedModules.add(definition);

		// recursively load dependencies
		final IScriptService scriptService = ScriptService.getInstance();
		for (final String dependency : definition.getDependencies()) {
			final ModuleDefinition dependencyDefinition = scriptService.getAvailableModules().get(dependency);
			if (dependencyDefinition != null)
				addLoadedModule(dependencyDefinition);
		}
	}

	/**
	 * @param originalCode
	 */
	private void addInclude(final String code) {
		final List<Position> includePositions = AbstractCompletionParser.findInvocations("include(java.lang.String)", code);

		for (final Position position : includePositions) {
			try {
				final String call = code.substring(position.getOffset(), position.getOffset() + position.getLength());
				final String[] parameters = AbstractCompletionParser.getParameters(call);
				if (parameters.length > 0) {
					final String candidate = parameters[0].trim();

					if (candidate.charAt(0) == candidate.charAt(candidate.length() - 1)) {
						// TODO add string literal characters lookup method
						if ((candidate.charAt(0) == '"') || (candidate.charAt(0) == '\'')) {
							// found resource, try to resolve
							final Object includeResource = ResourceTools.resolveFile(candidate.substring(1, candidate.length() - 1), getResource(), true);
							if (includeResource != null) {
								if (!fIncludes.containsKey(includeResource)) {
									// store object & content, as we need to parse this content multiple times
									fIncludes.put(includeResource, ResourceTools.resourceToString(includeResource));

									// recursively process include files
									addInclude(fIncludes.get(includeResource));
								}
							}
						}
					}
				}
			} catch (final Exception e) {
				// ignore invalid include locations
			}
		}
	}

	@Override
	public String getOriginalCode() {
		return fOriginalCode;
	}

	@Override
	public String getProcessedCode() {
		return getOriginalCode();
	}

	@Override
	public Object getResource() {
		return fResource;
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fScriptEngine;
	}

	@Override
	public ScriptType getScriptType() {
		return fScriptType;
	}

	@Override
	public Collection<ModuleDefinition> getLoadedModules() {
		if (fLoadedModules == null) {
			// lazy loading of modules
			fLoadedModules = new HashSet<ModuleDefinition>();

			// add default environment module
			// use static service instance to enable unit tests in headless mode
			final IScriptService scriptService = ScriptService.getInstance();
			addLoadedModule(scriptService.getAvailableModules().get(EnvironmentModule.MODULE_NAME));

			// process loadModule() calls
			addLoadedModules(getOriginalCode());

			for (final String includeContent : fIncludes.values()) {
				// recursively process include files for loadModule() calls
				if (includeContent != null)
					addLoadedModules(includeContent);
			}

			// add loaded modules from script engine
			if (getScriptEngine() != null) {
				for (ModuleDefinition definition : ModuleHelper.getLoadedModules(getScriptEngine()))
					addLoadedModule(definition);
			}
		}

		return fLoadedModules;
	}

	@Override
	public Map<Object, String> getIncludedResources() {
		return fIncludes;
	}

	@Override
	public String getFilter() {
		return fFilter;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getSelectionRange() {
		return fSelectionRange;
	}

	@Override
	public String getPackage() {
		return fPackage;
	}

	@Override
	public String getCaller() {
		return fCaller;
	}

	@Override
	public int getParameterOffset() {
		return fParameterOffset;
	}

	private static Collection<Bracket> matchBrackets(final String code, final char openChar, final char closeChar) {
		final List<Bracket> brackets = new ArrayList<Bracket>();

		for (int pos = 0; pos < code.length(); pos++) {
			final char c = code.charAt(pos);
			if (c == openChar) {
				// push new Bracket
				brackets.add(0, new Bracket(pos, -1));

			} else if (c == closeChar) {
				boolean found = false;
				for (final Bracket bracket : brackets) {
					if (bracket.fEnd == -1) {
						bracket.fEnd = pos;
						found = true;
						break;
					}
				}

				if (!found)
					brackets.add(0, new Bracket(-1, pos));
			}
		}

		return brackets;
	}

	private static Bracket getBracket(final Collection<Bracket> brackets, final int pos) {
		for (final Bracket bracket : brackets) {
			if ((bracket.fStart != -1) && (bracket.fStart <= pos) && (bracket.fEnd != -1) && (bracket.fEnd > pos))
				return bracket;
		}

		return null;
	}
}