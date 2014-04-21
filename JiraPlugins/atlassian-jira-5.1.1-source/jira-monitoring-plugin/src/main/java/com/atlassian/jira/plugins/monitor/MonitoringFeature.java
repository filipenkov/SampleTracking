package com.atlassian.jira.plugins.monitor;

import com.atlassian.jira.config.FeatureEvent;
import com.atlassian.jira.config.FeatureManager;

import static com.atlassian.jira.config.CoreFeatures.ON_DEMAND;

/**
 * The "monitoring" feature.
 *
 * @since v5.1
 */
public class MonitoringFeature
{
    /**
     * The name of the monitoring feature.
     */
    private static final String NAME = "jira.monitoring";

    private final FeatureManager featureManager;

    public MonitoringFeature(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    /**
     * Returns this feature's name.
     *
     * @return a String containing this feature's name
     */
    public String name()
    {
        return NAME;
    }

    /**
     * Returns true if the monitoring feature is enabled. The feature is always enabled in JIRA behind-the-firewall and
     * disabled in JIRA OnDemand (unless the {@value #NAME} dark feature is switched on).
     *
     * @return a boolean indicating whether the monitoring feature is enabled
     */
    public boolean enabled()
    {
        return !featureManager.isEnabled(ON_DEMAND) || featureManager.isEnabled(NAME);
    }

    /**
     * Returns true if this feature is controlled by the given {@code FeatureEvent}.
     *
     * @param event a FeatureEvent
     * @return a boolean indicating whether this feature is affected by the given event
     */
    public boolean isControlledBy(FeatureEvent event)
    {
        return name().equals(event.feature());
    }
}
