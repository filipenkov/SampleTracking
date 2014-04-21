package com.atlassian.administration.quicksearch.web.condition;

import com.atlassian.administration.quicksearch.util.VersionVerifier;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Condition that enables web resources if application version is greater than or equal to a
 * pre-configured version.
 *
 * @since 1.0
 */
public class VersionGreaterThanOrEqual implements Condition
{

    private Integer minMajorVersion;
    private Integer minMinorVersion;
    private final VersionVerifier versionVerifier;

    public VersionGreaterThanOrEqual(VersionVerifier versionVerifier) {
        this.versionVerifier = versionVerifier;
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException
    {
        minMajorVersion = Integer.decode(paramMap.get("majorVersion"));
        minMinorVersion = Integer.decode(paramMap.get("minorVersion"));
    }

    public boolean shouldDisplay(final Map<String, Object> context) {
        return versionVerifier.isGreaterThanOrEqualTo(minMajorVersion, minMinorVersion);
    }
}
