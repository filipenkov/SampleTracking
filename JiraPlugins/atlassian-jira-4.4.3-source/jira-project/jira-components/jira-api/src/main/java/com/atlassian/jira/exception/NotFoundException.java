package com.atlassian.jira.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * The expected resource was not found. Corresponds to a 404 HTTP
 * response.
 */
public class NotFoundException extends NestableRuntimeException
{
    public NotFoundException()
    {
    }

    public NotFoundException(String string)
    {
        super(string);
    }

    public NotFoundException(Throwable throwable)
    {
        super(throwable);
    }

    public NotFoundException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
