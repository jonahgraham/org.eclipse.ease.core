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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.ease.ScriptResult;
import org.junit.Ignore;
import org.junit.Test;

public class Py4JEngineTest extends Py4JEngineTestBase {
	@Test
	public void mixedMode() throws Exception {
		// define a function in shell mode
		push("def a(i):", true);
		push(" return i + 1", true);
		push("", false);
		// define b function script mode
		assertResultIsNone(executeCode("def b(i):\n\treturn i + 2", false));

		// try to use each function from the other mode
		assertEquals("11", printExpression("a(10)"));
		assertEquals("12", printExpression("b(10)"));

		ScriptResult result21 = executeCode("a(20)", true);
		assertNull(result21.getException());
		assertEquals(21, result21.getResult());
		ScriptResult result22 = executeCode("b(20)", true);
		assertNull(result22.getException());
		assertEquals(22, result22.getResult());
	}

	/**
	 * Test printing to the Python's standard output. This is different to where print_ outputs to.
	 *
	 * print() goes to stdout, which is gobbled and sent to the scriptengine's stream, this is done on a different thread.
	 *
	 * print_() (from /System/Environment) goes synchronously to the scriptengine's stream
	 *
	 * Therefore to check the output we have a race condition between this test code and the script engine's gobbler thread. The way we resolve the race
	 * condition is to cleanly shutdown the engined and then check the output.
	 */
	@Test
	@Ignore("Clean shutdown not reliable yet, See Bug 494034")
	public void print() throws Exception {
		assertResultIsNone(executeCode("from __future__ import print_function\nprint('text on python stdout', end='')\n", false));

		// need to shutdown the engine here to make sure we get output expected
		fEngine.setTerminateOnIdle(true);
		assertEngineTerminated(fEngine);

		assertEquals("text on python stdout", fOutputStream.getAndClearOutput());
	}

	/**
	 * Test that the Python process is being shutdown properly.
	 */
	@Test
	@Ignore("Clean shutdown not reliable yet, See Bug 494034")
	public void cleanShutdown() throws Exception {
		assertResultIsNone(executeCode("import atexit\n" + "import sys\n" + "atexit.register(lambda: sys.stdout.write('in atexit'))\n", false));

		assertEquals("", fOutputStream.getAndClearOutput());

		// need to shutdown the engine here to make sure we get output expected
		fEngine.setTerminateOnIdle(true);
		assertEngineTerminated(fEngine);

		assertEquals("in atexit", fOutputStream.getAndClearOutput());
	}
}
