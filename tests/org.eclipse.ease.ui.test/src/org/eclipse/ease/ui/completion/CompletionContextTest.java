/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CompletionContextTest {

	private CompletionContext fContext;

	@Before
	public void setUp() throws Exception {
		fContext = new CompletionContext(null, null) {

			@Override
			protected boolean isLiteral(final char candidate) {
				return (candidate == '"') || (candidate == '\'');
			}
		};
	}

	@Test
	public void replaceSimpleStringLiterals() {
		assertEquals("print('')", fContext.replaceStringLiterals("print('')"));
		assertEquals("print('')", fContext.replaceStringLiterals("print('Hello world')"));
		assertEquals("print('')", fContext.replaceStringLiterals("print('Hello \" world')"));

		assertEquals("print(\"\")", fContext.replaceStringLiterals("print(\"\")"));
		assertEquals("print(\"\")", fContext.replaceStringLiterals("print(\"Hello world\")"));
		assertEquals("print(\"\")", fContext.replaceStringLiterals("print(\"Hello ' world\")"));
	}

	@Test
	public void replaceEscapedStringLiterals() {
		assertEquals("print('')", fContext.replaceStringLiterals("print('Hello \\'world')"));
		assertEquals("print(\"\")", fContext.replaceStringLiterals("print(\"Hello \\\"world\")"));
	}

	@Test
	public void replaceMultipleStringLiterals() {
		assertEquals("print('', \"\")", fContext.replaceStringLiterals("print('', \"\")"));
		assertEquals("print('', \"\")", fContext.replaceStringLiterals("print('Hello world', \"Hello world\")"));
	}
}
