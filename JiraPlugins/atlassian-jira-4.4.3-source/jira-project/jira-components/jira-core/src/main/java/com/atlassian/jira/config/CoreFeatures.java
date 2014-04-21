package com.atlassian.jira.config;

/**
 * Core manipulable JIRA features.
 *
 */
public enum CoreFeatures
{
    /**
     * Enabled when running in the 'On Demand' environment.
     *
     */
    ON_DEMAND;

    private final String featureKey;

    CoreFeatures()
    {
        featureKey = CoreFeatures.class.getName() + "." + toString();
    }

    public String featureKey()
    {
        return featureKey;
    }
}
