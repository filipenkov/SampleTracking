package com.atlassian.upm;

/**
 * A runtime exception that wraps errors encountered while installing a plugin
 */
public class PluginInstallException extends RuntimeException
{
    public PluginInstallException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
