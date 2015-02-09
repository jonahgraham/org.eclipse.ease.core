package org.eclipse.ease.adapters;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.IScriptable;
import org.junit.Before;
import org.junit.Test;

public class ScriptableAdapterTest {

	private static final String PROJECT_NAME = "Sample_project";
	private static final String FOLDER_NAME = "Subfolder";
	private static final String FILE_NAME = "test_file.txt";

	private static final String URL_LOCATION = "http://download.eclipse.org/ease/update/release/artifacts.jar";

	private IWorkspaceRoot fWorkspace;
	private IProject fProject;
	private IFolder fFolder;
	private IFile fFile;
	private File fFsFile;
	private File fFsFolder;

	@Before
	public void setUp() throws Exception {

		// create workspace sample project
		fWorkspace = ResourcesPlugin.getWorkspace().getRoot();

		fProject = fWorkspace.getProject(PROJECT_NAME);
		if (!fProject.exists())
			fProject.create(null);

		if (!fProject.isOpen())
			fProject.open(null);

		fFolder = fProject.getFolder(FOLDER_NAME);
		if (!fFolder.exists())
			fFolder.create(0, true, null);

		fFile = fFolder.getFile(FILE_NAME);
		if (!fFile.exists())
			fFile.create(new ByteArrayInputStream("Hello world".getBytes("UTF-8")), false, null);

		fFsFile = fFile.getLocation().toFile();
		fFsFolder = fFsFile.getParentFile();
	}

	@Test
	public void adaptWorkspaceFile() {
		final Object adapter = new ScriptableAdapter().getAdapter(fFile, IScriptable.class);

		assertNotNull(adapter);
		assertTrue(IScriptable.class.isAssignableFrom(adapter.getClass()));
	}

	@Test
	public void adaptFilesystemFile() {
		final Object adapter = new ScriptableAdapter().getAdapter(fFsFile, IScriptable.class);

		assertNotNull(adapter);
		assertTrue(IScriptable.class.isAssignableFrom(adapter.getClass()));
	}

	@Test
	public void adaptURL() throws MalformedURLException {
		final Object adapter = new ScriptableAdapter().getAdapter(new URL(URL_LOCATION), IScriptable.class);

		assertNotNull(adapter);
		assertTrue(IScriptable.class.isAssignableFrom(adapter.getClass()));
	}

	@Test
	public void adaptURI() throws URISyntaxException {
		final Object adapter = new ScriptableAdapter().getAdapter(new URI(URL_LOCATION), IScriptable.class);

		assertNotNull(adapter);
		assertTrue(IScriptable.class.isAssignableFrom(adapter.getClass()));
	}

	@Test
	public void getAdapterList() {
		final Class[] adapterList = new ScriptableAdapter().getAdapterList();

		assertTrue(Arrays.asList(adapterList).contains(IScriptable.class));
	}
}
