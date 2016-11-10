/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.Test;

public class ScriptModeEngineTest extends Py4JEngineTestBase {

	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, false);
	}

	@Test
	public void pythonInteger() throws Exception {
		assertEquals("42", printExpression("40 + 2"));
	}

	@Test
	public void pythonString() throws Exception {
		assertEquals("42", printExpression("'42'"));
	}

	@Test
	public void javaInteger() throws Exception {
		assertEquals("42", printExpression("java.lang.Integer(42)"));
	}

	@Test
	public void javaString() throws Exception {
		assertEquals("42", printExpression("java.lang.String('42')"));
	}

	@Test
	public void createJavaType() throws Exception {
		assertEquals(new java.io.File("/").toString(), printExpression("java.io.File('/')"));
	}

	@Test
	public void createEclipseClass() throws Exception {
		assertEquals(new org.eclipse.core.runtime.Path("/").toString(), printExpression("org.eclipse.core.runtime.Path('/')"));
	}

	@Test
	public void createPythonObject() throws Exception {
		assertThat(printExpression("object()"), startsWith("<object object at "));
	}

	@Test
	public void testMultiple() throws Exception {
		javaInteger();
		javaString();
		createJavaType();
		createEclipseClass();
		javaInteger();
		javaString();
	}

	@Test
	public void callExit() throws Exception {
		ScriptResult result = executeCode("print_('this should be output', False)\nexit()\nprint_('this should not appear')");
		assertResultIsNone(result);
		assertEquals("this should be output", fOutputStream.getAndClearOutput());
	}

	@Test
	public void getScriptEngine() throws Exception {
		assertEquals(Integer.toString(System.identityHashCode(fEngine)), printExpression("java.lang.System.identityHashCode(getScriptEngine())"));
	}

	@Test
	public void incompleteStatement() throws Exception {
		ScriptResult result = executeCode("def a():");
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
		assertNull(result.getResult());
	}

	@Test
	public void invalidSyntax() throws Exception {
		executeCode("1++");
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
	}

	@Test
	public void runtimeError() throws Exception {
		executeCode("a");
		assertThat(fErrorStream.getAndClearOutput(), containsString("NameError"));
	}

	@Test
	public void multiLineStatement() throws Exception {
		assertResultIsNone(executeCode("def a():\n\treturn 42"));
		assertEquals("42", printExpression("a()"));
	}

	@Test
	public void multiLinesOfCode() throws Exception {
		assertResultIsNone(executeCode("print_(1)\nprint_(2)"));
		assertEquals(String.format("1%n2%n"), fOutputStream.getAndClearOutput());
	}
}