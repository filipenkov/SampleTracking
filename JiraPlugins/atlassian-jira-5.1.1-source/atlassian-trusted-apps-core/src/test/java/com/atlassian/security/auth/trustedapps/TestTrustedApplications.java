package com.atlassian.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

public class TestTrustedApplications extends TestCase
{
    private final long timeout = 200L;
    private final TestApplication app = new TestApplication("test", timeout);

    public void testRoundTrip() throws Exception
    {
        final EncryptedCertificate encodedCert = app.encode("userX");
        final ApplicationCertificate cert = app.decode(encodedCert, null);
        assertEquals("userX", cert.getUserName());
        assertEquals(app.getID(), cert.getApplicationID());
    }

    public void testNonExpiry() throws InvalidCertificateException
    {
        final EncryptedCertificate encodedCert = app.encode("userX");
        final ApplicationCertificate cert = app.decode(encodedCert, null);
        assertEquals("userX", cert.getUserName());
        assertEquals(app.getID(), cert.getApplicationID());

        // decode again to make sure we can call twice
        app.decode(encodedCert, null);
    }

    public void testExpiry() throws Exception
    {
        final EncryptedCertificate encodedCert = app.encode("userX");
        ApplicationCertificate cert = app.decode(encodedCert, null);
        assertEquals("userX", cert.getUserName());
        assertEquals(app.getID(), cert.getApplicationID());

        Thread.sleep(timeout + 10);

        // decode it again
        try
        {
            cert = app.decode(encodedCert, null);
            fail("This certificate should have expired");
        }
        catch (final InvalidCertificateException e)
        {
            // expected
        }
    }

    static class TestApplication implements CurrentApplication, TrustedApplication
    {
        private final KeyPair keyPair;
        private final String id;
        private final DefaultTrustedApplication trustedApp;
        private final DefaultCurrentApplication curApp;

        public TestApplication(final String id, final long timeout)
        {
            final EncryptionProvider encryptionProvider = new BouncyCastleEncryptionProvider();
            try
            {
                this.keyPair = encryptionProvider.generateNewKeyPair();
            }
            catch (final NoSuchAlgorithmException e)
            {
                throw new RuntimeException(e);
            }
            catch (final NoSuchProviderException e)
            {
                throw new RuntimeException(e);
            }
            this.id = id;
            trustedApp = new DefaultTrustedApplication(encryptionProvider, keyPair.getPublic(), id,
                    null, RequestConditions.builder().setCertificateTimeout(timeout).build())
            {
                @Override
                protected void checkRequest(HttpServletRequest request) throws InvalidCertificateException
                {
                }
            };
            curApp = new DefaultCurrentApplication(keyPair.getPublic(), keyPair.getPrivate(), id);
        }

        public EncryptedCertificate encode(final String userName)
        {
            return encode(userName, null);
        }
        
        public EncryptedCertificate encode(String userName, String urlToSign)
        {
            return curApp.encode(userName, urlToSign);
        }
        
        public String getID()
        {
            return id;
        }

        public PublicKey getPublicKey()
        {
            return trustedApp.getPublicKey();
        }

        public ApplicationCertificate decode(final EncryptedCertificate certificateStr, final HttpServletRequest request) throws InvalidCertificateException
        {
            return trustedApp.decode(certificateStr, request);
        }

        public RequestConditions getRequestConditions()
        {
            return trustedApp.getRequestConditions();
        }

        public String getName()
        {
            return null;
        }
    }
}
