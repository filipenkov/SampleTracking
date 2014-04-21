package com.atlassian.voorhees;

/**
 * Some exception was thrown by the underlying application while trying to service a request.
 */
public class ApplicationException extends Exception
{
    public ApplicationException(Throwable throwable)
    {
        super(throwable);
    }
}
