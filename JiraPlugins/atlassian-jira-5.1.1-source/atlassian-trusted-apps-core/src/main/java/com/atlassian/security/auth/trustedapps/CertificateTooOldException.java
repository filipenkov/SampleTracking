package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

public class CertificateTooOldException extends InvalidCertificateException
{
    public CertificateTooOldException(ApplicationCertificate certificate, long certificateTimeout)
    {
        super(new TransportErrorMessage(Code.OLD_CERT, "Certificate too old. Application: {0} Certificate Created: {1} Timeout: {2}", certificate.getApplicationID(), String.valueOf(certificate.getCreationTime()), String.valueOf(certificateTimeout)));
    }
}