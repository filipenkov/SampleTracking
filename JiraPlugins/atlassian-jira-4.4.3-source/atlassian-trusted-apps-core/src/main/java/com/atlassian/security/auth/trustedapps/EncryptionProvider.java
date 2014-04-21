package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.ApplicationRetriever.RetrievalException;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Abstracts out the provision of encryption to the trusted app service. For two applications to communicate
 * effectively, they <i>must</i> use the same encryption provider. In our experience, even using the same algorithms
 * but different providers will cause issues.
 * <p>
 * This abstraction is mostly used in unit testing, to avoid having to bring up a fully-fledged crypto provider
 */
public interface EncryptionProvider
{
    /**
     * Retrieve the application certificate from some other application, over HTTP. Will look for the certificate at
     * <code>${baseUrl}/admin/appTrustCertificate</code>. TODO: document the exception policy
     * 
     * @param baseUrl
     *            the base URL of the application to be queried
     * @return the retrieved application certificate
     * @throws RetrievalException
     *             if there are problems with the certificate retrieved from the remote server or the server cannot be
     *             contacted
     * @throws RuntimeException
     *             if there are problems retrieving the certificate from the remote server
     */
    Application getApplicationCertificate(String baseUrl) throws RetrievalException;

    /**
     * Generate a new public/private key pair for an application
     * 
     * @return a new public/private key pair
     * @throws NoSuchAlgorithmException
     *             if the algorithm to generate the keypair is not available
     * @throws NoSuchProviderException
     *             if no appropriate cryptographic provider is available
     */
    KeyPair generateNewKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException;

    /**
     * Generate a unique 32 character String ID. The default implementation combines the local IP address, a secure
     * random number, the current time, and the identity hashcode of a newly created object.
     * 
     * @return a 32 character unique ID string
     */
    String generateUID();

    /**
     * Decode an encrypted certificate to retrieve its ApplicationCertificate
     * 
     * @param encCert
     *            the encrypted certificate of the application
     * @param publicKey
     *            the application's public key
     * @param appId
     *            the application's ID
     * @return the decrypted ApplicationCertificate
     * @throws InvalidCertificateException
     *             if the certificate was malformed, or could not be decrypted
     */
    ApplicationCertificate decodeEncryptedCertificate(EncryptedCertificate encCert, PublicKey publicKey, String appId) throws InvalidCertificateException;

    /**
     * Create a new encrypted certificate for transmission to another application
     * 
     * @param userName
     *            the username to certify
     * @param privateKey
     *            the private key of this application
     * @param appId
     *            the ID of this application
     * @return
     * 
     * 
     * @deprecated use {@link EncryptionProvider#createEncryptedCertificate(String, PrivateKey, String, String)}
     */
    EncryptedCertificate createEncryptedCertificate(String userName, PrivateKey privateKey, String appId);

    /**
     * Create a new encrypted certificate for transmission to another application
     * 
     * @param userName the username to certify
     * @param privateKey the private key of this application
     * @param appId the ID of this application
     * @param urlToSign the target URL of this request, or <code>null</code> for a v1 request
     * @since 2.4
     */
    EncryptedCertificate createEncryptedCertificate(String userName, PrivateKey privateKey, String appId, String urlToSign);
    
    /**
     * Convert an encoded private key into a PrivateKey instance
     * 
     * @param encodedForm
     *            the byte-array representation of the key
     * @return the object representation of the key
     * @throws NoSuchAlgorithmException
     *             if the algorithm to generate the keypair is not available
     * @throws NoSuchProviderException
     *             if no appropriate cryptographic provider is available
     * @throws InvalidKeySpecException
     *             if the encoded form does not contain a valid key
     */
    PrivateKey toPrivateKey(byte[] encodedForm) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;

    /**
     * Convert an encoded public key into a PublicKey instance
     * 
     * @param encodedForm
     *            the byte-array representation of the key
     * @return the object representation of the key
     * @throws NoSuchAlgorithmException
     *             if the algorithm to generate the keypair is not available
     * @throws NoSuchProviderException
     *             if no appropriate cryptographic provider is available
     * @throws InvalidKeySpecException
     *             if the encoded form does not contain a valid key
     */
    PublicKey toPublicKey(byte[] encodedForm) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;
}
