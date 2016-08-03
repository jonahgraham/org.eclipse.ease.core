/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class JavaScriptCodeFactoryTest {
	private JavaScriptCodeFactory fFactory;

	@Before
	public void setup() {
		fFactory = new JavaScriptCodeFactory();
	}

	@Test
	public void testCommentCreator() {
		assertEquals("// Comment", fFactory.createCommentedString("Comment"));
		assertEquals(String.format("// Multi%n// Line%n// Comment"), fFactory.createCommentedString("Multi\nLine\nComment"));
		assertEquals(String.format("/*Multi%nLine%nComment*/"), fFactory.createCommentedString("Multi\nLine\nComment", true));
	}

}
