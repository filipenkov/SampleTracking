package com.atlassian.upm.osgi.impl;

import com.atlassian.plugin.Plugin;
import com.atlassian.upm.osgi.Version;

public class Versions
{
    /**
     * Parse a version string. Caches the function/result.
     *
     * @param version string to parse as a version
     * @return a new object representing the parsed string
     */
    public static Version fromString(String version)
    {
        return fromString(version, true);
    }

    /**
     * Parse a version string. Has the option of caching the function/result.
     *
     * @param version string to parse as a version
     * @param cacheFunction whether or not the function should be cached
     * @return a new object representing the parsed string
     */
    public static Version fromString(String version, boolean cacheFunction)
    {
        if (cacheFunction)
        {
            return wrap.fromSingleton(parseVersion(version));
        }
        else
        {
            return new VersionImpl(parseVersion(version));
        }
    }

    /**
     * Parse a version string from an installed plugin. Caches the function/result.
     *
     * @param plugin the installed plugin
     * @return a new object representing the parsed string
     */
    public static Version fromPlugin(Plugin plugin)
    {
        return fromPlugin(plugin, true);
    }

    /**
     * Parse a version string from an installed plugin. Has the option of caching the function/result.
     *
     * @param plugin the installed plugin
     * @param cacheFunction whether or not the function should be cached
     * @return a new object representing the parsed string
     */
    public static Version fromPlugin(Plugin plugin, boolean cacheFunction)
    {
        return fromString(plugin.getPluginInformation().getVersion(), cacheFunction);
    }

    private static org.osgi.framework.Version parseVersion(String version)
    {
        version = version.trim();
        if (version.contains("-"))
        {
            String suffix = version.substring(version.indexOf("-"));
            String numericVersion = version.substring(0, version.length() - suffix.length());
            org.osgi.framework.Version osgiVersion = org.osgi.framework.Version.parseVersion(numericVersion);
            //return a new Version including the suffix (without the leading dash)
            return new org.osgi.framework.Version(osgiVersion.getMajor(), osgiVersion.getMinor(), osgiVersion.getMicro(), suffix.substring(1));
        }
        else
        {
            return org.osgi.framework.Version.parseVersion(version);
        }
    }
    
    protected static final Wrapper<org.osgi.framework.Version, Version> wrap = new Wrapper<org.osgi.framework.Version, Version>("version")
    {
        protected Version wrap(org.osgi.framework.Version version)
        {
            return new VersionImpl(version);
        }
    };
}
