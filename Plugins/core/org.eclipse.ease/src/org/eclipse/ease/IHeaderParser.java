/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease;

import java.io.InputStream;
import java.util.Map;

public interface IHeaderParser {

	Map<String, String> parser(InputStream stream);

	String createHeader(Map<String, String> headerContent);
}
