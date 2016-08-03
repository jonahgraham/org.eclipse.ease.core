/*******************************************************************************
 * Copyright (c) 2013, 2016 Atos and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

public abstract class AbstractCodeFactory implements ICodeFactory {

	/** Default line break character. */
	public static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	public String createKeywordHeader(Map<String, String> keywords, String existingHeader) {
		StringBuilder header = new StringBuilder();

		// copy existing text before keywords
		if (existingHeader == null)
			existingHeader="";
		
		String[] existingLines = existingHeader.split("\\r?\\n");
		int index = 0;
		for (; index < existingLines.length; index++) {
			final Matcher matcher = AbstractCodeParser.PARAMETER_PATTERN.matcher(existingLines[index]);
			if (!matcher.matches())
				header.append(existingLines[index]).append(LINE_DELIMITER);
			else
				break;
		}

		// add line delimiter before keywords
		if ((header.length() > 0) && (!keywords.isEmpty()))
			header.append(LINE_DELIMITER);

		// add keywords
		for (final Entry<String, String> entry : keywords.entrySet()) {
			header.append(entry.getKey());
			header.append(new String(new char[Math.max(16 - entry.getKey().length(), 1)]).replace('\0', ' '));

			header.append(": ");
			header.append(entry.getValue()).append(LINE_DELIMITER);
		}

		// copy existing text after keywords
		boolean isFirstLineAfterKeywords = true;
		for (; index < existingLines.length; index++) {
			final Matcher matcher = AbstractCodeParser.PARAMETER_PATTERN.matcher(existingLines[index]);
			if (!matcher.matches()) {
				if (isFirstLineAfterKeywords) {
					header.append(LINE_DELIMITER);
					isFirstLineAfterKeywords = false;
				}

				header.append(existingLines[index]).append(LINE_DELIMITER);
			}
		}

		return header.toString();
	}

	/**
	 * Get the parameter name from a annotation. Use for engine which can have named variable
	 *
	 * @param parameterAnnotations
	 * @return
	 */
	protected String getParameterName(final Annotation[] parameterAnnotations) {
		for (final Annotation annot : parameterAnnotations) {
			if (annot instanceof ScriptParameter) {
				final ScriptParameter namedParameter = (ScriptParameter) annot;
				return namedParameter.name();
			}
		}
		return null;
	}

	@Override
	public String getDefaultValue(final Parameter parameter) {
		final String defaultStringValue = parameter.getDefaultValue().replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n");
		final Class<?> clazz = parameter.getClazz();

		// null as default value
		if (ScriptParameter.NULL.equals(defaultStringValue))
			return getNullString();

		// base datatypes
		if ((Integer.class.equals(clazz)) || (int.class.equals(clazz))) {
			try {
				return Integer.toString(Integer.parseInt(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Long.class.equals(clazz)) || (long.class.equals(clazz))) {
			try {
				return Long.toString(Long.parseLong(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Float.class.equals(clazz)) || (float.class.equals(clazz))) {
			try {
				return Float.toString(Float.parseFloat(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Double.class.equals(clazz)) || (double.class.equals(clazz))) {
			try {
				return Double.toString(Double.parseDouble(defaultStringValue));
			} catch (final NumberFormatException e1) {
			}
		}
		if ((Boolean.class.equals(clazz)) || (boolean.class.equals(clazz))) {
			return Boolean.parseBoolean(defaultStringValue) ? getTrueString() : getFalseString();
		}

		// undefined resolves to empty constructor
		if (ScriptParameter.UNDEFINED.equals(defaultStringValue)) {
			// look for empty constructor
			try {
				clazz.getConstructor();
				// empty constructor found, return class
				return classInstantiation(clazz, null);
			} catch (final SecurityException e) {
			} catch (final NoSuchMethodException e) {
			}
		}

		// look for string constructor
		try {
			clazz.getConstructor(String.class);
			// string constructor found, return class
			return classInstantiation(clazz, new String[] { "\"" + defaultStringValue + "\"" });
		} catch (final SecurityException e) {
		} catch (final NoSuchMethodException e) {
		}

		// special handling for string defaults passed to an Object.class
		if (clazz.isAssignableFrom(String.class))
			return classInstantiation(String.class, new String[] { "\"" + defaultStringValue + "\"" });

		return getNullString();
	}

	public static Collection<String> getMethodNames(final Method method) {
		final Set<String> methodNames = new HashSet<String>();
		methodNames.add(method.getName());

		final WrapToScript wrapAnnotation = method.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String name : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (!name.trim().isEmpty())
					methodNames.add(name.trim());
		}

		return methodNames;
	}

	public static String getPreExecutionCode(final IEnvironment environment, final Method method) {
		final StringBuffer code = new StringBuffer();

		for (final Object module : environment.getModules()) {
			if (module instanceof IScriptFunctionModifier)
				code.append(((IScriptFunctionModifier) module).getPreExecutionCode(method));
		}

		return code.toString();
	}

	public static String getPostExecutionCode(final IEnvironment environment, final Method method) {
		final StringBuffer code = new StringBuffer();

		for (final Object module : environment.getModules()) {
			if (module instanceof IScriptFunctionModifier)
				code.append(((IScriptFunctionModifier) module).getPostExecutionCode(method));
		}

		return code.toString();
	}

	/**
	 * Get string representation for <code>null</code> in target language.
	 *
	 * @return <code>null</code> in target language.
	 */
	protected abstract String getNullString();

	/**
	 * Get string representation for <code>true</code> in target language.
	 *
	 * @return <code>true</code> in target language.
	 */
	protected String getTrueString() {
		return Boolean.TRUE.toString();
	}

	/**
	 * Get string representation for <code>false</code> in target language.
	 *
	 * @return <code>false</code> in target language.
	 */
	protected String getFalseString() {
		return Boolean.FALSE.toString();
	}

	@Override
	public String createFunctionCall(final Method method, final Object... parameters) {
		final StringBuilder code = new StringBuilder();

		code.append(method.getName()).append('(');

		for (final Object parameter : parameters) {
			if (parameter instanceof String)
				code.append('"').append(((String) parameter).replace("\"", "\\\"")).append('"');
			else if (parameter == null)
				code.append(getNullString());
			else
				code.append(parameter.toString());

			code.append(", ");
		}

		// remove last comma separator
		if (parameters.length > 0)
			code.delete(code.length() - 2, code.length());

		code.append(");");

		return code.toString();
	}

	/**
	 * As many languages use //, provide a default of // style comments.
	 *
	 * @return "// "
	 */
	protected String getSingleLineCommentToken() {
		return "// ";
	}

	/**
	 * As many languages use /*, provide a default of /* style comments.
	 *
	 * @return "/*"
	 */
	protected  String getMultiLineCommentStartToken() {
		return "/*";
	}

	/**
	 * Default block comment end token.
	 *
	 * @return asterisk, dash
	 */
	protected String getMultiLineCommentEndToken() {
		return "*/";
	}

	/**
	 * Provide a default implementation that adds a single line comment token to the beginning of each line of the comment. Extenders can override
	 * {@link #getSingleLineCommentToken()} to return that token.
	 */
	@Override
	public String createCommentedString(String comment) {
		return createCommentedString(comment, false);
	}

	@Override
	public String createCommentedString(String comment, boolean addBlockComment) {
		if (addBlockComment) {
			return getMultiLineCommentStartToken() + comment + getMultiLineCommentEndToken();

		} else {
			String token = getSingleLineCommentToken();
			Stream<String> split = Pattern.compile("\r?\n").splitAsStream(comment);
			return split.map(s -> token + s).collect(Collectors.joining(System.lineSeparator()));
		}
	}
}
