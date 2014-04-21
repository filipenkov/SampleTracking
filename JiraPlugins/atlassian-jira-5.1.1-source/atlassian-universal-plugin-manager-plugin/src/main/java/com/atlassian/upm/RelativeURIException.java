package com.atlassian.upm;

import com.atlassian.sal.api.net.ResponseException;

/**
 * Thrown when an attempt is made to download a plugin from a relative URI
 */
public class RelativeURIException extends ResponseException
{
    public RelativeURIException(String message)
    {
        super(message);
    }
}
