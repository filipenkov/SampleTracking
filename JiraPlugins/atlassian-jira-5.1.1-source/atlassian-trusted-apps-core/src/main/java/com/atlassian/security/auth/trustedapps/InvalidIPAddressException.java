package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

/**
 * Thrown when the IP address of the client does not match the allowed IP addresses
 */
public abstract class InvalidIPAddressException extends InvalidRequestException
{
    public InvalidIPAddressException(Code code, String address)
    {
        super(new TransportErrorMessage(code, "Request not allowed from IP address: {0}", address));
    }
}