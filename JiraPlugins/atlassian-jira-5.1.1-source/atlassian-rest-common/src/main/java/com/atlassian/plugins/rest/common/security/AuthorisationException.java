package com.atlassian.plugins.rest.common.security;

import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;

/**
 * Exception thrown by the {@link SysadminOnlyResourceFilter} to indicate a user is not a system administrator.
 *
 * @since 1.1
 */
public class AuthorisationException extends SecurityException
{
    public AuthorisationException()
    {
        super("Client must be authenticated as a system administrator to access this resource.");
    }
}
