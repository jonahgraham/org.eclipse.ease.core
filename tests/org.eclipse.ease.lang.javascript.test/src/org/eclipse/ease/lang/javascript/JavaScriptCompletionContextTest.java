package org.eclipse.ease.lang.javascript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.completion.CompletionContext;
import org.junit.Before;
import org.junit.Test;

public class JavaScriptCompletionContextTest {

	private static JavaScriptCompletionContext fContext;

	@Before
	public void setup() {
		fContext = new JavaScriptCompletionContext(null);
	}

	@Test
	public void verifyUnknownTypes() {
		fContext.calculateContext(null, "getScriptEngine()", 0, 0);
		assertEquals(CompletionContext.Type.UNKNOWN, fContext.getType());
	}

	@Test
	public void verifyNoneTypes() {
		fContext.calculateContext(null, "", 0, 0);
		assertEquals(CompletionContext.Type.NONE, fContext.getType());

		fContext.calculateContext(null, "get", 0, 0);
		assertEquals(CompletionContext.Type.NONE, fContext.getType());
	}

	@Test
	public void verifyStaticClassTypes() {
		fContext.calculateContext(null, "java.lang.String.", 0, 0);
		assertEquals(CompletionContext.Type.STATIC_CLASS, fContext.getType());

		fContext.calculateContext(null, "Packages.java.lang.String.", 0, 0);
		assertEquals(CompletionContext.Type.STATIC_CLASS, fContext.getType());
	}

	@Test
	public void verifyStringLiteralTypes() {
		fContext.calculateContext(null, "'Hello", 0, 0);
		assertEquals(CompletionContext.Type.STRING_LITERAL, fContext.getType());
		assertEquals("Hello", fContext.getFilter());
		assertEquals("", fContext.getCaller());

		fContext.calculateContext(null, "print('Hello", 0, 0);
		assertEquals(CompletionContext.Type.STRING_LITERAL, fContext.getType());
		assertEquals("Hello", fContext.getFilter());
		assertEquals("print", fContext.getCaller());

		fContext.calculateContext(null, "new java.lang.String('", 0, 0);
		assertEquals(CompletionContext.Type.STRING_LITERAL, fContext.getType());
		assertEquals("", fContext.getFilter());
		assertEquals("new java.lang.String", fContext.getCaller());
	}

	@Test
	public void verifyClassInstanceTypes() {
		fContext.calculateContext(null, "getScriptEngine().", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());

		fContext.calculateContext(null, "getScriptEngine().getInputStream().", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());
	}

	@Test
	public void verifyPackageTypes() {
		fContext.calculateContext(null, "java.lang.String", 0, 0);
		assertEquals(CompletionContext.Type.PACKAGE, fContext.getType());

		fContext.calculateContext(null, "Packages.java.lang.String", 0, 0);
		assertEquals(CompletionContext.Type.PACKAGE, fContext.getType());
	}

	@Test
	public void verifyTypesForScriptEngine() {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription engineDescription = scriptService.getEngine(JavaScriptHelper.SCRIPT_TYPE_JAVASCRIPT);
		assertNotNull("No JavaScript engine available", engineDescription);

		final IScriptEngine engine = engineDescription.createEngine();
		engine.setVariable("test", "Hello world");

		fContext = new JavaScriptCompletionContext(engine);

		fContext.calculateContext(null, "test.", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());

		fContext.calculateContext(null, "test.getByt", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());

		fContext.calculateContext(null, "test.toString().", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());
	}

	@Test
	public void definedVariableTypes() {
		fContext.calculateContext(null, "		// @type java.lang.String\n" + "		var a = \"foo\";\n" + "		a.", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());

		fContext.calculateContext(null, "		// @type java.lang.String\n" + "		var a = \"foo\";\n" + "		a.toString().", 0, 0);
		assertEquals(CompletionContext.Type.CLASS_INSTANCE, fContext.getType());
	}
}
