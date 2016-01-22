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
import java.nio.file.Path;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class AbstractFileLocationCompletionProvider extends AbstractCompletionProvider {

	private static final int ORDER_URI_SCHEME = ScriptCompletionProposal.ORDER_DEFAULT;
	private static final int ORDER_PROJECT = ScriptCompletionProposal.ORDER_DEFAULT + 1;
	private static final int ORDER_FOLDER = ScriptCompletionProposal.ORDER_DEFAULT + 2;
	private static final int ORDER_FILE = ScriptCompletionProposal.ORDER_DEFAULT + 3;

	private final ILabelProvider fLabelProvider = new WorkbenchLabelProvider();

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (context.getType() == Type.STRING_LITERAL);
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		final LocationResolver resolver = new LocationResolver(context.getFilter(), context.getResource());

		if ((resolver.getResolvedFolder() == null) || (!resolver.isAbsolute())
				|| (resolver.getType() == org.eclipse.ease.ui.completion.provider.LocationResolver.Type.UNKNOWN)) {
			// add URI scheme proposals
			if ((matches(context.getFilter(), "workspace:/")) && (showCandidate("workspace://")))
				addProposal("workspace://", "workspace://", null, ORDER_URI_SCHEME, null);

			if ((matches(context.getFilter(), "project:/")) && (getContext().getResource() instanceof IResource) && (showCandidate("project://")))
				addProposal("project://", "project://", null, ORDER_URI_SCHEME, null);

			if ((matches(context.getFilter(), "file://")) && (showCandidate("file:///")))
				addProposal("file:///", "file:///", null, ORDER_URI_SCHEME, null);
		}

		// display proposals
		for (final Object child : resolver.getChildren()) {
			if (child instanceof File) {
				String name = ((File) child).getName();
				String suffix = "";
				if (name.isEmpty())
					name = ((File) child).toString().replace('\\', '/');
				else if (((File) child).isDirectory())
					suffix = "/";

				if ((matchesIgnoreCase(resolver.getFilterPart(), name)) && (showCandidate(child)))
					addProposal(name, resolver.getParentString() + name + suffix, getImage((File) child), ORDER_FILE, null);
			}

			if (child instanceof IResource) {
				if ((matchesIgnoreCase(resolver.getFilterPart(), ((IResource) child).getName())) && (showCandidate(child))) {
					final ImageDescriptor imageDescriptor = ImageDescriptor.createFromImage(fLabelProvider.getImage(child));
					if (child instanceof IProject) {
						addProposal(((IProject) child).getName(), resolver.getParentString() + ((IProject) child).getName() + '/', imageDescriptor,
								ORDER_PROJECT, null);
					} else if (child instanceof IContainer) {
						addProposal(((IContainer) child).getName(), resolver.getParentString() + ((IContainer) child).getName() + '/', imageDescriptor,
								ORDER_FOLDER, null);
					} else {
						addProposal(((IResource) child).getName(), resolver.getParentString() + ((IResource) child).getName(), imageDescriptor, ORDER_FILE,
								null);
					}
				}
			}
		}

		// add '..' proposal if we are not located in a root folder
		if ((matches(resolver.getFilterPart(), "..")) && (showCandidate(".."))) {
			final Object parentFolder = resolver.getResolvedFolder();

			if ((parentFolder instanceof IResource) && !(parentFolder instanceof IProject) && !(parentFolder instanceof IWorkspaceRoot)) {
				addProposal("..", resolver.getParentString() + "../",
						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), ORDER_FOLDER, null);

			} else if ((parentFolder instanceof File) && !(isRootFile((File) parentFolder))) {
				addProposal("..", resolver.getParentString() + "../",
						PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER), ORDER_FOLDER, null);
			}
		}
	}

	/**
	 * Checks if a given file is a root file of the local file system.
	 *
	 * @param file
	 *            file to check
	 * @return <code>true</code> for root files
	 */
	private static boolean isRootFile(final File file) {
		final Path filePath = file.toPath().normalize();
		for (final File rootFile : File.listRoots()) {
			if (rootFile.toPath().equals(filePath))
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

	protected boolean showCandidate(final Object candidate) {
		// do not show closed projects
		if ((candidate instanceof IProject) && (!((IProject) candidate).isOpen()))
			return false;

		return true;
	}

	protected static boolean hasFileExtension(final Object candidate, final String extension) {
		if (candidate instanceof File)
			return ((File) candidate).getName().toLowerCase().endsWith("." + extension.toLowerCase());

		else if (candidate instanceof IFile)
			return ((IFile) candidate).getFileExtension().equalsIgnoreCase(extension);

		return false;
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