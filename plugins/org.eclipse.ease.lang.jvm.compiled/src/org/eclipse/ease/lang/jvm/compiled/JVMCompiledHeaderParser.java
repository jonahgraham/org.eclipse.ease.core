/*******************************************************************************
 * Copyright (c) 2014 Nicolas Rouquette, JPL, Caltech and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Rouquette, JPL, Caltech - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.jvm.compiled;

import org.eclipse.ease.AbstractCodeParser;

public class JVMCompiledHeaderParser extends AbstractCodeParser {

	private static final String LINE_COMMENT = "//";
	private static final String BLOCK_COMMENT_START = "/*";
	private static final String BLOCK_COMMENT_END = "*/";

	@Override
	protected String getLineCommentToken() {
		return LINE_COMMENT;
	}

	@Override
	protected String getBlockCommentEndToken() {
		return BLOCK_COMMENT_END;
	}

	@Override
	protected String getBlockCommentStartToken() {
		return BLOCK_COMMENT_START;
	}

	@Override
	protected boolean hasBlockComment() {
		return true;
	}
}
