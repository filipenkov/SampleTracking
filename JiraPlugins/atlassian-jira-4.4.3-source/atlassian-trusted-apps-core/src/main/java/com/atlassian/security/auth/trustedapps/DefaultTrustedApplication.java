package com.atlassian.security.auth.trustedapps;

import java.math.BigInteger;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;

/**
 * very basic implementation
 */
public class DefaultTrustedApplication implements TrustedApplication
{
    private final String name;

    static ApplicationCertificate checkCertificateExpiry(final ApplicationCertificate certificate, final long certificateTimeout)
            throws InvalidCertificateException
    {
        if (certificateTimeout != 0L)
        {
            // use BigIntegers to prevent long wrapping
            final BigInteger created = BigInteger.valueOf(certificate.getCreationTime().getTime());
            final BigInteger ttl = BigInteger.valueOf(certificateTimeout);
            final BigInteger now = BigInteger.valueOf(System.currentTimeMillis());

            if (created.add(ttl).compareTo(now) < 0)
            {
                throw new CertificateTooOldException(certificate, certificateTimeout);
            }
        }
        return certificate;
    }

    protected final String id;
    protected final PublicKey publicKey;
    protected final RequestConditions requestConditions;
    protected final RequestValidator requestValidator;
    protected final EncryptionProvider encryptionProvider;

    public DefaultTrustedApplication(final EncryptionProvider encryptionProvider,
            final PublicKey publicKey,
            final String id,
            final String name, final RequestConditions requestConditions)
    {
        Null.not("encryptionProvider", encryptionProvider);
        Null.not("publicKey", publicKey);
        Null.not("id", id);
        Null.not("requestConditions", requestConditions);

        this.encryptionProvider = encryptionProvider;
        this.publicKey = publicKey;
        this.id = id;
        this.name = name;
        this.requestConditions = requestConditions;
        this.requestValidator = new DefaultRequestValidator(requestConditions.getIPMatcher(), requestConditions.getURLMatcher());
    }

     public DefaultTrustedApplication(final EncryptionProvider encryptionProvider,
            final PublicKey publicKey,
            final String id,
            final RequestConditions requestConditions)
    {
        this(encryptionProvider, publicKey, id, null, requestConditions);
    }

    public DefaultTrustedApplication(
            final PublicKey publicKey,
            final String id,
            final RequestConditions requestConditions)
    {
        this(new BouncyCastleEncryptionProvider(), publicKey, id, null, requestConditions);
    }

    public DefaultTrustedApplication(final PublicKey publicKey,
            final String id,
            final String name,
            final RequestConditions requestConditions)
    {
        this(new BouncyCastleEncryptionProvider(), publicKey, id, name, requestConditions);
    }

    public ApplicationCertificate decode(final EncryptedCertificate encCert,
                                         final HttpServletRequest request)
            throws InvalidCertificateException
    {
        final ApplicationCertificate certificate = encryptionProvider.decodeEncryptedCertificate(encCert, publicKey, getID());

        checkCertificateExpiry(certificate, requestConditions.getCertificateTimeout());
        checkRequest(request);

        return certificate;
    }

    public RequestConditions getRequestConditions()
    {
        return requestConditions;
    }

    public String getName()
    {
        return name;
    }

    public String getID()
    {
        return id;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    protected void checkRequest(final HttpServletRequest request) throws InvalidCertificateException
    {
        try
        {
            requestValidator.validate(request);
        }
        catch (final InvalidRequestException e)
        {
            throw new InvalidCertificateException(e);
        }
    }
}
