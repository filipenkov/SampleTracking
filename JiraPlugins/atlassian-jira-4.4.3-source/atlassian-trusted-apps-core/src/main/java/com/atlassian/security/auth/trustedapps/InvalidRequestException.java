package com.atlassian.security.auth.trustedapps;

/**
 * Thrown by a RequestMatcher if the Request is not to its liking.
 */
public abstract class InvalidRequestException extends TransportException
{
    public InvalidRequestException(TransportErrorMessage error)
    {
        super(error);
    }
}