package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScriptResultTest {

	private static final Object fResult = "done";
	private static final Exception fException = new Exception("some error");

	@Test
	public void isReady() {
		assertTrue(new ScriptResult(fResult).isReady());

		ScriptResult result = new ScriptResult();
		assertFalse(result.isReady());

		result.setResult(fResult);
		assertTrue(result.isReady());

		result = new ScriptResult();
		result.setException(new Exception());
		assertTrue(result.isReady());
	}

	@Test
	public void getResult() {
		final ScriptResult result = new ScriptResult(fResult);

		assertEquals(fResult, result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void getException() {
		final ScriptResult result = new ScriptResult();
		result.setException(fException);

		assertNull(result.getResult());
		assertEquals(fException, result.getException());
	}

	@Test
	public void hasException() {
		final ScriptResult result = new ScriptResult();
		assertFalse(result.hasException());

		result.setResult(fResult);
		assertFalse(result.hasException());

		result.setException(fException);
		assertTrue(result.hasException());
	}
}
