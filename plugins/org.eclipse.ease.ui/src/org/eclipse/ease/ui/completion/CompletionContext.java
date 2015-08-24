/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
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
 * Simple data-storage implementation of {@link ICompletionContext}.
 */
public abstract class CompletionContext implements ICompletionContext {

	private static final Pattern JAVA_PACKAGE_PATTERN = Pattern.compile("([A-Za-z]+\\.?)+");

	private final IScriptEngine fScriptEngine;
	private Object fResource;
	private final ScriptType fScriptType;
	private String fOriginalCode = "";

	private final Map<Object, String> fIncludes = new HashMap<Object, String>();
	private Collection<ModuleDefinition> fLoadedModules = null;
	private Class<? extends Object> fReferredClazz;
	private String fFilter = "";
	private Type fType = Type.UNKNOWN;
	private CharSequence fPackage;

	private int fOffset;
	private int fSelectionRange;

	public CompletionContext(final IScriptEngine scriptEngine, final ScriptType scriptType) {
		fScriptEngine = scriptEngine;
		fScriptType = scriptType;
	}

	@Override
	public Type getType() {
		return fType;
	}

	@Override
	public Class<? extends Object> getReferredClazz() {
		return fReferredClazz;
	}

	public void calculateContext(final Object resource, String code, int offset, int selectionRange) {
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
	 * @param code
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
						fPackage = code.subSequence(0, dotDelimiter);
					}
				}
			}
		}
	}

	/**
	 * @param originalCode
	 * @return
	 */
	protected String simplifyCode() {
		// only operate on last line
		final int lineFeedPosition = getOriginalCode().lastIndexOf('\n');
		String code = (lineFeedPosition > 0) ? getOriginalCode().substring(lineFeedPosition) : getOriginalCode();
		code = code.trim();

		// simplify String literals
		if (isStringLiteral(code)) {
			// we are within a string literal, cannot simplify further
			fType = Type.STRING_LITERAL;
			return code;

		} else {
			// remove all literals with dummies for simpler parsing
			code = replaceStringLiterals(code);
		}

		// if we find an opening bracket with no closing bracket, we can forget about everything left from it
		final int openBracketIndex = code.lastIndexOf('(');
		final int closeBracketIndex = code.lastIndexOf(')');
		if (openBracketIndex > closeBracketIndex) {
			code = code.substring(openBracketIndex + 1);

			// if we had multiple parameters, discard previous ones
			if (code.contains(","))
				code = code.substring(code.lastIndexOf('.') + 1).trim();
		}

		// when parsing to the left: if we find a delimiter outside a bracket, we can discard everything left from it
		// eg: foo + some.class.call('parameter', 23); -> discard 'foo +'
		int offset = code.length();
		int bracketCount = 0;
		while (offset > 0) {
			offset--;

			final char c = code.charAt(offset);
			if (c == '(') {
				bracketCount--;
				continue;
			} else if (c == ')') {
				bracketCount++;
				continue;
			}

			if (bracketCount > 0)
				// ignore content within method calls
				continue;

			if ((Character.isJavaIdentifierPart(c)) || (c == '.'))
				continue;

			// some separation character, break
			break;
		}

		if (offset > 0)
			code = code.substring(offset + 1);

		return code;
	}

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
	protected Class<? extends Object> parseVariableType(String name) {

		final List<String> sources = new ArrayList<String>();
		sources.add(getOriginalCode());
		sources.addAll(getIncludedResources().values());

		final Pattern pattern = Pattern.compile("@type\\s([a-zA-Z\\.]+)\\s*$\\s*.*?" + name + "\\s*=", Pattern.MULTILINE);
		// final Pattern pattern = Pattern.compile("@type\\s([a-zA-Z\\.]+)\\s*.?" + name + "\\s*=", Pattern.DOTALL);

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

	// public static void main(String[] args) {
	// final String code = "\\t\\t// @type java.lang.String\\n\\t\\tvar foo = \"foo\";\\n\\t\\ta.";
	// // final Pattern pattern = Pattern.compile("@type\\s([a-zA-Z\\.]+)\\s*.?" + "foo" + "\\s*=", Pattern.DOTALL | Pattern.MULTILINE);
	// final Pattern pattern = Pattern.compile("@type\\s([a-zA-Z\\.]+)\\s*$\\s*.*?foo\\s*=", Pattern.MULTILINE);
	//
	// final Matcher matcher = pattern.matcher(code);
	// System.out.println(matcher.find());
	// }

	private Method getMethodDefinition(String code) {
		for (final ModuleDefinition definition : getLoadedModules()) {
			for (final Method method : definition.getMethods()) {
				if (method.getName().equals(code))
					return method;
			}
		}

		return null;
	}

	private Field getFieldDefinition(String code) {
		for (final ModuleDefinition definition : getLoadedModules()) {
			for (final Field field : definition.getFields()) {
				if (field.getName().equals(code))
					return field;
			}
		}

		return null;
	}

	protected Class<? extends Object> getVariableClazz(final String name) {
		if (getScriptEngine() != null) {
			final Object variable = getScriptEngine().getVariable(name);
			if (variable != null)
				return variable.getClass();
		}

		return null;
	}

	/**
	 * @param code
	 * @param i
	 * @return
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

		return offset + 1;
	}

	public boolean isStringLiteral(final String code) {
		final StringBuilder string = new StringBuilder(code);

		int position = 0;
		int next = -1;
		Character currentLiteral = null;

		do {
			// find next literal
			next = getNextLiteral(string, position);

			if (next >= 0) {
				position = next + 1;
				if ((next >= 1) && (string.charAt(next - 1) == '\\'))
					// ignore as character is escaped
					continue;

				if (currentLiteral == null)
					currentLiteral = string.charAt(next);

				else if (currentLiteral == string.charAt(next))
					currentLiteral = null;
			}

		} while ((next >= 0) && (position < string.length()));

		return currentLiteral != null;
	}

	private int getNextLiteral(final StringBuilder string, final int offset) {
		int next = -1;
		for (final char literal : getStringLiteralChars()) {
			final int pos = string.indexOf(Character.toString(literal), offset);
			if ((pos != -1) && ((next == -1) || (next > pos)))
				next = pos;
		}

		return next;
	}

	public String replaceStringLiterals(final String code) {
		final StringBuilder string = new StringBuilder(code);

		int position = 0;
		int next = -1;
		Character currentLiteral = null;
		int literalStart = -1;

		do {
			// find next literal
			next = getNextLiteral(string, position);

			if (next >= 0) {
				position = next + 1;
				if ((next >= 1) && (string.charAt(next - 1) == '\\'))
					// ignore as character is escaped
					continue;

				if (currentLiteral == null) {
					currentLiteral = string.charAt(next);
					literalStart = next;

				} else if (currentLiteral == string.charAt(next)) {
					currentLiteral = null;
					string.replace(literalStart, next + 1, Character.toString(getStringLiteralChars()[0]) + Character.toString(getStringLiteralChars()[0]));
					position = literalStart + 2;
				}
			}

		} while ((next >= 0) && (position < string.length()));

		return string.toString();
	}

	protected abstract char[] getStringLiteralChars();

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
}