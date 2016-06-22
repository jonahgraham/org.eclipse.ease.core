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
	 * @param blockComStart
	 *            provide starting block comment string
	 * @param blockComEnd
	 *            provide ending block comment string
	 * @return <code>true</code> if signature is written to dataStream and <code>false</code> if signature can't be written for e.g. due to IOException
	 * @throws ScriptSignatureException
	 *             when one or more parameter are <code>null</code> or empty
	 */
	public static boolean appendSignature(final String signStr, final String certStr, String messageDigestAlgo, String provider, final OutputStream dataStream,
			final String blockComStart, final String blockComEnd) throws ScriptSignatureException {

		if (signStr == null || signStr.isEmpty() || certStr == null || certStr.isEmpty() || dataStream == null || blockComStart == null || blockComEnd == null)
			throw new ScriptSignatureException("One or more parameters are null or empty");

		if (messageDigestAlgo == null || messageDigestAlgo.isEmpty() || "default".equalsIgnoreCase(messageDigestAlgo))
			messageDigestAlgo = "SHA1";

		if (provider == null || provider.isEmpty())
			provider = "preferred";

		final String begin = blockComStart + "\n" + BEGIN_STRING, end = END_STRING + "\n" + blockComEnd,
				signatureParam = "Hash:" + messageDigestAlgo + " Provider:" + provider;

		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataStream);
		try {
			/*
			 * By default, last line in every file is ended by \n which is not visible directly. But if generated programmatically, it may not. To be sure, two
			 * \n characters are added.
			 *
			 * Fist to add \n if not already there and second to bring pointer to next line. This gives at most two empty lines and at least one empty line.
			 */
			// TODO remember while appending to file
			// bufferedOutputStream.write("\n\n".getBytes());

			// TODO use createCommentedString method of ICodeFactory

			bufferedOutputStream.write(begin.getBytes());
			bufferedOutputStream.write("\n".getBytes());

			bufferedOutputStream.write(signatureParam.getBytes());
			bufferedOutputStream.write("\n\n".getBytes());

			bufferedOutputStream.write(signStr.getBytes());
			bufferedOutputStream.write("\n\n".getBytes());

			int i = 0;
			for (String s : certStr.split("")) {
				bufferedOutputStream.write(s.getBytes());
				i++;
				if (i % 48 == 0)
					bufferedOutputStream.write("\n".getBytes());
			}

			if (i % 48 != 0)
				bufferedOutputStream.write("\n".getBytes());

			bufferedOutputStream.write("\n".getBytes());
			bufferedOutputStream.write(end.getBytes());
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