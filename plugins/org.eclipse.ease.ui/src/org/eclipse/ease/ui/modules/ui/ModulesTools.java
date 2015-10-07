/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.modules.ui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.ICodeFactory.Parameter;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.PlatformUI;

public class ModulesTools {

	private static Styler ITALIC_STYLE = new Styler() {
		private final Font italic = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);

		@Override
		public void applyStyles(final TextStyle textStyle) {
			textStyle.font = italic;
			textStyle.foreground = JFaceResources.getColorRegistry().get("QUALIFIER_COLOR");
		}
	};

	@Deprecated
	private ModulesTools() {
	}

	/**
	 * Generates the signature of the method. Optional parameters are written in italic.
	 *
	 * @param method
	 *            inspected method
	 * @return signature of method.
	 */
	public static StyledString getSignature(final Method method, final boolean useStyledReturnValue) {

		final StyledString signature = new StyledString();
		signature.append(method.getName());

		signature.append('(');
		final List<Parameter> parameters = ModuleHelper.getParameters(method);
		for (final Parameter parameter : parameters) {
			if (parameter.isOptional()) {
				signature.append(parameter.getClazz().getSimpleName(), ITALIC_STYLE);
			} else {
				signature.append(parameter.getClazz().getSimpleName());
			}

			if (!parameter.equals(parameters.get(parameters.size() - 1)))
				signature.append(", ");
		}
		signature.append(')');

		signature.append(" : " + method.getReturnType().getSimpleName(), (useStyledReturnValue) ? StyledString.DECORATIONS_STYLER : null);

		return signature;
	}

	/**
	 * Get the module owning the method provided.
	 *
	 * @param method
	 *            inspected method
	 * @return module containing the method or null.
	 */
	public static ModuleDefinition getDeclaringModule(final Method method) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(scriptService.getAvailableModules().values());

		// try for exact match
		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(method.getDeclaringClass()))
				return module;
		}

		// try for derived match
		for (final ModuleDefinition module : modules) {
			if (method.getDeclaringClass().isAssignableFrom(module.getModuleClass()))
				return module;
		}

		return null;
	}

	/**
	 * Get the module owning the field provided.
	 *
	 * @param field
	 *            inspected field
	 * @return module containing the field or null.
	 */
	public static ModuleDefinition getDeclaringModule(final Field field) {

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<ModuleDefinition>(scriptService.getAvailableModules().values());

		// try for exact match
		for (final ModuleDefinition module : modules) {
			if (module.getModuleClass().equals(field.getDeclaringClass()))
				return module;
		}

		// try for derived match
		for (final ModuleDefinition module : modules) {
			if (field.getDeclaringClass().isAssignableFrom(module.getModuleClass()))
				return module;
		}

		return null;
	}

	public static int getOptionalParameterCount(final Method method) {
		int optional = 0;

		for (final Annotation[] list : method.getParameterAnnotations()) {
			for (final Annotation annotation : list) {
				if ((annotation.annotationType().equals(ScriptParameter.class)) && (ScriptParameter.Helper.isOptional((ScriptParameter) annotation))) {
					optional++;
					break;
				}
			}
		}

		return optional;
	}
}
