package com.atlassian.security.auth.trustedapps;

import java.util.Date;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInvalidCertificateException extends TestCase
{
    private static final String APPLICATION_ID = "jira:6403609";

    private long timeout;
    private ApplicationCertificate certificate;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        timeout = 1000L;
        certificate = mock(ApplicationCertificate.class);
        when(certificate.getApplicationID()).thenReturn(APPLICATION_ID);
        when(certificate.getCreationTime()).thenReturn(new Date());
        when(certificate.getUserName()).thenReturn("admin");
    }

    public void testPlaceholdersAreReplacedWithValues()
    {
        CertificateTooOldException invalidCertificateEx = new CertificateTooOldException(certificate, timeout);

        assertFalse(invalidCertificateEx.toString().contains("{0}"));
        assertTrue(invalidCertificateEx.toString().contains("Application: " + APPLICATION_ID));
    }
}