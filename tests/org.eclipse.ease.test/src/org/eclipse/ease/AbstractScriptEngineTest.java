package org.eclipse.ease;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractScriptEngineTest {

	private static final String SAMPLE_CODE = "Hello world";

	private AbstractScriptEngine fTestEngine;

	@Before
	public void setup() {
		fTestEngine = new AbstractScriptEngine("Test engine") {

			@Override
			public void terminateCurrent() {
			}

			@Override
			public void registerJar(final URL url) {
			}

			@Override
			public String getSaveVariableName(final String name) {
				return null;
			}

			@Override
			protected boolean teardownEngine() {
				return true;
			}

			@Override
			protected boolean setupEngine() {
				return true;
			}

			@Override
			protected void internalSetVariable(final String name, final Object content) {
			}

			@Override
			protected Object internalRemoveVariable(final String name) {
				return null;
			}

			@Override
			protected boolean internalHasVariable(final String name) {
				return false;
			}

			@Override
			protected Map<String, Object> internalGetVariables() {
				return null;
			}

			@Override
			protected Object internalGetVariable(final String name) {
				return null;
			}

			@Override
			protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
				return script.getCommand();
			}
		};
	}

	@Test
	public void isJob() {
		assertTrue(fTestEngine instanceof Job);
	}

	@Test(timeout = 1000)
	public void executeAsync() {

		final ScriptResult result = fTestEngine.executeAsync(SAMPLE_CODE);
		assertFalse(result.isReady());

		fTestEngine.schedule();

		synchronized (result) {
			try {
				while (!result.isReady())
					result.wait();
			} catch (final InterruptedException e) {
			}
		}
		assertTrue(result.isReady());
	}

	@Test(timeout = 1000)
	public void executeSync() throws InterruptedException {

		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();

		final ScriptResult result = fTestEngine.executeSync(SAMPLE_CODE);
		assertTrue(result.isReady());

		fTestEngine.terminate();
	}

	@Test(timeout = 1000)
	public void inject() throws InterruptedException {
		assertEquals(SAMPLE_CODE, fTestEngine.inject(SAMPLE_CODE));
	}

	@Test
	public void streamsAvailable() {
		assertNotNull(fTestEngine.getOutputStream());
		assertNotNull(fTestEngine.getErrorStream());
		assertNotNull(fTestEngine.getInputStream());
	}

	@Test(timeout = 1000)
	public void terminateOnIdle() throws InterruptedException {
		fTestEngine.setTerminateOnIdle(true);
		fTestEngine.schedule();
		fTestEngine.join();

		// test valid if it terminates within the timeout period
	}

	@Test(timeout = 1000)
	public void terminateOnIdleAfterSchedule() throws InterruptedException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();
		while (true) {
			Thread.sleep(10);
			if (fTestEngine.getState() != Job.RUNNING) {
				// eclipse job has not started yet
				continue;
			}
			if (!fTestEngine.isIdle()) {
				// ease still has work to do in the job
				continue;
			}
			if (fTestEngine.getThread().getState() != Thread.State.WAITING) {
				// thread is still running, we want it to be waiting
				continue;
			}
			break;
		}
		fTestEngine.setTerminateOnIdle(true);
		fTestEngine.join();

		// test valid if it terminates within the timeout period
	}

	@Test
	public void keepRunningOnIdle() throws InterruptedException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.executeAsync(SAMPLE_CODE);
		fTestEngine.schedule();

		final ScriptResult result = fTestEngine.executeSync(SAMPLE_CODE);
		assertTrue(result.isReady());
	}

	@Test
	public void terminateEngine() {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();

		fTestEngine.terminate();

		assertEquals(Job.NONE, fTestEngine.getState());
	}

	@Test
	public void extractEmptyArguments() {
		assertEquals(0, AbstractScriptEngine.extractArguments(null).length);
		assertEquals(0, AbstractScriptEngine.extractArguments("").length);
		assertEquals(0, AbstractScriptEngine.extractArguments("    ").length);
		assertEquals(0, AbstractScriptEngine.extractArguments("\t\t").length);
	}

	@Test
	public void extractArguments() {
		assertArrayEquals(new String[] { "one" }, AbstractScriptEngine.extractArguments("one"));
		assertArrayEquals(new String[] { "one with spaces" }, AbstractScriptEngine.extractArguments("one with spaces"));
		assertArrayEquals(new String[] { "one", "and", "another" }, AbstractScriptEngine.extractArguments("one,and, another"));
	}
}
