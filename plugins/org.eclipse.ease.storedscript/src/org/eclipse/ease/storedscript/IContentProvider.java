/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.storedscript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;


public interface IContentProvider {

	public URI createURI();

	public String getContent() throws CoreException, IOException;

	public InputStream getInputStream() throws IOException;

	/**
	 * Try to get the file related to this store script. Can return null if the stored script is not a file
	 * @return
	 */
	public File getFile();


}
