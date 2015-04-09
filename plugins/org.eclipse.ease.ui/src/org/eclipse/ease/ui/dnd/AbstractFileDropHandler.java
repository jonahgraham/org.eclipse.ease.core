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
import org.eclipse.ease.modules.AbstractEnvironment;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Helper class for file drop handler. Accepts files with certain file extensions and allows to encode their path.
 */
public abstract class AbstractFileDropHandler implements IShellDropHandler {

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
	 * Load a dedicated module if it is not already loaded.
	 *
	 * @param scriptEngine
	 *            script engine to load module
	 * @param moduleID
	 *            moduleID to look for
	 * @param force
	 *            if set to <code>false</code> load only when not already loaded
	 * @return module instance or <code>null</code>
	 */
	protected Object loadModule(final IScriptEngine scriptEngine, final String moduleID, final boolean force) {
		IEnvironment environment = AbstractEnvironment.getEnvironment(scriptEngine);
		if (environment != null) {
			if ((force) || (environment.getModule(moduleID) == null))
				return environment.loadModule(moduleID);

			return environment.getModule(moduleID);
		}

		return null;
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
