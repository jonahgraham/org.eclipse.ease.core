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
import org.eclipse.core.internal.expressions.SystemTestExpression;

@SuppressWarnings("restriction")
public class SystemTestExpressionDefinition extends AbstractExpressionDefinition {

	protected static final String PROPERTY = "property";
	protected static final String VALUE = "value";

	@Override
	public Expression toCoreExpression() {
		return new SystemTestExpression(getParameter(PROPERTY), getParameter(VALUE));
	}
}
