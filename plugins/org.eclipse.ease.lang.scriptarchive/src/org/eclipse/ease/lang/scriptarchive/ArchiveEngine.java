package org.eclipse.ease.lang.scriptarchive;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.PlatformUI;

public class ArchiveEngine extends AbstractScriptEngine implements IScriptEngine {

	/**
	 * Unpack an archive into a workspace project.
	 *
	 * @param archive
	 *            archive to be unpacked
	 * @return local workspace project
	 * @throws CoreException
	 *             when project resources cannot be created
	 */
	private static IProject unpack(Object archive) throws CoreException {
		final IProject project = createProject("__EASE_ScriptArchive_");
		ResourceTools.unpackArchive(archive, project);

		return project;
	}

	/**
	 * Create a project in the workspace. The project will be opened and is ready to be used.
	 *
	 * @param prefix
	 *            prefix to be used for the project name
	 * @return created project instance
	 * @throws CoreException
	 *             on project creation errors
	 */
	private static IProject createProject(String prefix) throws CoreException {
		int count = 1;
		IProject candidate = ResourcesPlugin.getWorkspace().getRoot().getProject(prefix + count);
		while (candidate.exists()) {
			count++;
			candidate = ResourcesPlugin.getWorkspace().getRoot().getProject(prefix + count);
		}

		candidate.create(new NullProgressMonitor());
		candidate.open(new NullProgressMonitor());
		return candidate;
	}

	/**
	 * Get a file from an archive as an input stream. As the stream is returned directly, it needs to be closed by the calling method.
	 *
	 * @param archive
	 *            archive source: file, stream or uri
	 * @param filename
	 *            name of file within archive to look for
	 * @return {@link InputStream} or <code>null</code>
	 */
	public static InputStream getArchiveStream(Object archive, String filename) {
		final InputStream inputStream = ResourceTools.getInputStream(archive);
		if (inputStream != null) {
			final ZipInputStream stream = new ZipInputStream(new BufferedInputStream(inputStream));
			try {

				ZipEntry entry = stream.getNextEntry();
				while (entry != null) {
					IPath path = new Path(entry.getName());
					path = path.removeFirstSegments(1).makeAbsolute();

					if (new Path(filename).makeAbsolute().equals(path))
						return stream;

					entry = stream.getNextEntry();
				}
			} catch (final IOException e) {
				Logger.error(PluginConstants.PLUGIN_ID, "Could not read archive", e);

				if (stream != null) {
					try {
						stream.close();
					} catch (final IOException e1) {
					}
				}
			}
		}

		return null;
	}

	private static int countArchivedFiles(Object archive) {
		final InputStream inputStream = ResourceTools.getInputStream(archive);
		if (inputStream != null) {
			try (ZipInputStream stream = new ZipInputStream(new BufferedInputStream(inputStream))) {
				int count = 0;
				ZipEntry entry = stream.getNextEntry();
				while (entry != null) {
					count++;
					entry = stream.getNextEntry();
				}

				return count;

			} catch (final IOException e) {
			}
		}

		return -1;
	}

	public static Properties getManifest(Object archive) {
		final InputStream manifestStream = getArchiveStream(archive, "/META-INF/MANIFEST.MF");
		if (manifestStream != null) {

			try {
				final Properties properties = new Properties();
				properties.load(manifestStream);

				return properties;
			} catch (final IOException e) {
				Logger.error(PluginConstants.PLUGIN_ID, "Could not read archive", e);

			} finally {
				try {
					manifestStream.close();
				} catch (final IOException e) {
				}
			}
		}

		return null;
	}

	private IScriptEngine fInternalEngine;
	private Properties fManifest;
	private List<URL> fRegisteredJars = null;

	public ArchiveEngine() {
		super("Script Archive Engine");
	}

	@Override
	public void registerJar(final URL url) {
		if (fRegisteredJars == null)
			fRegisteredJars = new ArrayList<>();

		fRegisteredJars.add(url);
	}

	@Override
	protected void setupEngine() throws ScriptEngineException {
		if (!getScheduledScripts().isEmpty()) {
			final Script script = getScheduledScripts().get(0);
			final Object input = script.getCommand();

			fManifest = getManifest(input);
			if (fManifest != null) {
				// manifest found
				final Object mainScript = fManifest.get("Main-Script");
				if (mainScript != null) {
					final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
					final ScriptType scriptType = scriptService.getScriptType(mainScript.toString());
					if (scriptType != null) {
						final EngineDescription engineDescription = scriptService.getEngine(scriptType.getName());
						if (engineDescription != null) {
							fInternalEngine = engineDescription.createEngine();

							// setup registered jars
							if (fRegisteredJars != null) {
								for (final URL url : fRegisteredJars)
									fInternalEngine.registerJar(url);
							}

							// setup streams
							fInternalEngine.setInputStream(getInputStream());
							fInternalEngine.setOutputStream(getOutputStream());
							fInternalEngine.setErrorStream(getErrorStream());

							fInternalEngine.setVariable("__MANIFEST", fManifest);
						}
					}

				} else
					throw new ScriptEngineException("No Main-Script found in manifest");

			} else
				throw new ScriptEngineException("No META-INF/MANIFEST.MF found");
		}
	}

	@Override
	protected void teardownEngine() throws ScriptEngineException {
		if (fInternalEngine != null) {
			// normally this engine should already be terminated
			fInternalEngine.terminate();
			// release local engine
			fInternalEngine = null;
		}
	}

	@Override
	protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {

		final Object input = script.getCommand();
		final Object mainScript = fManifest.get("Main-Script");

		// see if we need to unpack the archive
		final Object mainScriptObject;

		final int archivedFiles = countArchivedFiles(input);
		if (archivedFiles > 2) {
			// there is more than the manifest and the main script file
			try {
				final IProject localProject = unpack(input);
				mainScriptObject = localProject.getFile(new Path(mainScript.toString()));

			} catch (final CoreException e) {
				throw new ScriptEngineException("Could not create temporary project", e);
			}

		} else {
			// execute directly from archive
			mainScriptObject = getArchiveStream(input, mainScript.toString());
		}

		if (mainScriptObject != null) {

			// executeSync() will automatically schedule the internal engine
			final ScriptResult result = fInternalEngine.executeSync(mainScriptObject);

			if (mainScriptObject instanceof InputStream) {
				try {
					((InputStream) mainScriptObject).close();
				} catch (final Exception e) {
				}
			} else if (mainScriptObject instanceof IFile) {
				// we had a local project, delete it
				final IProject project = ((IFile) mainScriptObject).getProject();
				project.delete(true, new NullProgressMonitor());
			}

			// unpack execution result
			if (result.hasException())
				throw result.getException();

			return result.getResult();

		} else
			throw new ScriptEngineException("Main-Script cannot be read");
	}

	@Override
	public void terminateCurrent() {
		if (fInternalEngine != null)
			fInternalEngine.terminateCurrent();
		else
			throw new RuntimeException("Not supported");
	}

	@Override
	public String getSaveVariableName(String name) {
		if (fInternalEngine != null)
			return fInternalEngine.getSaveVariableName(name);

		throw new RuntimeException("Not supported");
	}

	@Override
	protected Object internalGetVariable(String name) {
		throw new RuntimeException("Not supported");
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		throw new RuntimeException("Not supported");
	}

	@Override
	protected boolean internalHasVariable(String name) {
		throw new RuntimeException("Not supported");
	}

	@Override
	protected void internalSetVariable(String name, Object content) {
		if (fInternalEngine != null)
			fInternalEngine.setVariable(name, content);

		else
			throw new RuntimeException("Not supported");
	}

	@Override
	protected Object internalRemoveVariable(String name) {
		throw new RuntimeException("Not supported");
	}
}
