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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;

public class VerifySignature {

	private final SignatureInfo fSignatureInfo;

	/**
	 * Use this method to get constructor when signature is attached to script file.
	 *
	 * @param scriptType
	 *            provide {@link ScriptType} instance of stream for script
	 * @param inputStream
	 *            provide stream of script to verify
	 * @return instance of {@link VerifySignature} when signature is present and can be properly loaded or null when signature is not present
	 * @throws ScriptSignatureException
	 *             when one or more parameters are not provided or signature format is improper
	 */
	public static VerifySignature getInstance(final ScriptType scriptType, final InputStream inputStream) throws ScriptSignatureException {
		return getInstance(scriptType, inputStream, null);
	}

	/**
	 * Use this method to get constructor when script contents and signature are separate. Use only when it is guaranteed that input stream of signature is for
	 * corresponding input stream of file.
	 *
	 * @param scriptType
	 *            provide {@link ScriptType} instance of stream for script
	 * @param inputStream
	 *            provide stream of script to verify
	 * @param signatureInputStream
	 *            provide stream where signature is stored
	 * @return instance of {@link VerifySignature} when signature can be properly loaded or null when signature is not present
	 * @throws ScriptSignatureException
	 *             when one or more parameters are not provided or signature format is improper
	 */
	public static VerifySignature getInstance(final ScriptType scriptType, final InputStream inputStream, final InputStream signatureInputStream)
			throws ScriptSignatureException {

		if (scriptType == null || inputStream == null)
			throw new ScriptSignatureException("One or more parameters are not provided");

		ICodeParser iCodeParser = scriptType.getCodeParser();
		if (signatureInputStream == null) {
			SignatureInfo signatureInfo = iCodeParser.getSignatureInfo(inputStream);
			if (signatureInfo != null) {
				if (signatureInfo.getSignature() == null || signatureInfo.getProvider() == null || signatureInfo.getMessageDigestAlgo() == null
						|| signatureInfo.getCertificateChain() == null || signatureInfo.getContentOnly() == null)
					throw new ScriptSignatureException("Error while parsing script. Try again.");

				return new VerifySignature(signatureInfo);

			} else
				return null;

		} else {
			SignatureInfo signatureInfo = iCodeParser.getSignatureInfo(signatureInputStream);
			if (signatureInfo != null) {
				if (signatureInfo.getSignature() == null || signatureInfo.getProvider() == null || signatureInfo.getMessageDigestAlgo() == null
						|| signatureInfo.getCertificateChain() == null)
					throw new ScriptSignatureException("Error while parsing script. Try again.");

				BufferedInputStream bInput = new BufferedInputStream(inputStream);
				StringBuffer sBuf = new StringBuffer();

				int cur;
				try {
					while ((cur = bInput.read()) >= 0)
						sBuf.append((char) cur);

					signatureInfo.setContentOnly(sBuf.toString());

					return new VerifySignature(signatureInfo);

				} catch (IOException e) {
					Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
					throw new ScriptSignatureException("An IO error occurred while reading file.", e);

				} finally {
					try {
						if (bInput != null)
							bInput.close();
					} catch (IOException e) {
						Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
					}

				}

			} else
				return null;

		}
	}

	/**
	 *
	 * @param signatureInfo
	 *            provide {@link SignatureInfo} containing the signature and contents of file on which signature was applied
	 */
	private VerifySignature(SignatureInfo signatureInfo) {
		fSignatureInfo = signatureInfo;
	}

	/**
	 * Converts byte array to corresponding certificate.
	 *
	 * @param bytesCert
	 *            provide certificate in bytes to convert it to {@link Certificate}
	 * @return an instance of {@link Certificate}
	 * @throws ScriptSignatureException
	 *             when there is an error while retrieving certificate
	 */
	private Certificate getCertificate(byte[] bytesCert) throws ScriptSignatureException {

		CertificateFactory certificateFactory;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
			return certificateFactory.generateCertificate(new ByteArrayInputStream(bytesCert));

		} catch (CertificateException e) {
			throw new ScriptSignatureException("Error while retrieving certificate.", e);
		}
	}

	/**
	 * Converts certificate chain in form of string array to list.
	 *
	 * @return {@link List} of {@link Certificate}
	 * @throws ScriptSignatureException
	 *             when there is an error while retrieving certificate
	 */
	private List<Certificate> getCertificateChain() throws ScriptSignatureException {

		String certChainString[] = fSignatureInfo.getCertificateChain();
		int noOfCert = certChainString.length;
		byte[][] certChainByte = new byte[noOfCert][];

		for (int i = 0; i < noOfCert; i++)
			certChainByte[i] = SignatureHelper.convertBase64ToBytes(certChainString[i]);

		ArrayList<Certificate> certificateList = new ArrayList<>();
		for (byte cert[] : certChainByte)
			certificateList.add(getCertificate(cert));

		return certificateList;
	}

	/**
	 * Checks whether certificate attached with script is self-signed or not.
	 *
	 * @return <code>true</code> if certificate is self-signed or <code>false</code> if certificate is CA signed
	 * @throws ScriptSignatureException
	 *             when script does not contain signature or there is an error while retrieving certificate
	 */
	public boolean isSelfSignedCertificate() throws ScriptSignatureException {

		if (fSignatureInfo != null) {
			ArrayList<Certificate> certificateList = (ArrayList<Certificate>) getCertificateChain();
			Certificate certificate = certificateList.get(0);

			return SignatureHelper.isSelfSignedCertificate(certificate);
		}
		throw new ScriptSignatureException("Script does not contain signature.");
	}

	/**
	 * Checks the validity of certificate. If certificate is CA signed, then it checks the validity of CA with trust-store.
	 *
	 * @param trustStoreLocation
	 *            provide location of truststore
	 * @param trustStorePassword
	 *            provide password for truststore
	 * @return <code>true</code> if certificate is valid and trusted or <code>false</code> if certificate is invalid or not trusted
	 * @throws ScriptSignatureException
	 *             when truststore can't be loaded due to one or more certificates can't be loaded from it or appropriate provider can't be found or truststore
	 *             file can't be read or password does not correspond to truststore or truststore does not contain any trusted certificate entry or script does
	 *             not contain signature
	 */
	public boolean isCertChainValid(InputStream trustStoreLocation, char[] trustStorePassword) throws ScriptSignatureException {

		if ((trustStoreLocation == null && trustStorePassword != null) || (trustStoreLocation != null && trustStorePassword == null))
			throw new ScriptSignatureException("Either both or none of the parameters should be null");

		if (fSignatureInfo != null) {
			InputStream iStream = null;
			try {
				if (trustStoreLocation == null && trustStorePassword == null) {
					// TODO check following command for windows
					iStream = new FileInputStream(System.getProperty("java.home") + "/lib/security/" + "cacerts");
					trustStorePassword = "changeit".toCharArray();
				} else
					iStream = ResourceTools.getInputStream(trustStoreLocation);

				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

				ArrayList<Certificate> certificateList = (ArrayList<Certificate>) getCertificateChain();

				int certLength = certificateList.size();
				if (SignatureHelper.isSelfSignedCertificate(certificateList.get(certLength - 1)))
					certificateList.remove(certLength - 1);

				CertPath certPath = certificateFactory.generateCertPath(certificateList);

				CertPathValidator validator = CertPathValidator.getInstance("PKIX");

				KeyStore keystore = KeyStore.getInstance("JKS");
				keystore.load(iStream, trustStorePassword);

				PKIXParameters params = new PKIXParameters(keystore);
				params.setRevocationEnabled(true);

				// If certificate does not contain OSCP or CRL responder than that certificate will be considered invalid
				Security.setProperty("ocsp.enable", "true");
				System.setProperty("com.sun.net.ssl.checkRevocation", "true");
				System.setProperty("com.sun.security.enableCRLDP", "true");

				// Validate will throw an exception on invalid chains.
				validator.validate(certPath, params);
				return true;

			} catch (CertificateException e) {
				throw new ScriptSignatureException("One or more certificates can't be loaded.", e);

			} catch (NoSuchAlgorithmException e) {
				throw new ScriptSignatureException("Algorithm used for securing truststore can't be found. Chose another Truststore.", e);

			} catch (KeyStoreException e) {
				throw new ScriptSignatureException("Truststore can't be loaded.");

			} catch (IOException e) {
				if (e.getCause() instanceof UnrecoverableKeyException)
					throw new ScriptSignatureException("Invalid Truststore Password.", e);
				else if (e.getCause() instanceof FileNotFoundException || e.getCause() instanceof SecurityException)
					throw new ScriptSignatureException("File can't be read. Chose another Truststore or try again.", e);

				Logger.error(Activator.PLUGIN_ID, Arrays.toString(e.getStackTrace()), e);
				throw new ScriptSignatureException("Error loading Truststore. Try again.", e);

			} catch (InvalidAlgorithmParameterException e) {
				Logger.error(Activator.PLUGIN_ID, Arrays.toString(e.getStackTrace()), e);
				throw new ScriptSignatureException("Can't perform validation.", e);

			} catch (CertPathValidatorException e) {
				// if any invalidation occurs, exception will be caught here
				throw new ScriptSignatureException(e.getMessage());

			} finally {
				try {
					if (iStream != null)
						iStream.close();
				} catch (IOException e) {
					Logger.error(Activator.PLUGIN_ID, Arrays.toString(e.getStackTrace()), e);
				}
			}
		}
		throw new ScriptSignatureException("Script does not contain signature.");
	}

	/**
	 * Checks the validity of certificate. If certificate is CA signed, then it checks the validity of CA with trust-store. It uses default truststore present
	 * at JRE_PATH/lib/security/cacerts and "changeit" as password. If password has been modified, use {@link #isCertChainValid(String, char[])}.
	 *
	 * @return <code>true</code> if certificate is valid and trusted or <code>false</code> if certificate is invalid or not trusted
	 * @throws ScriptSignatureException
	 *             when one or more certificates can't be loaded from truststore or truststore can't be loaded
	 */
	public boolean isCertChainValid() throws ScriptSignatureException {

		return isCertChainValid(null, null);
	}

	/**
	 * Verify given signature with provided public key of provided certificate.
	 *
	 * @return <code>true</code> if signature is valid or <code>false</code> if signature is invalid
	 * @throws ScriptSignatureException
	 *             when script does not contain signature or there is an error while retrieving certificate
	 */
	public boolean verify() throws ScriptSignatureException {

		if (fSignatureInfo != null) {
			byte[] signByte = SignatureHelper.convertBase64ToBytes(fSignatureInfo.getSignature());
			byte[] certByte = SignatureHelper.convertBase64ToBytes(fSignatureInfo.getCertificateChain()[0]);
			Certificate userCert = getCertificate(certByte);

			try {
				PublicKey publicKey = userCert.getPublicKey();
				String encryptionAlgo = publicKey.getAlgorithm();

				Signature signature = Signature.getInstance(fSignatureInfo.getMessageDigestAlgo() + "with" + encryptionAlgo, fSignatureInfo.getProvider());

				// initialize signature instance with public key
				signature.initVerify(publicKey);

				// perform verification
				signature.update(fSignatureInfo.getContentOnly().getBytes());

				return signature.verify(signByte);

			} catch (SignatureException e) {
				Logger.error(Activator.PLUGIN_ID, "Signature object not initialized properly or signature is not readable.", e);
				throw new ScriptSignatureException("Signature is not readable.", e);

			} catch (NoSuchAlgorithmException e) {
				throw new ScriptSignatureException("Algorithm used by signature is not recognized by provider.", e);

			} catch (InvalidKeyException e) {
				throw new ScriptSignatureException("Public key is invalid.", e);

			} catch (NoSuchProviderException e) {
				throw new ScriptSignatureException("No such provider is registered in Security Providers' list.", e);

			}
		}
		throw new ScriptSignatureException("Script does not contain signature.");
	}
}
