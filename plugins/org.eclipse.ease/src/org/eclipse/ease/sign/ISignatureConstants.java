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

package org.eclipse.ease.sign;

public interface ISignatureConstants {

	// set beginning of signature block preceded by block comments
	public static final String BEGIN_STRING = "-----BEGIN SIGNATURE-----";

	// set ending of signature block followed by block comments
	public static final String END_STRING = "-----END SIGNATURE-----";

	// parameters format for signature block
	public static final String HASH_PARAM = "Hash";

	// parameters format for signature block
	public static final String PROVIDER_PARAM = "Provider";

	// set default message digest algorithm
	public static final String DEFAULT_MESSAGE_DIGEST_ALGO = "SHA256";

	// set default signature provider
	public static final String DEFAULT_SIGNATURE_PROVIDER = "preferred";
}
