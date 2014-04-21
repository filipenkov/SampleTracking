package com.atlassian.plugins.rest.module;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiVersion implements Comparable
{
    private final static String DOT = ".";

    private final static Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\.([\\w-]*))?");

    private final Integer major;

    private final Integer minor;

    private final Integer micro;

    private final String classifier;

    public ApiVersion(String version)
    {
        if (version == null)
        {
            throw new InvalidVersionException(version);
        }

        final Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.matches())
        {
            throw new InvalidVersionException(version);
        }

        major = Integer.valueOf(matcher.group(1));
        minor = matcher.group(2) != null ? Integer.valueOf(matcher.group(2)) : null;
        micro = matcher.group(3) != null ? Integer.valueOf(matcher.group(3)) : null;
        classifier = matcher.group(4);
    }

    public Integer getMajor()
    {
        return major;
    }

    public Integer getMinor()
    {
        return minor;
    }

    public Integer getMicro()
    {
        return micro;
    }

    public String getClassifier()
    {
        return classifier;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (o.getClass() != this.getClass())
        {
            return false;
        }

        final ApiVersion version = (ApiVersion) o;
        return new EqualsBuilder()
                .append(this.major, version.major)
                .append(this.minor, version.minor)
                .append(this.micro, version.micro)
                .append(this.classifier, version.classifier).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 41).append(major).append(minor).append(micro).append(classifier).toHashCode();
    }

    public int compareTo(Object o)
    {
        if (o == null)
        {
            return 1;
        }
        if (o == this)
        {
            return 0;
        }
        if (o.getClass() != this.getClass())
        {
            return 1;
        }

        final ApiVersion that = (ApiVersion) o;

        final int majorDifference = compare(this.major, that.major);
        if (majorDifference != 0)
        {
            return majorDifference;
        }
        final int minorDifference = compare(this.minor, that.minor);
        if (minorDifference != 0)
        {
            return minorDifference;
        }
        final int microDifference = compare(this.micro, that.micro);
        if (microDifference != 0)
        {
            return microDifference;
        }
        return compare(this.classifier, that.classifier);
    }

    private <T extends Comparable<T>> int compare(T n, T m)
    {
        if (n == null && m == null)
        {
            return 0;
        }
        if (n == null)
        {
            return -1;
        }
        if (m == null)
        {
            return +1;
        }
        return n.compareTo(m);
    }

    @Override
    public String toString()
    {
        return major
                + (minor != null ? DOT + minor : "")
                + (micro != null ? DOT + micro : "")
                + (StringUtils.isNotBlank(classifier) ? DOT + classifier : "");
    }
}
