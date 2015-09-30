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
package org.eclipse.ease.ui.dnd;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Helper class for file drop handler. Accepts files with certain file extensions and allows to encode their path.
 */
public abstract class AbstractFileDropHandler extends AbstractModuleDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		String extension = null;
		if (element instanceof IFile)
			extension = ((IFile) element).getFileExtension();

		else if (element instanceof File) {
			String name = ((File) element).getName();
			extension = (name.contains(".")) ? name.substring(name.lastIndexOf('.') + 1) : null;
		}

		return (extension != null) ? getAcceptedFileExtensions().contains(extension) : false;
	}

	/**
	 * Encode the file instance to an URI. As we are accepting files only, we do not need to resolve relative paths here.
	 *
	 * @param element
	 *            {@link File} or {@link IFile} instance
	 * @return
	 */
	protected String getFileURI(final Object element) {
		return ResourceTools.toAbsoluteLocation(element, null);
	}

	/**
	 * Get accepted file extensions. The file extension portion is defined as the string following the last period (".") character in the name.
	 *
	 * @return accepted file extensions
	 */
	protected abstract Collection<String> getAcceptedFileExtensions();
}
