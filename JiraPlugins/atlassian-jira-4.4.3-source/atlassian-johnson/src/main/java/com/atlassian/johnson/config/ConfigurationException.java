package com.atlassian.johnson.config;

import org.apache.commons.lang.exception.NestableException;

/**
 * Exception thrown during Johnson configuration
 */
public class ConfigurationException extends NestableException
{
    public ConfigurationException()
    {
    }

    public ConfigurationException(String string)
    {
        super(string);
    }

    public ConfigurationException(Throwable throwable)
    {
        super(throwable);
    }

    public ConfigurationException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
