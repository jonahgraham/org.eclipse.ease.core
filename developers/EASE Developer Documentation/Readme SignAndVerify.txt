EASE SignAndVerify Readme
=========================

Table of contents:
------------------
    1. Introduction
    2. Overview
    3. Code Explanation
        3.1. Classes
            3.1.1. PerformSignature
            3.1.2. VerifySignature
            3.1.3. SignatureHelper


1. Introduction
---------------
The purpose of this document is to give an overview on the current Digital Signature and Verification features of EASE.
It gives an overview of the involved classes and how to perform signature, and perform verification.


2. Overview
-----------
Using EASE, one can run scripts hosted on remote places such as servers, other pc or server on LAN or downloaded script on pc.
Such scripts can be harmful for direct execution since using EASE, these scripts can modify anything on pc.
Hence, mechanism such as digital signature is required to check the credibility of such scripts similar to one being used on emails.

Eclipse does have a mechanism to sign JAR files and it is getting used while giving a new update.
These files are compressed in a JAR file and JAR is signed before release of new software. 
EASE currently does not have any mechanism to sign scripts.
After addition of such a mechanism, using EASE, any user can sign script with his private key and can place that signed script on server for others to use so that his credibility is maintained.

Main classes related to signature are 'PerformSignature' and 'VerifySignature'.
As name suggests, 'PerformSignature' class contains methods to perform signature and 'VerifySignature' contains methods for verifying signature.
Using this feature, user can sign a script file using his private key, update it as file gets modified and then extracts that file to get a script file appended with signature.
At the time of execution of remote scripts, depending upon preferences of user, signature check will take place and user will be informed appropriately.


3. Code Explanation
-------------------
The following sections will be excerpts from the code documentation or short descriptions of code.
They should help explain what the involved parties are, what they are intended to do and most of all they should help understand the other parts of this document.
Note that they are meant as short introductions and that further documentation is available via the JavaDocs.

3.1. Classes
````````````
The following list will give short introductions to the involved classes.
It will quickly describe the most relevant methods that will hopefully help clarify the completion flow later.
Again, for further information please refer to the JavaDocs.

3.1.1. PerformSignature
'''''''''''''''''''''''
Package:
    org.eclipse.ease.sign
Description:
	This class contains methods to perform signature.
	In addition, this class also handles loading of keystore and getting aliases, and certificates from keystore.
Methods of interest:
	- KeyStore loadKeyStore(InputStream, String, String, String) throws ScriptSignatureException, UnrecoverableKeyException, IOException
		Loads keystore using given configuration from inputstream using given password.
	- Collection<String> getAliases(KeyStore)
		Gets all alias from keystore which contain private key.
	- String getCertificate(KeyStore, String, boolean) throws ScriptSignatureException
		Get certificate corresponding to provided alias from provided keystore and can also specify whether certificate must be self-signed or not.
	- byte[] getSignature(Signature, InputStream) throws ScriptSignatureException
		Using initialized instance of java.security.Signature, this method returns signature of data in inputstream
	- String createSignature(KeyStore, InputStream, String, String,	String, String) throws ScriptSignatureException, UnrecoverableKeyException
		Takes input of valid keystore instance, data stream on which to perform signature, alias name, private key password, and parameters to perform signature and returns signature in Base64 format.

3.1.2. VerifySignature
''''''''''''''''''''''
Package:
    org.eclipse.ease.sign
Description:
	This class contains methods to verify signature.
	In addition, this class also contains methods to check validity of attached certificate, tell whether attached certificate is self-signed, and tell whether certificate is signed by a trusted certificate authority.
Constructor:
	- VerifySignature(SignatureInfo)
		This private constructor is used by getInstance methods described below to get instance of this class by setting signature information.
Methods of interest:
	- VerifySignature getInstance(ScriptType, InputStream) throws ScriptSignatureException
		Returns an instance of this class if contents of inputstream contains a signature.
	- VerifySignature getInstance(ScriptType, InputStream, InputStream) throws ScriptSignatureException
		Used when file contents and signature are at different place.
		Returns an instance of this class if signature content is proper.
	- Certificate getCertificate(byte[]) throws ScriptSignatureException
		This private method is used to get java.security.cert.Certificate object from bytes.
	- List<Certificate> getCertificateChain() throws ScriptSignatureException
		This private method is used to get certificate chain attached with signature.
	- boolean isSelfSignedCertificate() throws ScriptSignatureException
		Checks whether attached signature with which this constructor is initialized, contains self-signed certificate.
		It extracts first certificate from certificate chain and perform check.
	- boolean isCertChainValid(InputStream, char[]) throws ScriptSignatureException
		Using truststore provided as inputstream and its password, this method checks whether certificate chain in signature is valid certificate chain.
		In other words, whether user certificate is signed by a trusted authority.
	- boolean isCertChainValid() throws ScriptSignatureException
		Using default truststore "path_to_jre/lib/security/cacerts" as truststore and "changeit" as password checks whether certificate chain in signature is valid certificate chain.
		In other words, whether user certificate is signed by a trusted authority.
	- boolean verify() throws ScriptSignatureException
		Confirms that signature is of corresponding file only.
		In other words, if file is modified after application of signature, signature verification will return false and file is considered harmful to execute.

3.1.3. SignatureHelper
''''''''''''''''''''''
Package:
    org.eclipse.ease.sign
Description:
	This class contains helper methods for performing signature.
Methods of interest:
	- String convertBytesToBase64(byte[])
		Converts provided bytes to Base64 String.
	- byte[] convertBase64ToBytes(String)
		Converts provided Base64 string to bytes.
	- boolean appendSignature(ScriptType, String, String, String, String, OutputStream) throws ScriptSignatureException
		This method appends signature to outputstream with provided parameters.
	- boolean containSignature(ScriptType, InputStream) throws ScriptSignatureException
		This method checks whether provided inputstream contain signature in proper format for which it uses org.eclipse.ease.service.ScriptType class to get code specific comments.
	- boolean isSelfSignedCertificate(Certificate) throws ScriptSignatureException
		This method tells whether provided certificate is self-signed or not.