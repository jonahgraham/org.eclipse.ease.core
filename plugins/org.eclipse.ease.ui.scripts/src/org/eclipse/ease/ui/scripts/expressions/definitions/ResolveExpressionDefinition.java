/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.expressions.definitions;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.Expressions;
import org.eclipse.core.internal.expressions.ResolveExpression;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class ResolveExpressionDefinition extends AbstractExpressionDefinition {

	private static final String VARIABLE = "variable";
	private static final String ARGUMENTS = "arguments";

	@Override
	public Expression toCoreExpression() {
		Object[] arguments;
		try {
			arguments = Expressions.parseArguments(getParameter(ARGUMENTS));
		} catch (final CoreException e) {
			return Expression.FALSE;
		}

		return new ResolveExpression(getParameter(VARIABLE), arguments);
	}
}
