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
import org.eclipse.core.internal.expressions.NotExpression;

@SuppressWarnings("restriction")
public class NotExpressionDefinition extends AbstractCompositeExpressionDefinition {

	@Override
	public Expression toCoreExpression() {
		if (getChildren().size() == 1)
			return new NotExpression(getChildren().get(0).toCoreExpression());

		// true when no child elements are available
		return Expression.TRUE;
	}
}
