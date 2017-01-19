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
import org.eclipse.core.internal.expressions.AndExpression;
import org.eclipse.ease.ui.scripts.expressions.IExpressionDefinition;

@SuppressWarnings("restriction")
public class AndExpressionDefinition extends AbstractCompositeExpressionDefinition {

	@Override
	public Expression toCoreExpression() {
		final AndExpression coreExpression = new AndExpression();

		for (final IExpressionDefinition expression : getChildren())
			coreExpression.add(expression.toCoreExpression());

		return coreExpression;
	}
}
