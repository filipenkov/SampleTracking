package com.atlassian.upm.osgi.impl;

import com.atlassian.upm.osgi.Version;

import static java.lang.String.format;

/**
 * A wrapper class around OSGi versions
 */
public final class VersionImpl implements Version
{
    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    VersionImpl(org.osgi.framework.Version version)
    {
        this.major = version.getMajor();
        this.minor = version.getMinor();
        this.micro = version.getMicro();
        this.qualifier = version.getQualifier();
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getMicro()
    {
        return micro;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public int compareTo(Version version)
    {
        if (this == version)
        {
            return 0;
        }

        int result = getMajor() - version.getMajor();
        if (result != 0)
        {
            return result;
        }

        result = getMinor() - version.getMinor();
        if (result != 0)
        {
            return result;
        }

        result = getMicro() - version.getMicro();
        if (result != 0)
        {
            return result;
        }

        return getQualifier().compareTo(version.getQualifier());
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || !(other instanceof Version))
        {
            return false;
        }

        return compareTo((Version) other) == 0;
    }

    public int hashCode()
    {
        return 31 * (31 * (31 * major + minor) + micro) + qualifier.hashCode();
    }

    public String toString()
    {
        String result = format("%d.%d.%d", major, minor, micro);
        if (qualifier.length() != 0)
        {
            result += "." + qualifier;
        }
        return result;
    }
}
