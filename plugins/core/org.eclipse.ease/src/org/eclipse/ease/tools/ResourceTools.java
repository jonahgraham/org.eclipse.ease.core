/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.urlhandler.WorkspaceURLConnection;
import org.eclipse.ui.PlatformUI;

public final class ResourceTools {

	private static final String PROJECT_SCHEME = "project";

	/**
	 * @deprecated
	 */
	@Deprecated
	private ResourceTools() {
	}

	public static File getFile(final String uri) {
		try {
			// TODO find better way to encode URI correctly
			return new File(new URI(uri.replace(" ", "%20")));
		} catch (Exception e) {
		}

		return null;
	}

	public static IResource getResource(final String uri) {
		if (uri.startsWith(WorkspaceURLConnection.SCHEME + "://")) {
			Path path = new Path(uri.substring(WorkspaceURLConnection.SCHEME.length() + 3));
			if (!path.isEmpty()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
				return project.findMember(path.removeFirstSegments(1));
			}
		}

		return null;
	}

	public static InputStream getInputStream(final String uri) {
		try {
			return new URL(uri).openStream();
		} catch (Exception e) {
		}

		return null;
	}

	public static Object getContent(final String uri) {
		IResource resource = getResource(uri);
		if (resource != null)
			return resource;

		File file = getFile(uri);
		if (file != null)
			return file;

		InputStream inputStream = getInputStream(uri);
		if (inputStream != null)
			return inputStream;

		return null;
	}

	public static boolean exists(final String uri) {
		IResource resource = getResource(uri);
		if (resource != null)
			return resource.exists();

		File file = getFile(uri);
		if (file != null)
			return file.exists();

		InputStream inputStream = getInputStream(uri);
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			return true;
		}

		return false;
	}

	public static String toLocation(final IResource resource) {
		return WorkspaceURLConnection.SCHEME + ":/" + resource.getFullPath().toPortableString();
	}

	public static ScriptType getScriptType(final IFile file) {
		// resolve by content type
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		try {
			IContentDescription contentDescription = file.getContentDescription();
			if (contentDescription != null)
				return scriptService.getScriptType(contentDescription.getContentType());

		} catch (CoreException e) {
		}

		// did not work, resolve by extension
		return scriptService.getScriptType(file.getFileExtension());
	}

	public static ScriptType getScriptType(final File file) {
		// resolve by extension
		String name = file.getName();
		if (name.contains(".")) {
			String extension = name.substring(name.lastIndexOf('.') + 1);

			final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
			return scriptService.getScriptType(extension);
		}

		return null;
	}

	public static Object resolveFile(Object location, final Object parent) {
		if (location instanceof IFile)
			return location;

		if (location instanceof URI) {
			// resolve file:// URIs
			try {
				location = new File((URI) location);
			} catch (Exception e) {
			}
		}

		if (location instanceof File) {
			// try to map file to the current workspace
			File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
			if (((File) location).getAbsolutePath().startsWith(workspaceRoot.getAbsolutePath()))
				location = WorkspaceURLConnection.SCHEME + "/" + ((File) location).getAbsolutePath().substring(workspaceRoot.getAbsolutePath().length());

			else if (((File) location).isFile())
				// this is a system file
				return location;

			else
				// this is a system directory
				return null;
		}

		// nothing of the previous, try to resolve
		String reference = location.toString();

		if (reference.startsWith(PROJECT_SCHEME)) {
			// project relative link
			if (parent instanceof IResource)
				return ((IResource) parent).getProject().getFile(new Path(reference.substring(PROJECT_SCHEME.length() + 3)));

		} else if (reference.startsWith(WorkspaceURLConnection.SCHEME)) {
			// workspace absolute link
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(reference.substring(WorkspaceURLConnection.SCHEME.length() + 3)));

		} else {
			// maybe this is an absolute path within the workspace
			try {
				IFile workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(reference));
				if (workspaceFile.exists())
					return workspaceFile;
			} catch (IllegalArgumentException e) {
				// invalid path detected
			}

			// maybe this is an absolute path within the file system
			File systemFile = new File(reference);
			if (systemFile.isFile())
				return systemFile;

			// maybe a relative filename
			if (parent instanceof IContainer) {
				IFile workspaceFile = ((IContainer) parent).getFile(new Path(reference));
				if (workspaceFile.exists())
					return workspaceFile;

			} else if (parent instanceof IFile) {
				IFile workspaceFile = ((IResource) parent).getParent().getFile(new Path(reference));
				if (workspaceFile.exists())
					return workspaceFile;

			} else if (parent instanceof File) {
				if (((File) parent).isDirectory()) {
					systemFile = new File(((File) parent).getParentFile().getAbsolutePath() + File.pathSeparator + reference);
					if (systemFile.isFile())
						return systemFile;

				} else {
					systemFile = new File(((File) parent).getParentFile().getAbsolutePath() + File.pathSeparator + reference);
					if (systemFile.isFile())
						return systemFile;
				}
			}
		}

		// giving up
		return null;
	}

	public static Object resolveFolder(Object location, final Object parent) {
		if (location instanceof IContainer)
			return location;

		if (location instanceof URI) {
			// resolve file:// URIs
			try {
				location = new File((URI) location);
			} catch (Exception e) {
			}
		}

		if (location instanceof File) {
			// try to map file to the current workspace
			File workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
			if (((File) location).getAbsolutePath().startsWith(workspaceRoot.getAbsolutePath()))
				location = WorkspaceURLConnection.SCHEME + "/" + ((File) location).getAbsolutePath().substring(workspaceRoot.getAbsolutePath().length());

			else if (((File) location).isDirectory())
				// this is a system directory
				return location;

			else
				// this is a system file
				return null;
		}

		// nothing of the previous, try to resolve
		String reference = location.toString();

		if (reference.startsWith(PROJECT_SCHEME)) {
			// project relative link
			if (parent instanceof IResource)
				return ((IResource) parent).getProject().getFolder(new Path(reference.substring(PROJECT_SCHEME.length() + 3)));

		} else if (reference.startsWith(WorkspaceURLConnection.SCHEME)) {
			// workspace absolute link
			IPath path = new Path(reference.substring(WorkspaceURLConnection.SCHEME.length() + 3));
			if (path.segmentCount() > 1)
				return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
			else
				return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));

		} else {
			// maybe this is an absolute path within the workspace
			try {
				IPath path = new Path(reference);
				IContainer workspaceFile;
				if (path.segmentCount() > 1)
					workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
				else
					workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));

				if (workspaceFile.exists())
					return workspaceFile;

			} catch (IllegalArgumentException e) {
				// invalid path detected
			}

			// maybe this is an absolute path within the file system
			File systemFile = new File(reference);
			if (systemFile.isFile())
				return systemFile;

			// maybe a relative filename
			if (parent instanceof IContainer) {
				IContainer workspaceFile = ((IContainer) parent).getFolder(new Path(reference));
				if (workspaceFile.exists())
					return workspaceFile;

			} else if (parent instanceof IFile) {
				IContainer workspaceFile = ((IResource) parent).getParent().getFolder(new Path(reference));
				if (workspaceFile.exists())
					return workspaceFile;

			} else if (parent instanceof File) {
				if (((File) parent).isDirectory()) {
					systemFile = new File(((File) parent).getParentFile().getAbsolutePath() + File.pathSeparator + reference);
					if (systemFile.isFile())
						return systemFile;

				} else {
					systemFile = new File(((File) parent).getParentFile().getAbsolutePath() + File.pathSeparator + reference);
					if (systemFile.isFile())
						return systemFile;
				}
			}
		}

		// giving up
		return null;
	}
}
