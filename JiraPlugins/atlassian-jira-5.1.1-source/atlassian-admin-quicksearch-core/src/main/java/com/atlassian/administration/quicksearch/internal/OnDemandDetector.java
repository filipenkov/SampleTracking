package com.atlassian.administration.quicksearch.internal;

/**
 * Detects if app is running in OnDemand mode.
 *
 * @since 1.0
 */
public interface OnDemandDetector
{

    /**
     * Checks whether currently running in an OnDemand environment, where Atlassian apps are running integrated
     * together. Note that OnDemand instances can run in standalone mode (e.g. JIRA standalone) and this
     * method should return <code>false</code> in such case.
     *
     * @return <code>true</code>, it this is OnDemand integrated environment.
     */
    boolean isOnDemandMode();


    /**
     * Get OnDemand component. Throws exception if not in OnDemand mode.
     *
     * @param componentClass name of the component class
     * @return component instance
     * @throws IllegalStateException if not in OnDemand mode
     */
    Object getOnDemandComponent(String componentClass);
}
