package com.atlassian.jira.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * This exception is thrown when the data store exception is caught by the persistence code.
 * As there is very little that can be done about persistence exception on the "business logic" layer of the
 * application this is a Runtime exception.
 */
public class DataAccessException extends NestableRuntimeException
{
    public DataAccessException(String msg)
    {
        super(msg);
    }

    public DataAccessException(Throwable throwable)
    {
        super(throwable);
    }

    public DataAccessException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
