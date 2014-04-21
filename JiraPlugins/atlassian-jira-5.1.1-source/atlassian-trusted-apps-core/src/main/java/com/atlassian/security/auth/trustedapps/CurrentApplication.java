package com.atlassian.security.auth.trustedapps;

/**
 * Represents current application. This object is used when establishing trust with other servers and requesting them to
 * perform privileged operations for this application.
 */
public interface CurrentApplication extends Application
{
    /**
     * @deprecated since 0.34 use {@link TrustedApplicationUtils.Header.Request#ID} instead.
     */
    @Deprecated
    public static final String HEADER_TRUSTED_APP_ID = TrustedApplicationUtils.Header.Request.ID;

    /**
     * @deprecated since 0.34 use {@link TrustedApplicationUtils.Header.Request#CERT} instead.
     */
    @Deprecated
    public static final String HEADER_TRUSTED_APP_CERT = TrustedApplicationUtils.Header.Request.CERTIFICATE;

    /**
     * @deprecated since 0.34 use {@link TrustedApplicationUtils.Header.Request#SECRET_KEY} instead.
     */
    @Deprecated
    public static final String HEADER_TRUSTED_APP_SECRET_KEY = TrustedApplicationUtils.Header.Request.SECRET_KEY;

    /**
     * @deprecated since 0.34 use {@link TrustedApplicationUtils.Header.Response#ERROR} instead.
     */
    @Deprecated
    public static final String HEADER_TRUSTED_APP_ERROR = TrustedApplicationUtils.Header.Response.ERROR;

    /**
     * @deprecated since 0.34 use {@link TrustedApplicationUtils.Header.Response#STATUS} instead.
     */
    @Deprecated
    public static final String HEADER_TRUSTED_APP_STATUS = TrustedApplicationUtils.Header.Response.STATUS;

    /**
     * Generates a new certificate that will be sent to the remote server when asking to perform privileged operation
     * for this application.
     * 
     * @return encrypted certificate representing this application
     * @throws InvalidCertificateException
     * @deprecated since 2.4
     * @see #encode(String, String)
     */
    EncryptedCertificate encode(String userName);
    
    /**
     * <p>Generates a new certificate that will be sent to the remote server when asking to perform privileged operation
     * for this application.</p>
     * <p><code>urlToSign</code> should be non-null for a v2 request. Some servers may only accept
     * v2 requests.</p>
     * 
     * @param urlToSign the target URL for this operation. If <code>null</code>, only a v1 certificate will be generated
     * @return encrypted certificate representing this application
     * @throws InvalidCertificateException
     * @since 2.4
     */
    EncryptedCertificate encode(String userName, String urlToSign);
}