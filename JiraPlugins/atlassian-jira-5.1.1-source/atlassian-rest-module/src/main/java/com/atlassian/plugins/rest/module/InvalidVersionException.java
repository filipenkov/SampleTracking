package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.Plugin;

public class InvalidVersionException extends RuntimeException
{
    private final String invalidVersion;
    private final Plugin plugin;
    private final RestModuleDescriptor moduleDescriptor;

    InvalidVersionException(String invalidVersion)
    {
        this(null, null, invalidVersion);
    }

    InvalidVersionException(Plugin plugin, RestModuleDescriptor moduleDescriptor, InvalidVersionException e)
    {
        this(plugin, moduleDescriptor, e.getInvalidVersion());
    }

    private InvalidVersionException(Plugin plugin, RestModuleDescriptor moduleDescriptor, String invalidVersion)
    {
        this.plugin = plugin;
        this.moduleDescriptor = moduleDescriptor;
        this.invalidVersion = invalidVersion;
    }

    public String getInvalidVersion()
    {
        return invalidVersion;
    }

    @Override
    public String getMessage()
    {
        if (invalidVersion == null)
        {
            return "The REST module descriptor '" + moduleDescriptor + "'defined by plugin '" + plugin + "' doesn't specify a version, this is a required attribute. "
                    + "Please sepcify a version in the format 'major[.minor][.micro][.classifier]'";
        }
        else
        {
            return "The version (" + invalidVersion + ")set on the REST module descriptor '" + moduleDescriptor + "' of plugin '" + plugin + "' is not valid. " +
                    "It must follow the following pattern 'major[.minor][.micro][.classifier]'";
        }
    }
}
