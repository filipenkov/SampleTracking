package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

/**
 * Thrown if an IP address in an X-Forwarded-For header doesn't match
 */
public class InvalidXForwardedForAddressException extends InvalidIPAddressException
{
    public InvalidXForwardedForAddressException(String ipAddress)
    {
        super(Code.BAD_XFORWARD_IP, ipAddress);
    }
}