package org.eclipse.ease.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RunnableWithResultTest {

	@Test
	public void testResult() {
		final String expected = "Hello world";

		final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

			@Override
			public void run() {
				setResult(expected);
			}
		};

		runnable.run();

		assertEquals(expected, runnable.getResult());
	}
}
