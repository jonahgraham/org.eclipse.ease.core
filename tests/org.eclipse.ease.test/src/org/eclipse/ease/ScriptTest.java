package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.tools.ResourceTools;
import org.junit.Before;
import org.junit.Test;

public class ScriptTest {

	private static final String SAMPLE_CODE = "print('Hello world');";
	private IFile fFile;

	@Before
	public void setUp() throws Exception {
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

		final IProject project = workspace.getProject("Sample project");
		if (!project.exists())
			project.create(null);

		if (!project.isOpen())
			project.open(null);

		fFile = project.getFile("Script.js");
		if (!fFile.exists())
			fFile.create(new ByteArrayInputStream(SAMPLE_CODE.getBytes("UTF-8")), false, null);
	}

	@Test
	public void codeFromString() throws Exception {
		final Script script = new Script(SAMPLE_CODE);
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromInputStream() throws Exception {
		final Script script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromReader() throws Exception {
		final Script script = new Script(new StringReader(SAMPLE_CODE));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromScriptable() throws Exception {
		final Script script = new Script(new IScriptable() {

			@Override
			public InputStream getSourceCode() throws Exception {
				return new ByteArrayInputStream(SAMPLE_CODE.getBytes());
			}
		});
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromWorkspaceFile() throws Exception {
		final Script script = new Script(fFile);
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromFilesystemFile() throws Exception {
		final Script script = new Script(fFile.getLocation().toFile());
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromStringBuilder() throws Exception {
		final Script script = new Script(new StringBuilder(SAMPLE_CODE));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void getCodeStream() throws IOException, Exception {
		final Script script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertEquals(SAMPLE_CODE, ResourceTools.toString(script.getCodeStream()));

		// try a second time to see that stream is recreated
		assertEquals(SAMPLE_CODE, ResourceTools.toString(script.getCodeStream()));
	}

	@Test
	public void getCommand() {
		final Script script = new Script(fFile);
		assertEquals(fFile, script.getCommand());
	}

	@Test
	public void getFile() {
		Script script = new Script(fFile);
		assertEquals(fFile, script.getFile());

		script = new Script(fFile.getLocation().toFile());
		assertEquals(fFile.getLocation().toFile(), script.getFile());

		script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertNull(script.getFile());
	}

	@Test
	public void getTitle() {
		Script script = new Script(fFile);
		assertNull(script.getTitle());

		script = new Script("Script title", fFile);
		assertEquals("Script title", script.getTitle());
	}

	@Test
	public void getResult() {
		final Script script = new Script(fFile);
		assertNotNull(script.getResult());

		script.setResult("result");
		assertEquals("result", script.getResult().getResult());
	}

	@Test
	public void getException() {
		final Script script = new Script(fFile);

		script.setException(new Exception());
		assertTrue(script.getResult().hasException());
	}
}
