package com.atlassian.gadgets.opensocial.spi;

/**
 * Thrown if there is a problem while performing an operation in the PersonService
 *
 *  @since 2.0
 */
public class PersonServiceException extends RuntimeException
{
    public PersonServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PersonServiceException(String message)
    {
        super(message);
    }

    public PersonServiceException(Throwable cause)
    {
        super(cause);
    }
}