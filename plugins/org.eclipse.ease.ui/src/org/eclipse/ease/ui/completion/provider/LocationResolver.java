/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion.provider;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Resolves file location completion proposals. Helper for file location completion provider.
 */
public class LocationResolver {

	public enum Type {
		FILE, WORKSPACE, PROJECT, HTTP, GENERIC_URI, UNKNOWN
	}

	/**
	 * Detect windows operating system.
	 *
	 * @return <code>true</code> when executed on a windows based OS
	 */
	private static final boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	private final String fLocation;
	private boolean fAbsolute = false;
	private Type fType = Type.UNKNOWN;
	private final Object fParent;
	private Object fResolvedFolder;
	private boolean fProcessed = false;

	public LocationResolver(final String location, final Object parent) {
		fLocation = location;
		fParent = parent;
	}

	/**
	 * Returns true for absolute locations.
	 *
	 * @return <code>true</code> for absolute location
	 */
	public boolean isAbsolute() {
		process();
		return fAbsolute;
	}

	/**
	 * Get type of location.
	 *
	 * @return location type
	 */
	public Type getType() {
		process();
		return fType;
	}

	/**
	 * Detect part of location that denotes the filter component. This is the text string following the last delimiter (eg '/').
	 *
	 * @return filter part of location
	 */
	public String getFilterPart() {
		// try to locate operating system delimiter
		int delimiter = fLocation.lastIndexOf(File.separatorChar);
		if (delimiter != -1)
			return fLocation.substring(delimiter + 1);

		// not found, on windows we should try to look for slashes, too
		delimiter = fLocation.lastIndexOf('/');
		if (delimiter != -1)
			return fLocation.substring(delimiter + 1);

		// check if we see just the scheme of an URI
		if (fLocation.endsWith("//"))
			return "";

		// no delimiter found, the whole content acts as filter
		return fLocation;
	}

	/**
	 * Get base folder this location refers to.
	 *
	 * @return base folder of this location
	 */
	public Object getResolvedFolder() {
		process();
		return fResolvedFolder;
	}

	/**
	 * Get all children of this location. Returns the content of {@link #getResolvedFolder()}.
	 *
	 * @return content of location base folder
	 */
	public Collection<? extends Object> getChildren() {
		process();

		try {
			if (fResolvedFolder instanceof IContainer)
				return Arrays.asList(((IContainer) fResolvedFolder).members());
		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		if (fResolvedFolder instanceof File)
			return Arrays.asList(((File) fResolvedFolder).listFiles());

		if (fResolvedFolder == null) {
			if (fType == Type.WORKSPACE)
				return Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());

			if (fType == Type.FILE) {
				if (isWindows())
					return Arrays.asList(File.listRoots());

				// on unix we directly return the content of the single root node
				return Arrays.asList(new File("/").listFiles());
			}
		}

		return Collections.emptySet();
	}

	/**
	 * Get a string representation of the resolved location folder.
	 *
	 * @return location base folder representation
	 */
	public String getParentString() {
		process();

		// extract name of last given folder and truncate filter part after the last delimiter
		final String resolvedFolderName = fLocation.substring(0, fLocation.length() - getFilterPart().length());

		if (isAbsolute()) {
			switch (fType) {
			case FILE:
				if (getResolvedFolder() instanceof File)
					// URI uses a 'file:/' scheme, but we should use 'file:///' instead
					return "file://" + ((File) getResolvedFolder()).toURI().toString().substring(5);

				// no parent given, must be a root file
				return "file:///";

			case WORKSPACE:
				if (getResolvedFolder() instanceof IContainer) {
					final String suffix = ((IContainer) getResolvedFolder()).getFullPath().toString().equals("/") ? "" : "/";
					return "workspace:/" + ((IContainer) getResolvedFolder()).getFullPath() + suffix;
				}

				// no parent given
				return "workspace://";

			case PROJECT:
				if (getResolvedFolder() instanceof IContainer) {
					final String suffix = ((IContainer) getResolvedFolder()).getProjectRelativePath().isEmpty() ? "" : "/";
					return "project://" + ((IContainer) getResolvedFolder()).getProjectRelativePath() + suffix;
				}

				// no parent given
				return "project://";
			}
		}

		return resolvedFolderName;
	}

	/**
	 * Resolve the given location.
	 */
	private void process() {
		if (!fProcessed) {
			fProcessed = true;

			if (fLocation.contains("://")) {
				// the location contains a scheme, extract
				final String scheme = fLocation.substring(0, fLocation.indexOf("://"));
				if ("workspace".equals(scheme)) {
					fType = Type.WORKSPACE;
					fAbsolute = true;

				} else if ("project".equals(scheme)) {
					fType = Type.PROJECT;
					fAbsolute = true;

				} else if ("file".equals(scheme)) {
					fType = Type.FILE;
					fAbsolute = true;

				} else if (scheme.startsWith("http")) {
					fType = Type.HTTP;
					fAbsolute = true;

				} else {
					fType = Type.GENERIC_URI;
					fAbsolute = true;
				}

			} else {
				// no scheme detected, look for absolute patterns
				if (fLocation.startsWith("/")) {
					fType = Type.FILE;
					fAbsolute = true;

				} else if ((fLocation.indexOf(":/") == 1) || (fLocation.indexOf(":\\") == 1)) {
					fType = Type.FILE;
					fAbsolute = true;

				} else {
					// this is a relative path
					fAbsolute = false;
					if (fParent instanceof IResource)
						fType = Type.WORKSPACE;

					else if (fParent instanceof File)
						fType = Type.FILE;
				}
			}

			// extract name of last given folder and truncate filter part after the last delimiter
			String resolvedFolderName = fLocation.substring(0, fLocation.length() - getFilterPart().length());
			if (resolvedFolderName.startsWith("file://")) {
				// remove scheme
				resolvedFolderName = resolvedFolderName.substring(7);

				if ((resolvedFolderName.startsWith("/")) && (isWindows()))
					// remove additional slash on windows
					resolvedFolderName = resolvedFolderName.substring(1);
			}

			switch (fType) {
			case UNKNOWN:
				// seems we have a relative path here
				if (fParent == null) {
					fResolvedFolder = null;
					return;
				}

				if (fParent instanceof IResource) {
					final IContainer parentFolder = ((IResource) fParent).getParent();
					if (resolvedFolderName.trim().isEmpty())
						fResolvedFolder = parentFolder;
					else
						fResolvedFolder = ResourceTools.resolveFolder(resolvedFolderName, parentFolder, true);

				} else if (fParent instanceof File) {
					final File parentFolder = ((File) fParent).getParentFile();
					if (resolvedFolderName.trim().isEmpty())
						fResolvedFolder = parentFolder;
					else
						fResolvedFolder = ResourceTools.resolveFolder(resolvedFolderName, parentFolder, true);
				}
				break;

			case FILE:
				fResolvedFolder = ResourceTools.resolveFolder(resolvedFolderName, fAbsolute ? null : fParent, true);
				break;

			case WORKSPACE:
				fResolvedFolder = ResourceTools.resolveFolder(resolvedFolderName, fAbsolute ? null : fParent, true);
				break;

			case PROJECT:
				if (fParent instanceof IResource)
					try {
						fResolvedFolder = ResourceTools.resolveFolder(resolvedFolderName, fParent, true);
					} catch (final IllegalArgumentException e) {
						// seems we hit the root folder again by traversing up the chain
						fResolvedFolder = ((IResource) fParent).getProject();
					}
				break;

			default:
				break;
			}

			fResolvedFolder = ResourceTools.resolveFolder(fResolvedFolder, null, true);
		}
	}
}
