package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

public class InvalidRemoteAddressException extends InvalidIPAddressException
{
    public InvalidRemoteAddressException(String ipAddress)
    {
        super(Code.BAD_REMOTE_IP, ipAddress);
    }
}