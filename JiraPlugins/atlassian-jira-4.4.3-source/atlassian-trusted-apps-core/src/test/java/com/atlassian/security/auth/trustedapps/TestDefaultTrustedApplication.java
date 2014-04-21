package com.atlassian.security.auth.trustedapps;

import java.security.PublicKey;

import junit.framework.TestCase;

public class TestDefaultTrustedApplication extends TestCase
{
    public void testProductionCtor() throws Exception
    {
        final PublicKey key = new MockKey();
        final String id = "TestDefaultTrustedApplication:id";
        final RequestConditions conditions = RequestConditions.builder().setCertificateTimeout(13256L).build();

        // no exceptions
        TrustedApplication app =  new DefaultTrustedApplication(key, id, conditions);
        assertSame(key,  app.getPublicKey());
        assertEquals(id, app.getID());
    }

    public void testProductionCtorThrowsNullKey() throws Exception
    {
        final PublicKey key = null;
        final String id = "TestDefaultTrustedApplication:id";
        final RequestConditions conditions = RequestConditions.builder().setCertificateTimeout(13256L).build();
        try
        {
            new DefaultTrustedApplication(key, id, conditions);
            fail("Should have thrown IllegalArgEx");
        }
        catch (IllegalArgumentException yay)
        {
        }
    }

    public void testProductionCtorThrowsNullId() throws Exception
    {
        final PublicKey key = new MockKey();
        final String id = null;
        final RequestConditions conditions = RequestConditions.builder().setCertificateTimeout(13256L).build();
        try
        {
            new DefaultTrustedApplication(key, id, conditions);
            fail("Should have thrown IllegalArgEx");
        }
        catch (IllegalArgumentException yay)
        {
        }
    }

    public void testProductionCtorThrowsNullRequestConditions() throws Exception
    {
        final PublicKey key = new MockKey();
        final String id = "TestDefaultTrustedApplication:id";
        try
        {
            new DefaultTrustedApplication(key, id, null);
            fail("Should have thrown IllegalArgEx");
        }
        catch (IllegalArgumentException yay)
        {
        }
    }

    public void testTimeout()
    {
        final ApplicationCertificate certificate =
                new DefaultApplicationCertificate("foo", "joe", System.currentTimeMillis());

        assertValidCertificate(certificate, 0L);
        assertValidCertificate(certificate, 10000L);
        assertValidCertificate(certificate, Long.MAX_VALUE);

        assertExpiredCertificate(certificate, -1L);
        assertExpiredCertificate(certificate, Long.MIN_VALUE);
    }

    private void assertValidCertificate(final ApplicationCertificate certificate, long timeout)
    {
        try
        {
            DefaultTrustedApplication.checkCertificateExpiry(certificate, timeout);
        }
        catch (InvalidCertificateException e)
        {
            fail("Certificate should be valid.");
        }
    }

    private void assertExpiredCertificate(final ApplicationCertificate certificate, long timeout)
    {
        try
        {
            DefaultTrustedApplication.checkCertificateExpiry(certificate, timeout);
            fail("Certificate should be expired.");
        }
        catch (InvalidCertificateException e)
        {
        }
    }
}