package com.atlassian.security.auth.trustedapps;

/**
 * Contains the encoded certificate information to be included in the trusted requests between applications. The
 * provided information to be set in the request header using the following parameters:
 * {@link TrustedApplicationUtils.Header.Request#ID} {@link TrustedApplicationUtils.Header.Request#CERTIFICATE}
 * {@link TrustedApplicationUtils.Header.Request#SECRET_KEY}
 */
public interface EncryptedCertificate
{
    /**
     * ID of the trusted application that encrypted this certificate
     */
    String getID();

    /**
     * Secret Key for decrypting the certificate.
     * <p>
     * Encrypted with the private key of the trusted application and Base64 encoded
     */
    String getSecretKey();

    /**
     * String that contains three lines:
     * <p>
     * Encrypted with the secret key and Base64 encoded
     */
    String getCertificate();

    /**
     * Protocol version.
     * <p>
     * Not encrypted.
     */
    Integer getProtocolVersion();

    /**
     * Magic Number for Transmission decryption validation.
     * <p>
     * Encrypted with the private key of the trusted application and Base64 encoded
     */
    String getMagicNumber();
    
    /**
     * A Base64-encoded signature, of a timestamp and the URL this request is intended for,
     * signed with the private key of the client trusted application. This may be <code>null</code>
     * for a protocol version 1 request.
     * 
     * @since 2.4
     */
    String getSignature();
}
