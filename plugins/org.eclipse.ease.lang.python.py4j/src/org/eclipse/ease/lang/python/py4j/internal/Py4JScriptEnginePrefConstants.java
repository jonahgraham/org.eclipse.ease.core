/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j.internal;

public interface Py4JScriptEnginePrefConstants {
	String PREFIX = Activator.PLUGIN_ID + "."; //$NON-NLS-1$

	String INTERPRETER = PREFIX + "INTERPRETER";
	String INTERPRETER_CHOICE_PATH = "path"; //$NON-NLS-1$
	String INTERPRETER_CHOICE_CUSTOM = "custom"; //$NON-NLS-1$
	String INTERPRETER_CHOICE_PYDEV_DEFAULT = "pydev"; //$NON-NLS-1$
	String INTERPRETER_CHOICE_PYDEV_PREFIX = "pydev:"; //$NON-NLS-1$
	String DEFAULT_INTERPRETER = INTERPRETER_CHOICE_PATH;

	String INTERPRETER_CUSTOM = "INTERPRETER_CUSTOM"; //$NON-NLS-1$
	String DEFAULT_INTERPRETER_CUSTOM = "python"; //$NON-NLS-1$
}
