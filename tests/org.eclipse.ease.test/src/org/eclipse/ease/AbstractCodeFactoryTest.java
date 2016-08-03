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

package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.eclipse.ease.AbstractCodeFactory;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractCodeFactoryTest {

	@Test
	public void testCommentCreator() {
		AbstractCodeFactory factory = mock(AbstractCodeFactory.class, Mockito.CALLS_REAL_METHODS);
		assertEquals("// Comment", factory.createCommentedString("Comment"));
		assertEquals(String.format("// Multi%n// Line%n// Comment"), factory.createCommentedString("Multi\nLine\nComment"));
	}

	@Test
	public void testCommentCreatorCustomToken() {
		AbstractCodeFactory factory = mock(AbstractCodeFactory.class);

		when(factory.createCommentedString(anyString())).thenCallRealMethod();
		when(factory.createCommentedString(anyString(), Mockito.anyBoolean())).thenCallRealMethod();

		when(factory.getSingleLineCommentToken()).thenReturn("# ");
		assertEquals("# Comment", factory.createCommentedString("Comment"));
		assertEquals(String.format("# Multi%n# Line%n# Comment"), factory.createCommentedString("Multi\nLine\nComment"));

		when(factory.getMultiLineCommentStartToken()).thenReturn("/*");
		when(factory.getMultiLineCommentEndToken()).thenReturn("*/");
		assertEquals(String.format("/*Multi%nLine%nComment*/"), factory.createCommentedString("Multi\nLine\nComment", true));

		when(factory.getMultiLineCommentStartToken()).thenReturn("\"\"\"");
		when(factory.getMultiLineCommentEndToken()).thenReturn("\"\"\"");
		assertEquals(String.format("\"\"\"Multi%nLine%nComment\"\"\""), factory.createCommentedString("Multi\nLine\nComment", true));
	}

	@Test
	public void createNewKeywordHeader() {
		AbstractCodeFactory factory = mock(AbstractCodeFactory.class, Mockito.CALLS_REAL_METHODS);

		final HashMap<String, String> keywords = new HashMap<String, String>();
		keywords.put("first", "value");
		keywords.put("menu", "this is a menu entry");

		String header = factory.createKeywordHeader(keywords, null);
		
		
		
		assertTrue(header.contains("first           : value"));
		assertTrue(header.contains("menu            : this is a menu entry"));
	}
}
