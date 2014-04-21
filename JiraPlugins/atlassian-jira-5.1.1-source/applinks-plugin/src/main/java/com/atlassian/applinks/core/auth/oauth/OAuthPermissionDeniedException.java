package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.sal.api.net.ResponseException;

/**
 * Thrown when a user refuses to permit an OAuth consumer to access protected resources.
 * 
 * @since 3.6
 *
 */
public class OAuthPermissionDeniedException extends ResponseException
{
    public OAuthPermissionDeniedException(String message)
    {
        super(message);
    }
}
