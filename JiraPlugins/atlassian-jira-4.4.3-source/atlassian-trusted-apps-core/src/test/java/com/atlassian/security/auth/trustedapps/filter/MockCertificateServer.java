package com.atlassian.security.auth.trustedapps.filter;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class MockCertificateServer implements TrustedApplicationsFilter.CertificateServer
{
    private String certificate;

    public void writeCertificate(Writer writer) throws IOException
    {
        if (certificate != null)
        {
            writer.write(certificate);
        }
    }

    public String getCertificate()
    {
        return certificate;
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }
}
