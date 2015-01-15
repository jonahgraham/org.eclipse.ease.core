package org.eclipse.ease.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Before;
import org.junit.Test;

public class ResourceToolsTest {

	// private static final String TEST_FILE = "test.file";
	// private static final String DOES_NOT_EXIST = "does_not_exist";
	private static final String PROJECT_NAME = "Sample_project";
	private static final String FOLDER_NAME = "Subfolder";
	private static final String FILE_NAME = "test_file.txt";
	private static final String NEW_FILE_NAME = "another_file.txt";

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
	public void resolveInvalidFiles() {
		assertNull(ResourceTools.resolveFile(null, null, true));
		assertNull(ResourceTools.resolveFile(null, fFolder, true));
		assertNull(ResourceTools.resolveFile(null, null, false));
		assertNull(ResourceTools.resolveFile(null, fFolder, false));

		assertNull(ResourceTools.resolveFile(NEW_FILE_NAME, null, true));
	}

	@Test
	public void resolveFileByString() {
		// absolute workspace
		assertEquals(fFile, ResourceTools.resolveFile("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME, null, true));
		// relative workspace
		assertEquals(fFile, ResourceTools.resolveFile(FILE_NAME, "workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME, true));
		assertEquals(fFile, ResourceTools.resolveFile(FILE_NAME, "workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, true));
		assertEquals(fFile, ResourceTools.resolveFile("project://" + FOLDER_NAME + "/" + FILE_NAME, "workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, true));

		// absolute filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(fFsFile.getAbsolutePath(), null, true));
		// relative filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(FILE_NAME, fFsFolder.getAbsolutePath(), true));
	}

	@Test
	public void resolveFileByURI() throws UnsupportedEncodingException {
		// absolute workspace
		assertEquals(fFile, ResourceTools.resolveFile(URI.create("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME), null, true));
		// relative workspace
		assertEquals(fFile, ResourceTools.resolveFile(FILE_NAME, URI.create("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME), true));
		assertEquals(fFile,
				ResourceTools.resolveFile("project://" + FOLDER_NAME + "/" + FILE_NAME, URI.create("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME), true));

		// absolute filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(fFsFile.toURI(), null, true));
		// relative filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(FILE_NAME, fFsFolder.toURI(), true));
	}

	@Test
	public void resolveFileByReference() {
		// absolute workspace
		assertEquals(fFile, ResourceTools.resolveFile(fFile, null, true));
		// relative workspace
		assertEquals(fFile, ResourceTools.resolveFile(FILE_NAME, fFolder, true));
		assertEquals(fFile, ResourceTools.resolveFile("project://" + FOLDER_NAME + "/" + FILE_NAME, fFolder, true));

		// absolute filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(fFsFile, null, true));
		// relative filesystem
		assertEquals(fFsFile, ResourceTools.resolveFile(FILE_NAME, fFsFolder, true));
	}

	@Test
	public void resolveNewFile() {
		assertNull(ResourceTools.resolveFile("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + NEW_FILE_NAME, null, true));
		assertEquals(fFolder.getFile(NEW_FILE_NAME), ResourceTools.resolveFile(NEW_FILE_NAME, fFolder, false));
	}

	@Test
	public void resolveInvalidFolders() {
		assertNull(ResourceTools.resolveFolder(null, null, true));
		assertNull(ResourceTools.resolveFolder(null, fFolder, true));
		assertNull(ResourceTools.resolveFolder(null, null, false));
		assertNull(ResourceTools.resolveFolder(null, fFolder, false));

		assertNull(ResourceTools.resolveFolder(NEW_FILE_NAME, null, true));
	}

	@Test
	public void resolveFolderByString() {
		// absolute workspace
		assertEquals(fFolder, ResourceTools.resolveFolder("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, null, true));
		// relative workspace
		assertEquals(fFolder, ResourceTools.resolveFolder(FOLDER_NAME, "workspace://" + PROJECT_NAME, true));
		assertEquals(fFolder, ResourceTools.resolveFolder("project://" + FOLDER_NAME, "workspace://" + PROJECT_NAME, true));

		// absolute filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(fFsFolder.getAbsolutePath(), null, true));
		// relative filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(FOLDER_NAME, fFsFolder.getParentFile().getAbsolutePath(), true));
	}

	@Test
	public void resolveFolderByURI() throws UnsupportedEncodingException {
		// absolute workspace
		assertEquals(fFolder, ResourceTools.resolveFolder(URI.create("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME), null, true));
		// relative workspace
		assertEquals(fFolder, ResourceTools.resolveFolder(FOLDER_NAME, URI.create("workspace://" + PROJECT_NAME), true));
		assertEquals(fFolder, ResourceTools.resolveFolder("project://" + FOLDER_NAME, URI.create("workspace://" + PROJECT_NAME), true));

		// absolute filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(fFsFolder.toURI(), null, true));
		// relative filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(FOLDER_NAME, fFsFolder.getParentFile().toURI(), true));
	}

	@Test
	public void resolveFolderByReference() {
		// absolute workspace
		assertEquals(fFolder, ResourceTools.resolveFolder(fFolder, null, true));
		// relative workspace
		assertEquals(fFolder, ResourceTools.resolveFolder(FOLDER_NAME, fProject, true));
		assertEquals(fFolder, ResourceTools.resolveFolder("project://" + FOLDER_NAME, fProject, true));

		// absolute filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(fFsFolder, null, true));
		// relative filesystem
		assertEquals(fFsFolder, ResourceTools.resolveFolder(FOLDER_NAME, fFsFolder.getParentFile(), true));
	}

	@Test
	public void resolveNewFolder() {
		assertNull(ResourceTools.resolveFolder("workspace://" + PROJECT_NAME + "/" + NEW_FILE_NAME, null, true));
		assertEquals(fFolder.getFolder(NEW_FILE_NAME), ResourceTools.resolveFolder(NEW_FILE_NAME, fFolder, false));
	}

	@Test
	public void toProjectRelativeLocation_invalid() {
		assertNull(ResourceTools.toProjectRelativeLocation(null, null));
	}

	@Test
	public void toProjectRelativeLocation_not_existing() {
		assertNull(ResourceTools.toProjectRelativeLocation("workspace://" + PROJECT_NAME + "/" + NEW_FILE_NAME, null));
		assertNull(ResourceTools.toProjectRelativeLocation("project://" + NEW_FILE_NAME, fFile));
	}

	@Test
	public void toProjectRelativeLocation_valid() {
		assertEquals("project://" + FOLDER_NAME + "/" + FILE_NAME, ResourceTools.toProjectRelativeLocation(fFile, null));
		assertEquals("project://" + FOLDER_NAME + "/" + FILE_NAME, ResourceTools.toProjectRelativeLocation(FILE_NAME, fFolder));

		assertEquals("project://" + FOLDER_NAME, ResourceTools.toProjectRelativeLocation(fFolder, null));
	}

	@Test
	public void toAbsoluteLocation_invalid() {
		assertNull(ResourceTools.toAbsoluteLocation(null, null));
	}

	@Test
	public void toAbsoluteLocation_not_existing() {
		// workspace files
		assertNull(ResourceTools.toAbsoluteLocation("workspace://" + PROJECT_NAME + "/" + NEW_FILE_NAME, null));
		assertNull(ResourceTools.toAbsoluteLocation("project://" + NEW_FILE_NAME, fFile));

		// file system files
		assertNull(ResourceTools.toAbsoluteLocation(fFsFile.getAbsolutePath() + File.separator + NEW_FILE_NAME, null));
	}

	@Test
	public void toAbsoluteLocation_valid() {
		assertEquals("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME, ResourceTools.toAbsoluteLocation(fFile, null));
		assertEquals("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME, ResourceTools.toAbsoluteLocation(FILE_NAME, fFolder));
		assertEquals("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, ResourceTools.toAbsoluteLocation(fFolder, null));

		assertEquals(fFsFile.toURI().toString(), ResourceTools.toAbsoluteLocation(FILE_NAME, fFsFolder));
		assertEquals(fFsFolder.toURI().toString(), ResourceTools.toAbsoluteLocation(fFsFolder, null));
	}

	@Test
	public void exists_valid() {
		assertTrue(ResourceTools.exists("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME));
		assertTrue(ResourceTools.exists(fFsFile.toURI()));

		// a folder is not a readable resource
		assertFalse(ResourceTools.exists(fFsFolder));
	}

	@Test
	public void exists_invalid() {
		assertFalse(ResourceTools.exists(null));

		assertFalse(ResourceTools.exists("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + NEW_FILE_NAME));
		assertFalse(ResourceTools.exists(fFsFile.getAbsolutePath() + File.separator + NEW_FILE_NAME));
		assertFalse(ResourceTools.exists(URI.create("http://localhost:9999/nofile.xml")));
	}

	@Test
	public void getResource_valid() {
		assertEquals(fFile, ResourceTools.getResource("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME));
		assertEquals(fFsFile, ResourceTools.getResource(fFsFile.toURI()));
		assertEquals(fFsFolder, ResourceTools.getResource(fFsFolder));

		assertEquals(URI.create("http://localhost:9999/nofile.xml"), ResourceTools.getResource(URI.create("http://localhost:9999/nofile.xml")));
		assertEquals(URI.create("http://localhost:9999/nofile.xml"), ResourceTools.getResource("http://localhost:9999/nofile.xml"));
		assertEquals(URI.create("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + NEW_FILE_NAME),
				ResourceTools.getResource("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + NEW_FILE_NAME));
	}

	@Test
	public void getResource_invalid() {
		assertNull(ResourceTools.getResource(null));
	}

	@Test
	public void getInputStream_valid() throws IOException {
		InputStream stream = ResourceTools.getInputStream("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME);
		assertNotNull(stream);
		if (stream != null) {
			assertTrue(stream.available() > 0);
			stream.close();
		}

		stream = ResourceTools.getInputStream(fFsFile.toURI());
		assertNotNull(stream);
		if (stream != null) {
			assertTrue(stream.available() > 0);
			stream.close();
		}
	}

	@Test
	public void getInputStream_invalid() {
		assertNull(ResourceTools.getInputStream(null));

		assertNull(ResourceTools.getInputStream("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + NEW_FILE_NAME));
		assertNull(ResourceTools.getInputStream(fFolder));
	}

	@Test
	public void toURI_valid() {
		assertEquals(fFile.getLocationURI(), ResourceTools.toURI(fFile.getLocation()));
	}

	@Test
	public void toURI_invalid() {
		assertNull(ResourceTools.toURI(null));
	}

	@Test
	public void toPath_valid() {
		assertEquals(fFile.getFullPath(), ResourceTools.toPath("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE_NAME));
	}

	@Test
	public void toPath_invalid() {
		assertNull(ResourceTools.toPath(null));
		assertNull(ResourceTools.toPath(""));
		assertNull(ResourceTools.toPath(NEW_FILE_NAME));
	}

	@Test
	public void toString_valid() throws IOException {
		assertEquals("", ResourceTools.toString(new StringReader("")));
		assertEquals(FILE_NAME, ResourceTools.toString(new StringReader(FILE_NAME)));

		assertEquals("", ResourceTools.toString(new ByteArrayInputStream(new byte[0])));
		assertEquals(FILE_NAME, ResourceTools.toString(new ByteArrayInputStream(FILE_NAME.getBytes())));
	}

	@Test
	public void toString_invalid() throws IOException {
		assertNull(ResourceTools.toString((InputStream) null));
		assertNull(ResourceTools.toString((Reader) null));
	}
}
