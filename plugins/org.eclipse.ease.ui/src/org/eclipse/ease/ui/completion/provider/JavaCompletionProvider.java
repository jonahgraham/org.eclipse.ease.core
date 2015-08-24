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
package org.eclipse.ease.ui.completion.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

public class JavaCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return context.getReferredClazz() != null;
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		final Class<? extends Object> clazz = context.getReferredClazz();

		for (final Method method : clazz.getMethods()) {
			if ((context.getType() == Type.STATIC_CLASS) && (!Modifier.isStatic(method.getModifiers())))
				continue;

			final StyledString styledString = new StyledString(method.getName() + "(" + getMethodSignature(method) + ") : " + getMethodReturnType(method));
			styledString.append(" - " + method.getDeclaringClass().getSimpleName(), StyledString.DECORATIONS_STYLER);

			if (method.getParameterTypes().length > 0)
				addProposal(proposals, context, styledString, method.getName() + "(", getSharedImage(ISharedImages.IMG_OBJS_PUBLIC),
						ScriptCompletionProposal.ORDER_METHOD);
			else
				addProposal(proposals, context, styledString, method.getName() + "()", getSharedImage(ISharedImages.IMG_OBJS_PUBLIC),
						ScriptCompletionProposal.ORDER_METHOD);
		}

		for (final Field field : clazz.getFields()) {
			if ((context.getType() == Type.STATIC_CLASS) && (!Modifier.isStatic(field.getModifiers())))
				continue;

			final StyledString styledString = new StyledString(field.getName() + " : " + field.getType().getSimpleName());
			styledString.append(" - " + field.getDeclaringClass().getSimpleName(), StyledString.DECORATIONS_STYLER);

			addProposal(proposals, context, styledString, field.getName(), getSharedImage(ISharedImages.IMG_FIELD_PUBLIC),
					ScriptCompletionProposal.ORDER_FIELD);
		}

		return proposals;
	}

	public static String getMethodReturnType(final Method method) {
		final Class<?> returnType = method.getReturnType();
		return (returnType != null) ? returnType.getSimpleName() : "void";
	}

	public static String getMethodSignature(final Method method) {
		final StringBuilder result = new StringBuilder();

		for (final Class<?> type : method.getParameterTypes()) {
			if (result.length() > 0)
				result.append(", ");

			result.append(type.getSimpleName());
		}

		return result.toString();
	}

	public static ImageDescriptor getSharedImage(String name) {
		return JavaUI.getSharedImages().getImageDescriptor(name);
	}
}