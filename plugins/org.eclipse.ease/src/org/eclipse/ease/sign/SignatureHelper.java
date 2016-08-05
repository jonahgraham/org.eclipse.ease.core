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

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;

import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.ScriptType;

/**
 * Class containing helper methods for conversion of format and appending signature to file.
 *
 */
public class SignatureHelper {

	private static final String BEGIN_STRING = "-----BEGIN SIGNATURE-----", END_STRING = "-----END SIGNATURE-----";

	// set default message digest algorithm
	public static final String DEFAULT_MESSAGE_DIGEST_ALGO = "SHA256";

	// set default signature provider
	public static final String DEFAULT_SIGNATURE_PROVIDER = "preferred";

	/**
	 * Converts given bytes in {@link Base64} form.
	 *
	 * @param bytes
	 *            bytes to be converted to Base64
	 * @return String representation of bytes in Base64 form or <code>null</code> if input is <code>null</code>
	 */
	public static String convertBytesToBase64(final byte[] bytes) {

		if (bytes == null)
			return null;

		final Base64.Encoder b = Base64.getEncoder();
		return b.encodeToString(bytes);
	}

	/**
	 * Converts given {@link Base64} string to bytes.
	 *
	 * @param str
	 *            provide {@link Base64} string to convert
	 * @return bytes is conversion is successful and <code>null</code> if input is null
	 */
	public static byte[] convertBase64ToBytes(final String str) {

		if (str == null)
			return null;

		final Base64.Decoder decoder = Base64.getDecoder();
		return decoder.decode(str);
	}

	/**
	 * Converts given signature, messageSigestAlgorithm, provider, and certificate in proper format.<br/>
	 * Format for signature block will be as follows:
	 * <p>
	 * newline <br/>
	 * <i>blockCommentStart</i><br/>
	 * -----BEGIN SIGNATURE-----<br/>
	 * Hash:SHA1 Provider:SUN <br/>
	 * <br/>
	 * signature in {@link Base64} format (48 bytes) <br/>
	 * <br/>
	 * certificate chain in {@link Base64} format (multiple lines)(each line containing 64 bytes)<br/>
	 * <br/>
	 * -----END SIGNSTURE-----<br/>
	 * <i>blockCommentEnd</i><br/>
	 * newline
	 * </p>
	 *
	 * @param scriptType
	 *            provide {@link ScriptType} instance of stream for script
	 * @param signStr
	 *            string representation of signature in Base64 format
	 * @param certStr
	 *            string representation of certificate chain in Base64 format
	 * @param messageDigestAlgo
	 *            name the message-digest algorithm using which signature is created. Provide <code>null</code> or empty string or 'default' to set default
	 *            algorithm
	 * @param provider
	 *            name the provider used to perform signature. Provide <code>null</code> or empty string to set 'preferred'
	 * @return string representation of signature block in proper format
	 * @throws ScriptSignatureException
	 *             when one or more parameter are <code>null</code> or empty
	 */
	public static String getSignatureInFormat(final ScriptType scriptType, final String signStr, final String certStr, String messageDigestAlgo,
			String provider) throws ScriptSignatureException {

		if ((scriptType == null) || (signStr == null) || signStr.isEmpty() || (certStr == null) || certStr.isEmpty())
			throw new ScriptSignatureException("One or more parameters are null or empty");

		if ((messageDigestAlgo == null) || messageDigestAlgo.isEmpty() || "default".equalsIgnoreCase(messageDigestAlgo))
			messageDigestAlgo = DEFAULT_MESSAGE_DIGEST_ALGO;

		if ((provider == null) || provider.isEmpty())
			provider = DEFAULT_SIGNATURE_PROVIDER;

		final String begin = "\n" + BEGIN_STRING, end = END_STRING + "\n", signatureParam = "Hash:" + messageDigestAlgo + " Provider:" + provider;

		/*
		 * By default, last line in every file is ended by \n which is not visible directly. But if generated programmatically, it may not.
		 *
		 * A single \n character is added always to start signature block from new line. It may give atmost a single blank line in case \n character is already
		 * present.
		 */
		// TODO remember while appending to file
		// strBuf.append("\n");

		final StringBuffer strBuf = new StringBuffer();

		strBuf.append(begin);
		strBuf.append("\n");

		strBuf.append(signatureParam);
		strBuf.append("\n\n");

		strBuf.append(signStr);
		strBuf.append("\n\n");

		int i = 0;
		for (final String s : certStr.split("")) {
			strBuf.append(s);
			i++;
			if ((i % 48) == 0)
				strBuf.append("\n");
		}

		if ((i % 48) != 0)
			strBuf.append("\n");

		strBuf.append("\n");
		strBuf.append(end);

		final ICodeFactory iCodeFactory = scriptType.getCodeFactory();

		final StringBuffer finalBuf = new StringBuffer();

		// appending initial required new line character
		finalBuf.append("\n");

		// using createCommentedString method of ICodeFactory to make comment of signature block
		finalBuf.append(iCodeFactory.createCommentedString(strBuf.toString(), true));

		// appending final required new line character
		finalBuf.append("\n");
		return finalBuf.toString();
	}

	/**
	 * Checks the given input stream to see whether it contains signature or not.
	 *
	 * @param scriptType
	 *            provide {@link ScriptType} instance of stream for script
	 * @param inputStream
	 *            provide {@link InputStream} to check for signature
	 * @return <code>true</code> if signature is found or <code>false</code> if signature is not found
	 * @throws ScriptSignatureException
	 *             when signature format is improper
	 */
	public static boolean containSignature(final ScriptType scriptType, final InputStream inputStream) throws ScriptSignatureException {

		final ICodeParser iCodeParser = scriptType.getCodeParser();
		return iCodeParser.getSignatureInfo(inputStream) != null;
	}

	/**
	 * Checks whether provided certificate or certificate attached with is self-signed or not.
	 *
	 * @param certificate
	 *            provide certificate to check for
	 * @return <code>true</code> if certificate is self-signed or <code>false</code> if certificate is CA signed
	 * @throws ScriptSignatureException
	 *             when certificate is not provided or there is an error while retrieving certificate
	 */
	public static boolean isSelfSignedCertificate(Certificate certificate) throws ScriptSignatureException {
		if (certificate == null)
			throw new ScriptSignatureException("Provide appropriate certificate");

		try {
			certificate.verify(certificate.getPublicKey());
			return true;

		} catch (final CertificateException e) {
			Logger.error(Activator.PLUGIN_ID, "Error while parsing certificate.", e);
			throw new ScriptSignatureException("Error while parsing certificate.", e);
		} catch (final InvalidKeyException e) {
			throw new ScriptSignatureException("Key of the certificate is invalid.", e);

		} catch (final NoSuchAlgorithmException e) {
			throw new ScriptSignatureException("No aprovider support this type of algorithm.", e);

		} catch (final NoSuchProviderException e) {
			throw new ScriptSignatureException("No provider for this certificate.", e);

		} catch (final SignatureException e) {
			// private key with which certificate was signed does not correspond to this public key. Hence it is not self-signed certificate
			return false;
		}
	}
}