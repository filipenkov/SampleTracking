package com.atlassian.security.auth.trustedapps;

import static com.atlassian.security.auth.trustedapps.DefaultTrustedApplication.checkCertificateExpiry;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;

public class DefaultCurrentApplication implements CurrentApplication, TrustedApplication
{
    /**
     * A current application should only be called by itself, therefore the timeout only needs to be small.
     */
    private static final int LOCAL_TIMEOUT = 1000;

    private final EncryptionProvider encryptionProvider;

    protected final String id;
    protected final PublicKey publicKey;
    protected final PrivateKey privateKey;

    public DefaultCurrentApplication(final EncryptionProvider encryptionProvider, final PublicKey publicKey, final PrivateKey privateKey, final String id)
    {
        Null.not("encryptionProvider", encryptionProvider);
        Null.not("publicKey", publicKey);
        Null.not("privateKey", privateKey);
        Null.not("id", id);

        this.encryptionProvider = encryptionProvider;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.id = id;
    }

    public DefaultCurrentApplication(final PublicKey publicKey, final PrivateKey privateKey, final String id)
    {
        this(new BouncyCastleEncryptionProvider(), publicKey, privateKey, id);
    }

    /**
     * Returned String can be used as a certificate to talk
     * to the server that trusts this application. I.e. the ID of this app and the certificate go into the following header parameters:
     * {@link CurrentApplication#HEADER_TRUSTED_APP_CERT}
     * {@link CurrentApplication#HEADER_TRUSTED_APP_ID}
     */
    public EncryptedCertificate encode(final String userName)
    {
        return encode(userName, null);
    }

    public EncryptedCertificate encode(String userName, String urlToSign)
    {
        return encryptionProvider.createEncryptedCertificate(userName, privateKey, getID(), urlToSign);
    }
    
    public ApplicationCertificate decode(final EncryptedCertificate encCert, final HttpServletRequest request) throws InvalidCertificateException
    {
        final ApplicationCertificate certificate = encryptionProvider.decodeEncryptedCertificate(encCert, publicKey, getID());
        checkCertificateExpiry(certificate, LOCAL_TIMEOUT);
        return certificate;
    }

    public String getID()
    {
        return id;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    /**
     * {@inheritDoc}
     */
    public RequestConditions getRequestConditions()
    {
        return null;
    }

    public String getName()
    {
        return null;
    }
}