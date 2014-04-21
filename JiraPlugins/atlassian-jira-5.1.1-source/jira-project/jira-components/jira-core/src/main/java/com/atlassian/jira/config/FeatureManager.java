package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.DarkFeatures;

import java.util.Set;

/**
 * Component responsible for providing information whether certain features in JIRA are enabled or disabled.
 *
 * @since v4.4
 */
public interface FeatureManager
{
    /**
     * The prefix used for enabling dark features from the command line. For example, one might use
     * <code>-Datlassian.darkfeature.com.atlassian.jira.config.FAST_TABS=true</code> as a JVM argument in order to
     * enable fast tabs.
     */
    String SYSTEM_PROPERTY_PREFIX = "atlassian.darkfeature.";

    /**
     * Checks whether a feature with given <tt>featureKey</tt> is enabled in the running JIRA instance.
     *
     * @param featureKey feature key
     * @return <code>true</code>, if feature identified by <tt>featureKey</tt> is enabled, <code>false</code> otherwise
     */
    boolean isEnabled(String featureKey);


    /**
     * Checks whether a feature with given <tt>coreFeature</tt> is enabled in the running JIRA instance.
     *
     * @param coreFeature core feature instance
     * @return <code>true</code>, if given core feature is enabled, <code>false</code> otherwise
     */
    boolean isEnabled(CoreFeatures coreFeature);

    /**
     * Returns a set containing the feature keys of all features that are currently enabled.
     *
     * @return a set containing the feature keys of all features that are currently enabled
     * @since v5.0
     */
    Set<String> getEnabledFeatureKeys();

    /**
     * Creates {@link com.atlassian.jira.plugin.profile.DarkFeatures} instances from the PropertySet associated with the ThreadLocal user.
     *
     * @return Returns the Dark Features state for the current user.
     */
    DarkFeatures getDarkFeatures();

    /**
     * Enables a feature for a particular User. Raises a {@link FeatureEnabledEvent}.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureEnabledEvent} if it is successful.
     *
     * @param user the user to enable the feature for
     * @param feature the feature to enable
     */
    void enableUserDarkFeature(User user, String feature);

    /**
     * Disables a feature for a particular user.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureDisabledEvent} if it is successful.
     *
     * @param user the user to disable the feature for
     * @param feature the feature to disable
     */
    void disableUserDarkFeature(User user, String feature);

    /**
     * Enables a site-wide feature.
     * <p/>
     * Since JIRA 5.1, this method raises a {@link FeatureEnabledEvent} if it is successful.
     *
     * @param feature the feature to enable
     */
    void enableSiteDarkFeature(String feature);

    /**
     * Disables a site-wide feature.
     *
     * Since JIRA 5.1, this method raises a {@link FeatureDisabledEvent} if it is successful.
     *
     * @param feature the feature to disable
     */
    void disableSiteDarkFeature(String feature);
}
