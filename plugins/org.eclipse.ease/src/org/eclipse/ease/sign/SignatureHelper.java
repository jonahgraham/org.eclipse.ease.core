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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
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

		Base64.Encoder b = Base64.getEncoder();
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

		Base64.Decoder decoder = Base64.getDecoder();
		return decoder.decode(str);
	}

	/**
	 * Appends given signature, messageSigestAlgorithm, provider, and certificate to given file.<br/>
	 * Format for appending signature will be as follows:
	 * <p>
	 * <i>blockCommentStart</i><br/>
	 * -----BEGIN SIGNATURE-----<br/>
	 * Hash:SHA1 Provider:SUN <br/>
	 * <br/>
	 * signature in {@link Base64} format (48 bytes) <br/>
	 * <br/>
	 * certificate chain in {@link Base64} format (multiple lines)(each line containing 64 bytes)<br/>
	 * <br/>
	 * -----END SIGNSTURE-----<br/>
	 * <i>blockCommentEnd</i>
	 * </p>
	 *
	 * @param scriptType
	 *            provide {@link ScriptType} instance of stream for script
	 * @param signStr
	 *            string representation of signature in Base64 format
	 * @param certStr
	 *            string representation of certificate chain in Base64 format
	 * @param messageDigestAlgo
	 *            name the message-digest algorithm using which signature is created. Provide <code>null</code> or empty string or 'default' to set 'SHA1'
	 * @param provider
	 *            name the provider used to perform signature. Provide <code>null</code> or empty string to set 'preferred'
	 * @param dataStream
	 *            stream to which signature and certificate are to be attached
	 * @return <code>true</code> if signature is written to dataStream and <code>false</code> if signature can't be written for e.g. due to IOException
	 * @throws ScriptSignatureException
	 *             when one or more parameter are <code>null</code> or empty
	 */
	public static boolean appendSignature(final ScriptType scriptType, final String signStr, final String certStr, String messageDigestAlgo, String provider,
			final OutputStream dataStream) throws ScriptSignatureException {

		if (scriptType == null || signStr == null || signStr.isEmpty() || certStr == null || certStr.isEmpty() || dataStream == null)
			throw new ScriptSignatureException("One or more parameters are null or empty");

		if (messageDigestAlgo == null || messageDigestAlgo.isEmpty() || "default".equalsIgnoreCase(messageDigestAlgo))
			messageDigestAlgo = "SHA1";

		if (provider == null || provider.isEmpty())
			provider = "preferred";

		final String begin = "\n" + BEGIN_STRING, end = END_STRING + "\n", signatureParam = "Hash:" + messageDigestAlgo + " Provider:" + provider;

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataStream);
		try {
			/*
			 * By default, last line in every file is ended by \n which is not visible directly. But if generated programmatically, it may not.
			 *
			 * A single \n character is added always to start signature block from new line. It may give atmost a single blank line in case \n character is
			 * already present.
			 */
			// TODO remember while appending to file
			// bufferedOutputStream.write("\n".getBytes());

			// using createCommentedString method of ICodeFactory to make comment of signature block

			StringBuffer strBuf = new StringBuffer();

			strBuf.append(begin);
			strBuf.append("\n");

			strBuf.append(signatureParam);
			strBuf.append("\n\n");

			strBuf.append(signStr);
			strBuf.append("\n\n");

			int i = 0;
			for (String s : certStr.split("")) {
				strBuf.append(s);
				i++;
				if (i % 48 == 0)
					strBuf.append("\n");
			}

			if (i % 48 != 0)
				strBuf.append("\n");

			strBuf.append("\n");
			strBuf.append(end);

			ICodeFactory iCodeFactory = scriptType.getCodeFactory();
			bufferedOutputStream.write(iCodeFactory.createCommentedString(strBuf.toString(), true).getBytes());
			bufferedOutputStream.write("\n".getBytes());
			return true;

		} catch (IOException e) {
			Logger.error(Activator.PLUGIN_ID, Arrays.toString(e.getStackTrace()), e);

		} finally {
			try {
				if (bufferedOutputStream != null)
					bufferedOutputStream.close();
			} catch (IOException e) {
				Logger.error(Activator.PLUGIN_ID, Arrays.toString(e.getStackTrace()), e);
			}
		}
		return false;
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
	public boolean containSignature(final ScriptType scriptType, final InputStream inputStream) throws ScriptSignatureException {

		ICodeParser iCodeParser = scriptType.getCodeParser();
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

		} catch (CertificateException e) {
			Logger.error(Activator.PLUGIN_ID, "Error while parsing certificate.", e);
			throw new ScriptSignatureException("Error while parsing certificate.", e);
		} catch (InvalidKeyException e) {
			throw new ScriptSignatureException("Key of the certificate is invalid.", e);

		} catch (NoSuchAlgorithmException e) {
			throw new ScriptSignatureException("No aprovider support this type of algorithm.", e);

		} catch (NoSuchProviderException e) {
			throw new ScriptSignatureException("No provider for this certificate.", e);

		} catch (SignatureException e) {
			// private key with which certificate was signed does not correspond to this public key. Hence it is not self-signed certificate
			return false;
		}
	}
}