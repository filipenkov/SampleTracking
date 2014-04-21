package com.atlassian.plugins.rest.common.security;

/**
 * Exception thrown when a client tries to access a resources that requires authentication and the client is not authenticated.
 * @see AnonymousAllowed
 */
public class AuthenticationRequiredException extends SecurityException
{
    public AuthenticationRequiredException()
    {
        super("Client must be authenticated to access this resource.");
    }
}
