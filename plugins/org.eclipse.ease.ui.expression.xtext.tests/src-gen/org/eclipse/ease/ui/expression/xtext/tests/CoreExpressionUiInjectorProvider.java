/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.ui.expression.xtext.tests;

import org.eclipse.xtext.junit4.IInjectorProvider;

import com.google.inject.Injector;

public class CoreExpressionUiInjectorProvider implements IInjectorProvider {

	public Injector getInjector() {
		return org.eclipse.ease.ui.expression.xtext.ui.internal.CoreExpressionActivator.getInstance().getInjector("org.eclipse.ease.ui.expression.xtext.CoreExpression");
	}

}
