package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.Before;
import org.junit.Test;

public class RhinoScriptEngineTest {
	private static final String SCRIPT_SNIPPET_1 = "foo = 40 + 2;";
	private static final String SCRIPT_SNIPPET_2 = "new java.lang.String(\"" + SCRIPT_SNIPPET_1 + "\");";
	private static final String SCRIPT_SNIPPET_3 = "throw new java.lang.Exception();";
	private static final String SCRIPT_SNIPPET_4 = "new org.eclipse.core.runtime.Path(\"/\");";

	private static final String SYNTAX_ERROR_SNIPPET = "'asdf";
	private static final String RUNTIME_ERROR_SNIPPET = "var x;\nx.foobar();\n}";

	private RhinoScriptEngine fEngine;

	@Before
	public void setUp() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
	}

	@Test
	public void simpleScriptCode() {
		ScriptResult result = executeCode(SCRIPT_SNIPPET_1);

		assertEquals(42, result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void accessJavaClasses() {
		ScriptResult result = executeCode(SCRIPT_SNIPPET_2);

		assertEquals(SCRIPT_SNIPPET_1, result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void accessEclipseClasses() {
		ScriptResult result = executeCode(SCRIPT_SNIPPET_4);

		assertEquals(new Path("/"), result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void callModuleCode() {
		ScriptResult result = executeCode("exit(\"done\");");

		assertEquals("done", result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void optionalModuleParameters() {
		ScriptResult result = executeCode("exit();");

		assertNull(result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void throwException() {
		ScriptResult result = executeCode(SCRIPT_SNIPPET_3);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
	}

	@Test
	public void verifySyntaxErrorException() {
		ScriptResult result = executeCode(SYNTAX_ERROR_SNIPPET);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
	}

	@Test
	public void verifyRuntimeErrorException() throws Exception {
		ScriptResult result = executeCode(RUNTIME_ERROR_SNIPPET);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
	}

	private ScriptResult executeCode(final Object code) {
		ScriptResult result = fEngine.executeAsync(code);
		fEngine.schedule();
		synchronized (result) {
			while (!result.isReady()) {
				try {
					result.wait();
				} catch (InterruptedException e) {
					return null;
				}
			}
		}

		return result;
	}
}
