package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.EncryptedCertificate;
import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils;

/**
 * version 1 adds magic number and protocol version headers
 */
class MockTrustedAppRequestV1 extends MockTrustedAppRequestV0
{
    MockTrustedAppRequestV1(String pathInfo, EncryptedCertificate cert)
    {
        super(pathInfo, cert);
        addHeader(TrustedApplicationUtils.Header.Request.VERSION, cert.getProtocolVersion().toString());
        addHeader(TrustedApplicationUtils.Header.Request.MAGIC, cert.getMagicNumber());
    }

    MockTrustedAppRequestV1(EncryptedCertificate cert)
    {
        this("/some/path", cert);
    }
}
