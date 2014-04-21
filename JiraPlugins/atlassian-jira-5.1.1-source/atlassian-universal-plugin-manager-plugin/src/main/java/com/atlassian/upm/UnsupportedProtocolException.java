package com.atlassian.upm;

import com.atlassian.sal.api.net.ResponseException;

/**
 * Thrown when a user attempts to download a plugin from a URI with an unsupported protocol
 */

public class UnsupportedProtocolException extends ResponseException
{
    public UnsupportedProtocolException(String message)
    {
        super(message);
    }
}
