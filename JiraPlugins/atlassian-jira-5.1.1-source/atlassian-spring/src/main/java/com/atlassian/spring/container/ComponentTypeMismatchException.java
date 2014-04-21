package com.atlassian.spring.container;

/**
 * Thrown when the specified component is not assignable to the type specified
 */
public class ComponentTypeMismatchException extends RuntimeException
{
    public ComponentTypeMismatchException(String message)
    {
        super(message);
    }

    public ComponentTypeMismatchException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
