package com.atlassian.jira.upgrade;

/**
 * Resolves build numbers to versions of JIRA.
 *
 * @since v4.1
 */
public interface BuildVersionRegistry
{
    /**
     * <p>Takes a build number and attempts to resolve it to a released (or unreleased during development) version of JIRA.
     * If the build number does not directly correspond to a released version of JIRA, we attempt to infer the version
     * by searching for the next known build number above the input.
     *
     * <p>For example, if the build number is <code>207</code>, the version returned would be <code>3.8</code>, as it
     * has a build number of <code>209</code>. In this case, the resulting {@link BuildVersion} object will have a build
     * number corresponding to the target build number and not the input build number, as the target build number is the
     * official number against that version.
     *
     * <p>If the input build number is higher than any known build number, we return the currently running version of JIRA.
     * 
     * @param buildNumber the build number to look up; must be not null and parseable to an integer.
     * @return the version that corresponds to this build number.
     */
    public BuildVersion getVersionForBuildNumber(String buildNumber);

    /**
     * Simple result object for representing the build number and version together. 
     */
    public interface BuildVersion
    {
        String getBuildNumber();

        String getVersion();
    }
}
