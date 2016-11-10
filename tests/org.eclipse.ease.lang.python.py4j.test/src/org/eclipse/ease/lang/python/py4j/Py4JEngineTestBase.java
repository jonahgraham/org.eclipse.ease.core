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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptResult;
import org.junit.After;
import org.junit.Before;

public abstract class Py4JEngineTestBase extends EaseTestBase {

	protected IScriptEngine fEngine;
	protected ByteArrayPrintStream fErrorStream;
	protected ByteArrayPrintStream fOutputStream;

	@Before
	public void setUp() throws Exception {
		fEngine = createEngine();
		fEngine.setTerminateOnIdle(false);
		fErrorStream = ((ByteArrayPrintStream) fEngine.getErrorStream());
		fOutputStream = ((ByteArrayPrintStream) fEngine.getOutputStream());
	}

	@After
	public void tearDown() throws Exception {
		fEngine.setTerminateOnIdle(true);
		assertEngineTerminated(fEngine);
		fErrorStream.assertNoOutput();
		fOutputStream.assertNoOutput();
	}

	protected ScriptResult push(String line, boolean expectMore) throws Exception {
		ScriptResult result = executeCode(line, true);
		if (expectMore) {
			assertNull(result.getException());
			assertThat(result.getResult(), instanceOf(String.class));
			assertThat((String) result.getResult(), startsWith("..."));
		}
		return result;
	}

	protected ScriptResult executeCode(String code, boolean shellMode) throws Exception {
		Script scriptFromShell = new Script("test code", code, shellMode);
		return fEngine.executeSync(scriptFromShell);
	}

	/**
	 * Evaluate the given expression by printing the str and return the standard output.
	 */
	protected String printExpression(String expression) throws Exception {
		Script scriptFromShell = new Script("test code", "import sys; sys.stdout.write(str(" + expression + ")); sys.stdout.flush()", false);
		ScriptResult result = fEngine.executeSync(scriptFromShell);
		assertResultIsNone(result);
		/* TODO: Until Bug 493677 is resolved we need a delay here to ensure data has been received by the stream gobbler */
		Thread.sleep(1000);
		return fOutputStream.getAndClearOutput();
	}

}
