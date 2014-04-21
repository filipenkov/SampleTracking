package com.atlassian.security.auth.trustedapps;


/**
 * Used when something serious is wrong. Should only occur if 
 */
public class SystemException extends InvalidCertificateException
{
    public SystemException(String appId, Exception cause)
    {
        super(new TransportErrorMessage.System(cause, appId), cause);
    }
}