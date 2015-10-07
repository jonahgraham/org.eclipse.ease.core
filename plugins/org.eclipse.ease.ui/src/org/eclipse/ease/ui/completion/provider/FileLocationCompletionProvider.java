/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.Logger;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.CompletionContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class FileLocationCompletionProvider extends AbstractCompletionProvider {

	private static final int ORDER_URI_SCHEME = ScriptCompletionProposal.ORDER_DEFAULT;
	private static final int ORDER_PROJECT = ScriptCompletionProposal.ORDER_DEFAULT + 1;
	private static final int ORDER_FOLDER = ScriptCompletionProposal.ORDER_DEFAULT + 2;
	private static final int ORDER_FILE = ScriptCompletionProposal.ORDER_DEFAULT + 3;

	/**
	 * Simple context to create proposals for exchanged filters.
	 */
	private class StringContext extends CompletionContext {

		private final String fFilter;
		private final ICompletionContext fContext;

		public StringContext(final ICompletionContext context, final String filter) {
			super(context.getScriptEngine(), context.getScriptType());

			fContext = context;
			fFilter = filter;
		}

		@Override
		public String getFilter() {
			return fFilter;
		}

		@Override
		public int getOffset() {
			return fContext.getOffset();
		}

		@Override
		protected boolean isLiteral(final char candidate) {
			return false;
		}
	}

	private final ILabelProvider fLabelProvider = new WorkbenchLabelProvider();

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (context.getType() == Type.STRING_LITERAL) && (context.getCaller().endsWith("include")) && (context.getParameterOffset() == 0);
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		final Collection<ScriptCompletionProposal> proposals = new ArrayList<ScriptCompletionProposal>();

		// add URI schemes
		if (showCandidate(context, "workspace://"))
			addProposal(proposals, context, "workspace://", "workspace://", null, ORDER_URI_SCHEME);

		if (showCandidate(context, "project://"))
			addProposal(proposals, context, "project://", "project://", null, ORDER_URI_SCHEME);

		if (showCandidate(context, "file:///"))
			addProposal(proposals, context, "file:///", "file:///", null, ORDER_URI_SCHEME);

		ICompletionContext proposalContext = context;
		String location = context.getFilter();
		location = location.replace('\\', '/');
		Object displayResource = null;

		// special handling for project:// URIs
		if ((location.startsWith("project://")) && (context.getResource() instanceof IResource)) {
			final IProject project = ((IResource) context.getResource()).getProject();
			location = location.replace("project://", "workspace://" + project.getName() + "/");

			// now let workspace:// resolver do the job

		} else if (location.startsWith("/")) {
			// absolute path into file system (unix)
			location = "file://" + location;

		} else if (location.indexOf(":/") == 1) {
			// absolute path into file system (windows)
			location = "file:///" + location;

		} else if ((!location.contains("://") && (!location.startsWith("file:///")) && (!location.startsWith("workspace://")))) {
			// must be a relative path, make absolute

			if (context.getResource() instanceof IResource)
				location = "workspace:/" + ((IResource) context.getResource()).getParent().getFullPath().toString() + "/" + location;

			else if (context.getResource() instanceof File)
				location = "workspace:/" + ((File) context.getResource()).getParentFile().getAbsolutePath() + "/" + location;
		}

		if (location.startsWith("file:///")) {
			// absolute path into file system

			// split into base & filter
			int lastSlash = location.lastIndexOf('/');
			final String base = (lastSlash > "file:///".length()) ? location.substring("file:///".length(), lastSlash) : "";
			final String filter = location.substring(lastSlash + 1);

			proposalContext = new StringContext(context, filter);

			if (base.isEmpty()) {
				// get root elements
				if (isWindows()) {
					for (final File rootFile : File.listRoots()) {
						final String name = rootFile.getPath().replace('\\', '/');
						if (showCandidate(context, rootFile))
							addProposal(proposals, proposalContext, name, name, getImage(rootFile), ORDER_FOLDER);
					}

					// done
					return proposals;

				} else
					displayResource = new File("/");

			} else {
				// some root path already chosen
				// if (location.endsWith(":/"))
				// do not remove slash
				lastSlash++;

				displayResource = ResourceTools.resolveFolder(location.substring(0, lastSlash), context.getResource(), true);
			}

		} else if (location.startsWith("workspace://")) {
			// absolute path into workspace

			// split into base & filter
			final int lastSlash = location.lastIndexOf('/');
			final String base = (lastSlash > "workspace://".length()) ? location.substring("workspace://".length(), lastSlash) : "";
			final String filter = location.substring(lastSlash + 1);

			proposalContext = new StringContext(context, filter);

			if (base.isEmpty()) {
				// get root projects
				for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (showCandidate(context, project)) {
						final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(fLabelProvider.getImage(project));
						addProposal(proposals, proposalContext, project.getName(), project.getName() + "/", imageDescriptor, ORDER_PROJECT);
					}
				}

				// done
				return proposals;

			} else {
				// some root path already chosen
				displayResource = ResourceTools.resolveFolder(location.substring(0, lastSlash), context.getResource(), true);
			}
		}

		// display proposals
		if (displayResource instanceof IContainer) {
			// display an eclipse resource container
			try {
				for (final IResource resource : ((IContainer) displayResource).members()) {
					if (showCandidate(context, resource)) {
						if (resource instanceof IFile) {
							final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(fLabelProvider.getImage(resource));
							addProposal(proposals, proposalContext, resource.getName(), resource.getName(), imageDescriptor, ORDER_FILE);

						} else {
							final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(fLabelProvider.getImage(resource));
							addProposal(proposals, proposalContext, resource.getName(), resource.getName() + "/", imageDescriptor, ORDER_FOLDER);
						}
					}
				}

				// add proposal to traverse up one level
				if (!(displayResource instanceof IProject)) {
					if (showCandidate(context, "..")) {
						addProposal(proposals, proposalContext, "..", "../",
								PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), ORDER_FOLDER);
					}
				}

			} catch (final CoreException e) {
				Logger.error(Activator.PLUGIN_ID, "Could not traverse folder \"" + ((IContainer) displayResource).getName() + "\"", e);
			}

		} else if (displayResource instanceof File) {
			// display a file system file
			final File[] dirListing = ((File) displayResource).listFiles();
			if (dirListing != null) {
				// sometimes returns null - seems to be related with insufficient rights to access folders
				for (final File file : dirListing) {
					if (showCandidate(context, file)) {
						if (file.isFile())
							addProposal(proposals, proposalContext, file.getName(), file.getName(), getImage(file), ORDER_FILE);

						else
							addProposal(proposals, proposalContext, file.getName(), file.getName() + "/", getImage(file), ORDER_FOLDER);
					}
				}
			}

			if (!isRootFile((File) displayResource)) {
				if (showCandidate(context, "..")) {
					addProposal(proposals, proposalContext, "..", "../",
							PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), ORDER_FOLDER);
				}
			}
		}

		return proposals;
	}

	/**
	 * Checks if a given file is a root file of the local file system.
	 *
	 * @param file
	 *            file to check
	 * @return <code>true</code> for root files
	 */
	private static boolean isRootFile(final File file) {
		for (final File rootFile : File.listRoots()) {
			if (rootFile.equals(file))
				return true;
		}

		return false;
	}

	private static ImageDescriptor getImage(final File file) {
		if (isRootFile(file))
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);

		if (file.isFile())
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE);

		if (file.isDirectory())
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);

		return null;
	}

	protected boolean showCandidate(final ICompletionContext context, final Object candidate) {
		// do not show closed projects
		if ((candidate instanceof IProject) && (!((IProject) candidate).isOpen()))
			return false;

		return true;
	}

	private static final boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	protected static boolean isFileSystemResource(final Object candidate) {
		return ("file:///".equals(candidate)) || (candidate instanceof File);
	}

	protected static boolean isWorkspaceResource(final Object candidate) {
		return ("workspace:///".equals(candidate)) || ("project:///".equals(candidate)) || (candidate instanceof IResource);
	}

	protected static boolean isFile(final Object candidate) {
		return ((candidate instanceof File) && (((File) candidate).isFile())) || (candidate instanceof IFile);
	}

	protected static boolean isFolder(final Object candidate) {
		return ((candidate instanceof File) && (((File) candidate).isDirectory())) || (candidate instanceof IContainer);
	}
}