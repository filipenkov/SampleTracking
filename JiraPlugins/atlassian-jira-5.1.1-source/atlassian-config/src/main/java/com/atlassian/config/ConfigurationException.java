package com.atlassian.config;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 12:10:21
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationException extends Exception
{
    public ConfigurationException(String message)
    {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
