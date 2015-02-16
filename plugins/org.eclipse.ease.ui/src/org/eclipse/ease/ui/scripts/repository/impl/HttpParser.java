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
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ease.ui.repository.IScriptLocation;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public class HttpParser extends InputStreamParser {

	public void parse(final IScriptLocation location) {
		InputStream stream = location.getInputStream();
		if (stream != null) {

			try {
				IMemento memento = XMLMemento.createReadRoot(new InputStreamReader(stream));

				List<IMemento> anchorNodes = new ArrayList<IMemento>();
				extractAnchors(memento, anchorNodes);
				Map<URI, String> links = convertAnchors(anchorNodes);
				links = filterExternals(URI.create(location.getLocation()), links);
				System.out.println(links);

			} catch (WorkbenchException e1) {
				// TODO handle this exception (but for now, at least know it happened)
				throw new RuntimeException(e1);

			}

			try {
				stream.close();
			} catch (IOException e) {

			}
		}
	}

	private Map<URI, String> filterExternals(final URI base, final Map<URI, String> links) {
		Map<URI, String> filtered = new HashMap<URI, String>();

		for (Entry<URI, String> entry : links.entrySet()) {
			URI link = entry.getKey();
			if (!link.isAbsolute())
				link = base.resolve(link);

			if (link.toString().startsWith(base.toString()))
				// this is a link bekow the base link
				filtered.put(link, entry.getValue());
		}

		return filtered;
	}

	private Map<URI, String> convertAnchors(final List<IMemento> anchorNodes) {
		Map<URI, String> links = new HashMap<URI, String>();

		for (IMemento node : anchorNodes)
			links.put(URI.create(node.getString("href")), extractText(node));

		return links;
	}

	private String extractText(final IMemento node) {
		StringBuilder text = new StringBuilder();
		text.append(node.getTextData());

		for (IMemento child : node.getChildren())
			text.append(extractText(child));

		return text.toString();
	}

	private void extractAnchors(final IMemento memento, final List<IMemento> anchorNodes) {
		for (IMemento child : memento.getChildren()) {
			if (child.getType().equals("a"))
				anchorNodes.add(child);

			else
				extractAnchors(child, anchorNodes);
		}
	}
}
