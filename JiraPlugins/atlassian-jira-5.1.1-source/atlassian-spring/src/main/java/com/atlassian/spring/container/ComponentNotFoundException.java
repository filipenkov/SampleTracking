package com.atlassian.spring.container;

/**
 * Thrown if the specified component cannot be found in the container
 */
public class ComponentNotFoundException extends RuntimeException
{
    public ComponentNotFoundException(String message)
    {
        super(message);
    }

    public ComponentNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
