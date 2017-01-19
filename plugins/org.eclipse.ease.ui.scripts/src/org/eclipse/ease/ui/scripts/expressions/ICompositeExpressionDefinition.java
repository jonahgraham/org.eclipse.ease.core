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
package org.eclipse.ease.ui.scripts.expressions;

import java.util.List;

public interface ICompositeExpressionDefinition extends IExpressionDefinition {

	boolean addChild(IExpressionDefinition expression);

	void removeChild(IExpressionDefinition element);

	List<IExpressionDefinition> getChildren();

	boolean acceptsChild();
}
