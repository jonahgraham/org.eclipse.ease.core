/*******************************************************************************
 * Copyright (c) 2016 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import org.eclipse.ease.modules.WrapToScript;

/**
 * Module only used for unit testing.
 */
public class SampleModule {

	/** PI constant. */
	@WrapToScript
	public static final double PI = 3.1415926;

	/**
	 * Provide sum of 2 variables.
	 *
	 * @param a
	 *            summand 1
	 * @param b
	 *            summand 2
	 * @return sum
	 */
	@WrapToScript
	public double sum(double a, double b) {
		return a + b;
	}

	/**
	 * Subtract b from a.
	 */
	@WrapToScript
	public double sub(double a, double b) {
		return a - b;
	}

	/**
	 * Multiply 2 values.
	 */
	@WrapToScript
	public double mul(double a, double b) {
		return a * b;
	}

	/**
	 * Divide a by b.
	 */
	@WrapToScript
	public double div(double a, double b) {
		return a / b;
	}
}
