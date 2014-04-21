package com.atlassian.crowd.manager.application;

/**
 * Represents an error attempting to modify application configuration.
 */
public class ApplicationManagerException extends Exception
{
    public ApplicationManagerException()
    {
        super();
    }

    public ApplicationManagerException(String s)
    {
        super(s);
    }

    public ApplicationManagerException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public ApplicationManagerException(Throwable throwable)
    {
        super(throwable);   
    }
}
