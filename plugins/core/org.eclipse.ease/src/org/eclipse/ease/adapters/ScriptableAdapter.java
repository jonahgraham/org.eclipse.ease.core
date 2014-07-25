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
package org.eclipse.ease.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ease.IScriptable;

/**
 * Adapts files to {@link IScriptable}s. Works for eclipse workspace files and java File objects.
 */
public class ScriptableAdapter implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public final Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType.equals(IScriptable.class)) {
			if (adaptableObject instanceof IFile) {
				return new IScriptable() {

					@Override
					public InputStream getSourceCode() throws Exception {
						return ((IFile) adaptableObject).getContents();
					}
				};
			}

			if (adaptableObject instanceof File) {
				return new IScriptable() {

					@Override
					public InputStream getSourceCode() throws Exception {
						return new FileInputStream((File) adaptableObject);
					}
				};
			}

			if (adaptableObject instanceof URL) {
				return new IScriptable() {

					@Override
					public InputStream getSourceCode() throws Exception {
						return ((URL) adaptableObject).openStream();
					}
				};
			}

			if (adaptableObject instanceof URI) {
				return new IScriptable() {

					@Override
					public InputStream getSourceCode() throws Exception {
						return ((URI) adaptableObject).toURL().openStream();
					}
				};
			}
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public final Class[] getAdapterList() {
		return new Class[] { IScriptable.class };
	}
}
