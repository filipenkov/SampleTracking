package com.atlassian.upm;

/**
 * Thrown when a user attempts to install a plugin with an non-empty, unrecognised plugins-version field.
 */
public class UnrecognisedPluginVersionException extends RuntimeException
{
    final private String version;

    public UnrecognisedPluginVersionException(String version)
    {
        super("Attempted to install a plugin with version: " + version);
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

}
