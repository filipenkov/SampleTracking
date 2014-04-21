package com.atlassian.applinks.spi.auth;

/**
 * @since 3.0
 */
public class AuthenticationConfigurationException extends Exception
{
    public AuthenticationConfigurationException(final String message)
    {
        super(message);
    }

    public AuthenticationConfigurationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AuthenticationConfigurationException(final Throwable cause)
    {
        super(cause);
    }
}
