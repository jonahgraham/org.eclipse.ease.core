/*******************************************************************************
 * Copyright (c) 2016 Varun Raval and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Varun Raval - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.sign;

import org.eclipse.ease.ui.Activator;

public interface ISignatureHelperConstants {

	// name of file in state location used to store recently accesses keystores
	public static final String KEYSTORE_SETTING_FILE = Activator.getDefault().getStateLocation().toString() + "EASE_KeyStoreFiles";

	// node used to store keystore and alias pass
	public static final String KEYTORE_ALIAS_NODE = "KeystoreAliasPass";

	// name of folder in state location to store signatures
	public static final String PATH_TO_SIGNATURES = Activator.getDefault().getStateLocation().toString() + "/signatures";
}
