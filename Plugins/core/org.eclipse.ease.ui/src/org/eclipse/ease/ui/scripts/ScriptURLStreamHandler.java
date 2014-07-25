/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.scripts;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ui.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class ScriptURLStreamHandler extends AbstractURLStreamHandlerService {

	public ScriptURLStreamHandler() {
	}

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
		if (repositoryService != null) {
			IScript script = repositoryService.getScript(new Path(url.getHost() + url.getFile()).makeAbsolute().toString());

			if (script != null)
				return new ScriptURLConnection(url, script);

			throw new IOException("\"" + url + "\" not found");
		}

		return null;
	}
}
