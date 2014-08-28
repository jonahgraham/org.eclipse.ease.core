package org.eclipse.ease.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Before;
import org.junit.Test;

public class ResourceToolsTest {

	private static final String TEST_FILE = "test.file";
	private static final String DOES_NOT_EXIST = "does_not_exist";
	private IWorkspaceRoot fWorkspace;
	private File fExistingFolder;
	private File fNonExistingResource;
	private File fExistingFile;
	private File fExistingSubFolder;

	@Before
	public void setUp() throws Exception {
		fWorkspace = ResourcesPlugin.getWorkspace().getRoot();

		fExistingSubFolder = fWorkspace.getRawLocation().toFile();
		fExistingFolder = fExistingSubFolder.getParentFile();
		fExistingFile = new File(fExistingFolder.getAbsolutePath() + File.separator + TEST_FILE);
		if (!fExistingFile.exists())
			fExistingFile.createNewFile();

		fNonExistingResource = new File(fExistingFolder.getAbsolutePath() + File.separator + DOES_NOT_EXIST);
	}

	@Test
	public void resolveInvalidFiles() {
		assertNull(ResourceTools.resolveFile(null, null, true));
		assertNull(ResourceTools.resolveFile(null, fExistingFolder.toString(), true));
		assertNull(ResourceTools.resolveFile(null, null, false));
		assertNull(ResourceTools.resolveFile(null, fExistingFolder.toString(), false));
	}

	@Test
	public void resolveInvalidFolders() {
		assertNull(ResourceTools.resolveFolder(null, null, true));
		assertNull(ResourceTools.resolveFolder(null, fExistingFolder.toString(), true));
		assertNull(ResourceTools.resolveFolder(null, null, false));
		assertNull(ResourceTools.resolveFolder(null, fExistingFolder.toString(), false));
	}

	@Test
	public void resolveAbsoluteFilesystemFiles() {
		// using paths
		assertNull(ResourceTools.resolveFile(fNonExistingResource.toString(), null, true));
		Object file = ResourceTools.resolveFile(fNonExistingResource.toString(), null, false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		assertNotNull(ResourceTools.resolveFile(fExistingFile.toString(), null, true));
		assertNotNull(ResourceTools.resolveFile(fExistingFile.toString(), null, false));

		// using URIs
		assertNull(ResourceTools.resolveFile(fNonExistingResource.toURI().toASCIIString(), null, true));
		file = ResourceTools.resolveFile(fNonExistingResource.toURI().toASCIIString(), null, false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		assertNotNull(ResourceTools.resolveFile(fExistingFile.toURI().toASCIIString(), null, true));
		assertNotNull(ResourceTools.resolveFile(fExistingFile.toURI().toASCIIString(), null, false));
	}

	@Test
	public void resolveRelativeFilesystemFiles() {
		assertNull(ResourceTools.resolveFile(DOES_NOT_EXIST, fExistingFolder.toString(), true));
		assertNull(ResourceTools.resolveFile(DOES_NOT_EXIST, fNonExistingResource.toString(), true));

		// relative to existing folder
		Object file = ResourceTools.resolveFile(DOES_NOT_EXIST, fExistingFolder.toString(), false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		// relative to existing file
		file = ResourceTools.resolveFile(DOES_NOT_EXIST, fExistingFile.toString(), false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		assertNotNull(ResourceTools.resolveFile(TEST_FILE, fExistingFolder.toString(), true));
		assertNotNull(ResourceTools.resolveFile(TEST_FILE, fExistingFile.toString(), true));

		assertNotNull(ResourceTools.resolveFile(TEST_FILE, fExistingFolder.toString(), false));
		assertNotNull(ResourceTools.resolveFile(TEST_FILE, fExistingFile.toString(), false));
	}

	@Test
	public void resolveAbsoluteFilesystemFolders() {
		assertNull(ResourceTools.resolveFolder(fNonExistingResource.toString(), null, true));
		Object file = ResourceTools.resolveFolder(fNonExistingResource.toString(), null, false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		assertNotNull(ResourceTools.resolveFolder(fExistingFolder.toString(), null, true));
		assertNotNull(ResourceTools.resolveFolder(fExistingFolder.toString(), null, false));
	}

	@Test
	public void resolveRelativeFilesystemFolders() {
		assertNull(ResourceTools.resolveFolder(DOES_NOT_EXIST, fExistingFolder.toString(), true));
		assertNull(ResourceTools.resolveFolder(DOES_NOT_EXIST, fNonExistingResource.toString(), true));

		Object file = ResourceTools.resolveFolder(DOES_NOT_EXIST, fExistingFolder.toString(), false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		file = ResourceTools.resolveFolder(DOES_NOT_EXIST, fExistingFile, false);
		assertNotNull(file);
		assertEquals(fNonExistingResource, file);

		assertNotNull(ResourceTools.resolveFolder(fExistingSubFolder.getName(), fExistingFolder.toString(), true));
		assertNotNull(ResourceTools.resolveFolder(fExistingSubFolder.getName(), fExistingFile.toString(), true));

		assertNotNull(ResourceTools.resolveFolder(fExistingSubFolder.getName(), fExistingFolder.toString(), false));
		assertNotNull(ResourceTools.resolveFolder(fExistingSubFolder.getName(), fExistingFile.toString(), false));
	}

	// @Test
	// public void testResolveFolder() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testToLocation() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetScriptTypeIFile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetScriptTypeFile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testExists() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetResource() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetInputStream() {
	// fail("Not yet implemented");
	// }

}
