package com.atlassian.jira.config;

/**
 * Component responsible for providing information whether certain features in JIRA are enabled or disabled.
 *
 * @since v4.4
 */
public interface FeatureManager
{

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
}
