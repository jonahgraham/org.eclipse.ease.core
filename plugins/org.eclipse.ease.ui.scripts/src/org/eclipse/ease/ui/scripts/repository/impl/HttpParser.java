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
package org.eclipse.ease.ui.scripts.repository.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.StringTools;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScriptLocation;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.PlatformUI;

public class HttpParser extends InputStreamParser {

	public void parse(final URL location, final IScriptLocation entry) {
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		ScriptType scriptType = scriptService.getScriptType(location.toString());
		if (scriptType != null) {
			// register script
			final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(IRepositoryService.class);
			repositoryService.updateLocation(entry, location.toExternalForm(), System.currentTimeMillis());

		} else {
			try {
				InputStream stream = location.openStream();
				if (stream != null) {
					String content = StringTools.toString(stream);

					try {
						stream.close();
					} catch (IOException e) {
					}

					Collection<URL> anchors = extractAnchors(location, content);
					// make sure we do not recursive load the same page over and over
					anchors.remove(location);

					for (URL subLocation : anchors)
						parse(subLocation, entry);
				}

			} catch (IOException e) {
				// cannot read content, ignore location
			}
		}
	}

	private Collection<URL> extractAnchors(final URL base, final String content) {
		Collection<URL> anchorNodes = new HashSet<URL>();

		URI baseURI = URI.createURI(base.toString());
		if (baseURI.fileExtension() != null) {
			// base refers to a file, not a folder
			baseURI = baseURI.trimSegments(1);
		}

		if (baseURI.hasTrailingPathSeparator())
			baseURI = baseURI.trimSegments(1);

		int pos = content.indexOf(" href=");
		do {
			if (pos != -1) {
				int endpos = content.indexOf(content.charAt(pos + 6), pos + 7);
				try {
					if (content.charAt(pos + 7) != '#') {
						URI candidate = URI.createURI(content.substring(pos + 7, endpos));
						if (candidate.isRelative())
							candidate = candidate.resolve(baseURI);

						if (candidate.hasTrailingPathSeparator())
							candidate = candidate.trimSegments(1);

						if (candidate.toString().startsWith(baseURI.toString())) {
							// candidate is stored below the base

							if (candidate.segmentCount() > baseURI.segmentCount())
								anchorNodes.add(new URL(candidate.toString()));
						}
					}

				} catch (MalformedURLException e) {
					// ignore invalid URLs
				}
			}

			pos = content.indexOf(" href=", pos + 1);

		} while (pos != -1);

		return anchorNodes;
	}
}
