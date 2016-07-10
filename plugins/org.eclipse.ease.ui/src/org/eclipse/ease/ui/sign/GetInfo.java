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

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.List;

public class GetInfo {

	private KeyStore fKeyStore;
	private String fCertificates, fAlias, fKeyStoreFile, fKeyStorePassword, fSignature, fSignProvider, fSignMessageDigestAlgo, fContentOnly;
	private List<String> fKeyStoreFilesList;
	public static final String KEYSTORE_SETTING_FILE = "EASE_KeyStoreFiles", KEYTORE_ALIAS_NODE = "KeystoreAliasPass";
	private boolean fSignaturePresence = false;

	public static String[] getProvider() {
		Provider[] providers = Security.getProviders();
		String[] providerName = new String[providers.length];

		int i = 0;
		for (Provider provider : providers) {
			providerName[i] = provider.getName();
			i++;
		}

		return providerName;
	}

	public static String[] getMessageDigestAlgo() {
		return new String[] { "SHA256", "SHA384", "SHA512", "SHA1", "MD2", "MD5" };
	}

	/**
	 * @param keyStore
	 *            the keyStore to set
	 */
	public void setKeyStore(KeyStore keyStore) {
		fKeyStore = keyStore;
	}

	/**
	 * @return the keyStore
	 */
	public KeyStore getKeyStore() {
		return fKeyStore;
	}

	/**
	 * @param file
	 *            path to keyStore
	 */
	public void setKeyStoreFile(String file) {
		fKeyStoreFile = file;
	}

	/**
	 *
	 * @return the keyStoreFile
	 */
	public String getKeyStoreFile() {
		return fKeyStoreFile;
	}

	/**
	 * @param password
	 *            password of keyStore
	 */
	public void setKeyStorePassword(String password) {
		fKeyStorePassword = password;
	}

	/**
	 *
	 * @return the keyStorePassword
	 */
	public String getKeyStorePassword() {
		return fKeyStorePassword;
	}

	/**
	 * @param signature
	 *            the signature to set
	 */
	public void setSignature(String signature) {
		fSignature = signature;
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return fSignature;
	}

	/**
	 * @param signProvider
	 *            the signProvider to set
	 */
	public void setSignProvider(String signProvider) {
		fSignProvider = signProvider;
	}

	/**
	 * @return the signProvider
	 */
	public String getSignProvider() {
		return fSignProvider;
	}

	/**
	 * @param signMessageDigestAlgo
	 *            the signMessageDigestAlgo to set
	 */
	public void setSignMessageDigestAlgo(String signMessageDigestAlgo) {
		fSignMessageDigestAlgo = signMessageDigestAlgo;
	}

	/**
	 * @return the signMessageDigestAlgo
	 */
	public String getSignMessageDigestAlgo() {
		return fSignMessageDigestAlgo;
	}

	/**
	 * @param certificates
	 *            the certificates to set
	 */
	public void setCertificates(String certificates) {
		fCertificates = certificates;
	}

	/**
	 * @return the certificates
	 */
	public String getCertificates() {
		return fCertificates;
	}

	/**
	 *
	 * @param contain
	 *            tell whether file contains signature
	 */
	public void setSignaturePresence(boolean contain) {
		fSignaturePresence = contain;
	}

	/**
	 *
	 * @return <code>true</code> if file contains signature or <code>false</code> otherwise
	 */
	public boolean getSignaturePresence() {
		return fSignaturePresence;
	}

	/**
	 *
	 * @param content
	 *            provide original content of file excluding signature if file contains signature
	 */
	public void setContentOnly(String content) {
		fContentOnly = content;
	}

	/**
	 *
	 * @return content of file excluding signature
	 */
	public String getContentOnly() {
		return fContentOnly;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(String alias) {
		fAlias = alias;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return fAlias;
	}

	/**
	 * @param list
	 *            list of files to set
	 */
	public void setKeyStoreFiles(List<String> list) {
		fKeyStoreFilesList = list;
	}

	/**
	 * @return the keystore files list
	 */
	public List<String> getKeyStoreFiles() {
		return fKeyStoreFilesList;
	}
}
