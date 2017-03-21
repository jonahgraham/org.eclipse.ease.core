package org.eclipse.ease.lang.scriptarchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class ArchiveEngineTest {

	@Test
	public void zipFileRegistration() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.zip").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void jarFileRegistration() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.jar").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void sarFileRegistration() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void engineIdRegistration() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final EngineDescription engine = scriptService.getEngineByID("org.eclipse.ease.lang.scriptarchive.engine");
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void executeWithManifest() throws MalformedURLException, InterruptedException {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/manifest.sar");
		final ScriptResult result = engine.executeSync(location);

		assertEquals(42.0, Double.parseDouble(result.getResult().toString()), 0.1);
	}

	@Test
	public void executeWithIncludes() throws MalformedURLException, InterruptedException {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/with_includes.sar");
		final ScriptResult result = engine.executeSync(location);

		assertEquals(6.0, Double.parseDouble(result.getResult().toString()), 0.1);

		// make sure no temporary projects remain
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			assertFalse(project.getName().contains("__EASE"));
	}

	@Test
	public void executeWithErrors() throws MalformedURLException, InterruptedException {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/with_errors.sar");
		final ScriptResult result = engine.executeSync(location);

		assertTrue(result.hasException());

		// make sure no temporary projects remain
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			assertFalse(project.getName().contains("__EASE"));
	}
}
