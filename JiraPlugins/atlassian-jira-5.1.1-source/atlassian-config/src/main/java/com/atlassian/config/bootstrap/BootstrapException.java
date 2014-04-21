package com.atlassian.config.bootstrap;

/**
 * BootstrapException: reports exceptions for any bootstrapping activities nickf Nov 9, 2004 12:58:36 PM
 */
public class BootstrapException extends Exception
{
    public BootstrapException()
    {
    }

    public BootstrapException(String message)
    {
        super(message);
    }

    public BootstrapException(Throwable cause)
    {
        super(cause);
    }

    public BootstrapException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
