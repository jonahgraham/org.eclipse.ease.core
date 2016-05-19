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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.Test;

public class ShellModeEngineTest extends Py4JEngineTestBase {

	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, true);
	}

	@Test
	public void simpleScriptCode() throws Exception {
		ScriptResult result = executeCode("40 + 2");
		assertNull(result.getException());
		assertEquals(42, result.getResult());
	}

	@Test
	public void createAutoConvertedJavaInteger() throws Exception {
		ScriptResult result = executeCode("java.lang.Integer(42)");
		assertNull(result.getException());
		assertEquals(42, result.getResult());
	}

	@Test
	public void createAutoConvertedJavaString() throws Exception {
		ScriptResult result = executeCode("java.lang.String('42')");
		assertNull(result.getException());
		assertEquals("42", result.getResult());
	}

	@Test
	public void createList() throws Exception {
		ScriptResult result = executeCode("[1, 2, 3]");
		assertNull(result.getException());
		assertEquals(Arrays.asList(1, 2, 3), result.getResult());
	}

	@Test
	public void createMap() throws Exception {
		ScriptResult result = executeCode("dict(a=1, b=2, c=3)");
		assertNull(result.getException());
		Map<Object, Object> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		assertEquals(map, result.getResult());
	}

	@Test
	public void createSet() throws Exception {
		ScriptResult result = executeCode("set([1, 2, 3])");
		assertNull(result.getException());
		Set<Object> set = new HashSet<>();
		set.add(1);
		set.add(2);
		set.add(3);
		assertEquals(set, result.getResult());
	}

	@Test
	public void createJavaType() throws Exception {
		ScriptResult result = executeCode("java.io.File('/')");
		assertNull(result.getException());
		assertEquals(new java.io.File("/"), result.getResult());
	}

	@Test
	public void createEclipseClass() throws Exception {
		ScriptResult result = executeCode("org.eclipse.core.runtime.Path('/')");
		assertNull(result.getException());
		assertEquals(new org.eclipse.core.runtime.Path("/"), result.getResult());
	}

	@Test
	public void createPythonObject() throws Exception {
		ScriptResult result = executeCode("object()");
		assertNull(result.getException());
		assertThat(result.getResult(), instanceOf(String.class));
		assertThat((String) result.getResult(), startsWith("<object object at "));
	}

	@Test
	public void testMultiple() throws Exception {
		simpleScriptCode();
		createJavaType();
		createEclipseClass();
		simpleScriptCode();
	}

	@Test
	public void callModuleCode() throws Exception {
		ScriptResult result = executeCode("exit(\"done\")");
		assertNull(result.getException());
		assertEquals("done", result.getResult());
	}

	@Test
	public void optionalModuleParameters() throws Exception {
		assertResultIsNone(executeCode("exit()"));
	}

	@Test
	public void getScriptEngine() throws Exception {
		ScriptResult result = executeCode("getScriptEngine()");
		assertNull(result.getException());
		assertSame(fEngine, result.getResult());
	}

	@Test
	public void print_() throws Exception {
		assertResultIsNone(executeCode("print_()"));
		assertEquals(String.format("%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	public void print_NoNewline() throws Exception {
		assertResultIsNone(executeCode("print_('', False)"));
		assertEquals("", fOutputStream.getAndClearOutput());
	}

	@Test
	public void print_Text() throws Exception {
		assertResultIsNone(executeCode("print_('text')"));
		assertEquals(String.format("text%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	public void print_TextNoNewline() throws Exception {
		assertResultIsNone(executeCode("print_('text', False)"));
		assertEquals("text", fOutputStream.getAndClearOutput());
	}

	@Test
	public void incompleteStatement() throws Exception {
		push("def a():", true);
	}

	@Test
	public void multiLineStatement() throws Exception {
		push("def a():", true);
		push("    return 42", true);
		assertResultIsNone(push("", false));
		ScriptResult result = push("a()", false);
		assertNull(result.getException());
		assertEquals(42, result.getResult());
	}

	@Test
	public void invalidSyntax() throws Exception {
		ScriptResult result = executeCode("1++");
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
		assertNull(result.getResult());
	}

	@Test
	public void runtimeError() throws Exception {
		ScriptResult result = executeCode("x");
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("NameError"));
		assertNull(result.getResult());
	}

}
