package com.atlassian.upm;

/**
 * A runtime exception thrown when a handler cannot be found for the requested plugin type
 */
public class UnknownPluginTypeException extends RuntimeException
{
    UnknownPluginTypeException(String message)
    {
        super(message);
    }
}
