package com.atlassian.administration.quicksearch.util;

import com.atlassian.sal.api.ApplicationProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifies if JIRA version is as expected.
 *
 * @since 1.0
 */
public class VersionVerifier
{
    private final String versionString;
    private final Integer majorVersion;
    private final Integer minorVersion;

    public VersionVerifier(ApplicationProperties applicationProperties)
    {
        this.versionString = applicationProperties.getVersion();
        String versionRegex = "^(\\d+)\\.(\\d+)";
        Pattern versionPattern = Pattern.compile(versionRegex);
        Matcher versionMatcher = versionPattern.matcher(versionString);
        if (!versionMatcher.find())
        {
            majorVersion = -1;
            minorVersion = -1;
        }
        else
        {
            majorVersion = Integer.decode(versionMatcher.group(1));
            minorVersion = Integer.decode(versionMatcher.group(2));
        }
    }

    public boolean hasVersion()
    {
        return majorVersion >= 0;
    }

    public int getMajorVersion()
    {
        return majorVersion;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    public boolean isGreaterThan(int minMajorVersion, int minMinorVersion)
    {
        checkVersion();
        return (majorVersion > minMajorVersion) || (majorVersion.equals(minMajorVersion)) && (minorVersion > minMinorVersion);
    }

    public boolean isGreaterThanOrEqualTo(int minMajorVersion, int minMinorVersion)
    {
        checkVersion();
        return (majorVersion > minMajorVersion) || (majorVersion.equals(minMajorVersion)) && (minorVersion >= minMinorVersion);
    }

    public boolean isLessThan(int maxMajorVersion, int maxMinorVersion)
    {
        checkVersion();
        return (majorVersion < maxMajorVersion) || (majorVersion.equals(maxMajorVersion)) && (minorVersion < maxMinorVersion);
    }

    public boolean isLessThanOrEqualTo(int maxMajorVersion, int maxMinorVersion)
    {
        checkVersion();
        return (majorVersion < maxMajorVersion) || (majorVersion.equals(maxMajorVersion)) && (minorVersion <= maxMinorVersion);
    }

    private void checkVersion() {
        if (!hasVersion())
        {
            throw new IllegalStateException("Could not parse version '" + versionString + "'");
        }
    }

}
