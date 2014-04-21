package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage.Code;

/**
 * Thrown when the Requested URL does not match the allowed request urls.
 */
public class InvalidRequestUrlException extends InvalidRequestException
{
    public InvalidRequestUrlException(String url)
    {
        super(new TransportErrorMessage(Code.BAD_URL, "Request not allowed to access URL: {0}",  url));
    }
}