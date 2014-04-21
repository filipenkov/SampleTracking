package com.atlassian.jira.config;

import static com.atlassian.jira.config.FeatureManager.SYSTEM_PROPERTY_PREFIX;

/**
 * Core manipulable JIRA features.
 */
public enum CoreFeatures
{
    /**
     * Enabled when running in the 'On Demand' environment.
     *
     */
    ON_DEMAND();

    private final String featureKey;

    /**
     * Determines if this feature is a 'Dev Feature' that can be turned on and off in a running
     * production instance for a single user without breaking anything.
     *
     * Other names "Dark Feature", "User-enabled Feature", "Runtime Feature".
     *
     * false by default!
     */
    private final boolean devFeature;

    CoreFeatures()
    {
        this(false);
    }

    CoreFeatures(boolean isDevFeature)
    {
        devFeature = isDevFeature;
        featureKey = CoreFeatures.class.getName() + "." + name();
    }

    public String featureKey()
    {
        return featureKey;
    }

    public boolean isDevFeature()
    {
        return devFeature;
    }

    /**
     * Returns true if the system property corresponding to this feature is set to <b>true</b>. The property name will
     * have the form <code>{@value FeatureManager#SYSTEM_PROPERTY_PREFIX}.com.atlassian.jira.config.CoreFeatures.FEATURE</code>.
     *
     * @return a boolean indicating whether this feature is enabled by system property
     */
    public boolean isSystemPropertyEnabled()
    {
        return Boolean.getBoolean(SYSTEM_PROPERTY_PREFIX + getClass().getName() + "." + name());
    }
}
