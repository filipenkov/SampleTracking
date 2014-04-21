/**
 * 
 */
package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;

import com.mockobjects.servlet.MockHttpSession;

/**
 * trusted app protocol version 0
 */
class MockTrustedAppRequestV0 extends MockRequest
{
    MockTrustedAppRequestV0(EncryptedCertificate cert)
    {
        this("/jira/secure/DeleteProject.jspa", cert);
    }

    MockTrustedAppRequestV0(String pathInfo, EncryptedCertificate cert)
    {
        super(pathInfo);
        setSession(new MockHttpSession());
        addHeader(TrustedApplicationUtils.Header.Request.ID, cert.getID());
        addHeader(TrustedApplicationUtils.Header.Request.CERTIFICATE, cert.getCertificate());
        addHeader(TrustedApplicationUtils.Header.Request.SECRET_KEY, cert.getSecretKey());
    }
}