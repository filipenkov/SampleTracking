package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.request.TrustedRequest;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link TrustedApplicationUtils}
 */
public class TestTrustedApplicationUtils
{
    @Test
    public void testAddRequestParameters()
    {
        final String id = "some id";
        final String certificate = "some cert";
        final String secretKey = "some secret key";
        final String magic = "some magic number";

        EncryptedCertificate mockEncryptedCertificate = mock(EncryptedCertificate.class);
        TrustedRequest trustedRequest = mock(TrustedRequest.class);

        when(mockEncryptedCertificate.getID()).thenReturn(id);
        when(mockEncryptedCertificate.getCertificate()).thenReturn(certificate);
        when(mockEncryptedCertificate.getSecretKey()).thenReturn(secretKey);
        when(mockEncryptedCertificate.getMagicNumber()).thenReturn(magic);
        when(mockEncryptedCertificate.getProtocolVersion()).thenReturn(Integer.valueOf(1));

        TrustedApplicationUtils.addRequestParameters(mockEncryptedCertificate, trustedRequest);

        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.ID, id);
        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.CERTIFICATE, certificate);
        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.SECRET_KEY, secretKey);
        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.MAGIC, magic);
        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.VERSION, TrustedApplicationUtils.Constant.VERSION.toString());
    }
    
    @Test
    public void addRequestParametersIncludesSignature()
    {
        EncryptedCertificate mockEncryptedCertificate = mock(EncryptedCertificate.class);
        TrustedRequest trustedRequest = mock(TrustedRequest.class);
        
        when(mockEncryptedCertificate.getSignature()).thenReturn("base64-encoded-rsa-signature");
        
        TrustedApplicationUtils.addRequestParameters(mockEncryptedCertificate, trustedRequest);
        
        verify(trustedRequest).addRequestParameter(TrustedApplicationUtils.Header.Request.SIGNATURE, "base64-encoded-rsa-signature");
    }
}
