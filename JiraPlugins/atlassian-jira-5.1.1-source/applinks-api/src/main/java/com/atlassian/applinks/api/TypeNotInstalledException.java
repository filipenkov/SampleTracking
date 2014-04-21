package com.atlassian.applinks.api;

/**
 * Thrown if the requested {@link ApplicationLink} or {@link EntityLink}'s {@link ApplicationType} or
 * {@link EntityType} is currently not installed. This will only occur in the case where an additional plugin that
 * provides a custom {@link ApplicationType} or {@link EntityType} is installed, a new link of that custom type is
 * registered and then the plugin is uninstalled without deleting the link.
 *
 * @since 3.0
 */
public class TypeNotInstalledException extends Exception
{
    private final String type;

    public TypeNotInstalledException(final String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public String getMessageKey()
    {
        return "applinks.type.not.installed";
    }
}
