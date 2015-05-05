/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.urlhandler.WorkspaceURLConnection;

public final class ResourceTools {

	private static final String PROJECT_SCHEME = "project";

	/**
	 * @deprecated
	 */
	@Deprecated
	private ResourceTools() {
	}

	/**
	 * Resolve a file from a given input location. Tries to resolve absolute and relative files within the workspace or file system. Relative files will be
	 * resolved against a provided parent location.
	 *
	 * @param location
	 *            file location to be resolved
	 * @param parent
	 *            location of parent resource
	 * @param exists
	 *            return file only if it exists, if <code>false</code> the file is returned whether it exists or not
	 * @return {@link IFile}, {@link File} or <code>null</code>
	 */
	public static Object resolveFile(final Object location, final Object parent, final boolean exists) {
		if (location == null)
			return null;

		final Object parentObject = resolveParent(parent);
		Object candidate = resolveAbsolute(location, parentObject, false);

		if ((candidate == null) && (parentObject != null))
			candidate = resolveRelativeFile(location, parentObject, exists);

		if (candidate instanceof IFile)
			return ((((IFile) candidate).exists()) || (!exists)) ? candidate : null;

		if ((candidate instanceof File) && ((((File) candidate).isFile()) || (!exists)))
			return ((((File) candidate).exists()) || (!exists)) ? candidate : null;

		// giving up
		return null;
	}

	/**
	 * Resolve a folder from a given input location. Tries to resolve absolute and relative folders within the workspace or file system. Relative folders will
	 * be resolved against a provided parent location.
	 *
	 * @param location
	 *            folder location to be resolved
	 * @param parent
	 *            location of parent resource
	 * @param exists
	 *            return folder only if it exists
	 * @return {@link IContainer}, {@link File} or <code>null</code>
	 */
	public static Object resolveFolder(final Object location, final Object parent, final boolean exists) {
		if (location == null)
			return null;

		final Object parentObject = resolveParent(parent);
		Object candidate = resolveAbsolute(location, parentObject, true);

		if ((candidate == null) && (parentObject != null))
			candidate = resolveRelativeFolder(location, parentObject, exists);

		if (candidate instanceof IContainer)
			return ((((IContainer) candidate).exists()) || (!exists)) ? candidate : null;

		if ((candidate instanceof File) && ((((File) candidate).isDirectory()) || (!exists)))
			return ((((File) candidate).exists()) || (!exists)) ? candidate : null;

		// giving up
		return null;
	}

	private static Object resolveParent(Object parent) {
		if ((parent != null) && (!(parent instanceof IResource)) && (!(parent instanceof File))) {
			final Object parentReference = parent;
			parent = resolveFile(parentReference, null, true);
			if (parent == null)
				parent = resolveFolder(parentReference, null, true);
		}

		if (parent instanceof IFile)
			return ((IFile) parent).getParent();

		if ((parent instanceof File) && (((File) parent).isFile()))
			return ((File) parent).getParentFile();

		return parent;
	}

	private static Object resolveAbsolute(Object location, final Object parent, final boolean isFolder) {
		if ((!isFolder) && (location instanceof IFile))
			return location;

		if ((isFolder) && (location instanceof IContainer))
			return location;

		if (location instanceof String) {
			// try to convert to an URI
			try {
				location = URI.create((String) location);
			} catch (final IllegalArgumentException e) {
				// throw on invalid URIs, ignore and continue with location as-is
			}
		}

		if (location instanceof URI) {
			// resolve file:// URIs
			try {
				location = new File((URI) location);
			} catch (final Exception e) {
				// URI scheme is not "file"
			}
		}

		if (location instanceof File)
			return location;

		// nothing of the previous, try to resolve
		final String reference = location.toString();

		if (reference.startsWith(PROJECT_SCHEME)) {
			// project relative link
			if (parent instanceof IResource) {
				final IProject project = ((IResource) parent).getProject();
				if (project != null) {
					if (isFolder)
						return project.getFolder(new Path(reference.substring(PROJECT_SCHEME.length() + 2)));
					else
						return project.getFile(new Path(reference.substring(PROJECT_SCHEME.length() + 2)));
				}
			}

		} else if (reference.startsWith(WorkspaceURLConnection.SCHEME)) {
			// workspace absolute link
			final Path path = new Path(reference.substring(WorkspaceURLConnection.SCHEME.length() + 2));
			if (isFolder) {
				if (path.segmentCount() > 1)
					return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
				else
					return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));

			} else {
				if (path.segmentCount() > 1)
					return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			}

		} else {
			// maybe this is an absolute path within the file system
			final File systemFile = new File(reference);
			if (systemFile.isAbsolute())
				return systemFile;
		}

		return null;

	}

	private static Object resolveRelativeFile(final Object location, final Object parent, final boolean exists) {
		final String reference = location.toString();

		if (parent instanceof IResource) {
			// resolve a relative path in the workspace
			final IFile relativeFile = ((IContainer) parent).getFile(new Path(reference));
			if ((relativeFile.exists()) || (!exists))
				return relativeFile;

		} else if (parent instanceof File) {
			// resolve a relative path in the file system
			final File systemFile = new File(((File) parent).getAbsolutePath() + File.separator + reference);
			if (((systemFile.exists()) && (systemFile.isFile())) || (!exists))
				return systemFile;
		}

		// giving up
		return null;
	}

	private static Object resolveRelativeFolder(final Object location, final Object parent, final boolean exists) {
		final String reference = location.toString();

		if (parent instanceof IResource) {
			// resolve a relative path in the workspace
			final IContainer relativeFolder = ((IContainer) parent).getFolder(new Path(reference));
			if ((relativeFolder.exists()) || (!exists))
				return relativeFolder;

		} else if (parent instanceof File) {
			// resolve a relative path in the file system
			final File systemFolder = new File(((File) parent).getAbsolutePath() + File.separator + reference);
			if (((systemFolder.exists()) && (systemFolder.isDirectory())) || (!exists))
				return systemFolder;
		}

		// giving up
		return null;
	}

	public static String toProjectRelativeLocation(final Object location, final Object parent) {
		// try to resolve file
		Object resource = resolveFile(location, parent, true);
		if (!(resource instanceof IResource))
			// try to resolve folder
			resource = resolveFolder(location, parent, true);

		if (resource instanceof IResource)
			return PROJECT_SCHEME + "://" + ((IResource) resource).getProjectRelativePath().toPortableString();

		// nothing to resolve, return null
		return null;
	}

	/**
	 * Resolve a given location to an absolute location URI. When <i>location</i> is not a string, we create an absolute string representation of the given
	 * location.
	 *
	 * @param location
	 *            (relative) location
	 * @param parent
	 *            parent object to resolve from
	 * @return resolved location string or <code>null</code>
	 */
	public static String toAbsoluteLocation(final Object location, final Object parent) {
		// try to resolve file
		final Object file = resolveFile(location, parent, true);
		if (file instanceof IResource)
			return WorkspaceURLConnection.SCHEME + ":/" + ((IResource) file).getFullPath().toPortableString();

		else if (file instanceof File)
			return ((File) file).toURI().toASCIIString();

		// try to resolve folder
		final Object folder = resolveFolder(location, parent, true);
		if (folder instanceof IResource)
			return WorkspaceURLConnection.SCHEME + ":/" + ((IResource) folder).getFullPath().toPortableString();

		else if (folder instanceof File)
			return ((File) folder).toURI().toASCIIString();

		// nothing to resolve, return null
		return null;
	}

	/**
	 * Verifies that a readable source (file/stream) exists at location.
	 *
	 * @param location
	 *            location to verify
	 * @return <code>true</code> when location is readable
	 */
	public static boolean exists(final Object location) {
		if (location == null)
			return false;

		if (resolveFile(location, null, true) != null)
			return true;

		// not a file, maybe an URI?
		try {

			final URI uri = (location instanceof URI) ? (URI) location : URI.create(location.toString());
			final InputStream stream = uri.toURL().openStream();
			if (stream != null) {
				stream.close();
				return true;
			}
		} catch (final Exception e) {
			// cannot open / read from stream
		}

		return false;
	}

	/**
	 * Get an existing resource (file/folder/URI).
	 *
	 * @param location
	 *            location to look up
	 * @return resource, either {@link File}, {@link IResource} or {@link URI}
	 */
	public static Object getResource(final Object location) {
		Object file = resolveFile(location, null, true);
		if (file != null)
			return file;

		// not a file, maybe a folder?
		file = resolveFolder(location, null, true);
		if (file != null)
			return file;

		// not a folder, maybe an URI?
		if (location instanceof URI)
			return location;

		try {
			if (location != null)
				return URI.create(location.toString());
		} catch (final Exception e) {
			// cannot create URI
		}

		return null;
	}

	public static InputStream getInputStream(final Object location) {
		try {
			final Object resource = getResource(location);
			if (resource instanceof IFile)
				return ((IFile) resource).getContents();

			if (resource instanceof File)
				return new FileInputStream((File) resource);

			if (resource instanceof URI)
				return ((URI) resource).toURL().openStream();
		} catch (final Exception e) {
			// cannot open stream
		}

		return null;
	}

	/**
	 * Converts an {@link IPath} representing a workspace resource to an {@link URI}.
	 *
	 * @param path
	 *            The path to convert
	 * @return The URI representing the provided path
	 */
	public static URI toURI(final IPath path) {
		// source from org.eclipse.core.filesystem.URIUtil (Indigo version)
		if (path == null)
			return null;
		if (path.isAbsolute())
			return toURI(path.toFile().getAbsolutePath());
		try {
			// try to preserve the path as a relative path
			return new URI(escapeColons(path.toString()));
		} catch (final URISyntaxException e) {
			return toURI(path.toFile().getAbsolutePath());
		}
	}

	/**
	 * Converts a String representing a local file system path to a {@link URI}. For example, this method can be used to create a URI from the output of
	 * {@link File#getAbsolutePath()}.
	 *
	 * @param pathString
	 *            The path string to convert
	 * @return The URI representing the provided path string
	 */
	private static URI toURI(String pathString) {
		// source from org.eclipse.core.filesystem.URIUtil (Indigo version)
		if (File.separatorChar != '/')
			pathString = pathString.replace(File.separatorChar, '/');
		final int length = pathString.length();
		final StringBuffer pathBuf = new StringBuffer(length + 1);
		// There must be a leading slash in a hierarchical URI
		if ((length > 0) && (pathString.charAt(0) != '/'))
			pathBuf.append('/');
		// additional double-slash for UNC paths to distinguish from host separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);
		try {
			return new URI(EFS.SCHEME_FILE, null, pathBuf.toString(), null);
		} catch (final URISyntaxException e) {
			// try java.io implementation
			return new File(pathString).toURI();
		}
	}

	/**
	 * Replaces any colon characters in the provided string with their equivalent URI escape sequence.
	 */
	private static String escapeColons(final String string) {
		// source from org.eclipse.core.filesystem.URIUtil (Indigo version)
		final String COLON_STRING = "%3A"; //$NON-NLS-1$
		if (string.indexOf(':') == -1)
			return string;
		final int length = string.length();
		final StringBuffer result = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			final char c = string.charAt(i);
			if (c == ':')
				result.append(COLON_STRING);
			else
				result.append(c);
		}
		return result.toString();
	}

	/**
	 * Convert a location to a path in the workspace.
	 *
	 * @param location
	 *            location to convert (workspace://...)
	 * @return
	 */
	public static IPath toPath(final String location) {
		if (location == null)
			return null;

		Object resource = resolveAbsolute(location, null, true);
		if (resource == null)
			resource = resolveAbsolute(location, null, false);

		return (resource instanceof IResource) ? ((IResource) resource).getFullPath() : null;
	}

	/**
	 * Convert an input stream to a string.
	 *
	 * @param stream
	 *            input string to read from
	 * @return string containing stream data
	 * @throws IOException
	 *             thrown on problems with input stream
	 */
	public static String toString(final InputStream stream) throws IOException {
		if (stream == null)
			return null;

		return toString(new InputStreamReader(stream));
	}

	/**
	 * Read characters from a {@link Reader} and return its string representation. Can be used to convert an {@link InputStream} to a string.
	 *
	 * @param reader
	 *            reader to read from
	 * @return string content of reader
	 * @throws IOException
	 *             when reader is not accessible
	 */
	public static String toString(final Reader reader) throws IOException {
		if (reader == null)
			return null;

		final StringBuffer out = new StringBuffer();

		final char[] buffer = new char[1024];
		int bytes = 0;
		do {
			bytes = reader.read(buffer);
			if (bytes > 0)
				out.append(buffer, 0, bytes);
		} while (bytes != -1);

		return out.toString();
	}
}
