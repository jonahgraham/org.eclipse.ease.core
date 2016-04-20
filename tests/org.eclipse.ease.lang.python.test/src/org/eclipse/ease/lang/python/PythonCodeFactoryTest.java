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

package org.eclipse.ease.lang.python;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PythonCodeFactoryTest {
	private PythonCodeFactory fFactory;

	@Before
	public void setup() {
		fFactory = new PythonCodeFactory();
	}

	@Test
	public void testCommentCreator() {
		assertEquals("# Comment", fFactory.createCommentedString("Comment"));
		assertEquals(String.format("# Multi%n# Line%n# Comment"),
				fFactory.createCommentedString("Multi\nLine\nComment"));
	}

}
