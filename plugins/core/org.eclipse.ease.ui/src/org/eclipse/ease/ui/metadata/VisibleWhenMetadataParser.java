/**
 *   Copyright (c) 2013 Atos
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *  
 *   Contributors:
 *       Arthur Daussy - initial implementation
 */
package org.eclipse.ease.ui.metadata;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ease.storedscript.metada.AbstractRegexMetadataParser;


/**
 * Used to parse Enable When metadata
 * 
 * @author adaussy
 * 
 */
public class VisibleWhenMetadataParser extends AbstractRegexMetadataParser {


	private static final Pattern ENABLE_WHEN_PATTERN = Pattern.compile("VisibleWhen:\\s*(\\[[^\\]]*\\])", Pattern.DOTALL);

	@Override
	protected Pattern createPattern() {
		return ENABLE_WHEN_PATTERN;
	}

	@Override
	public List<String> parserMetadata(String header) {
		String newHeader = header.replace("\n", "");
		return super.parserMetadata(newHeader);
	}


}
