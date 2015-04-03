/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.io.InputStream;

/**
 * Generic interface for a scriptable object. Allows to provide adapters for any kind of object.
 */
public interface IScriptable {

	/**
	 * Get input stream containing source code.
	 * 
	 * @return source code as {@link InputStream}
	 * @throws Exception
	 *             when inputStream cannot be created
	 */
	InputStream getSourceCode() throws Exception;
}
