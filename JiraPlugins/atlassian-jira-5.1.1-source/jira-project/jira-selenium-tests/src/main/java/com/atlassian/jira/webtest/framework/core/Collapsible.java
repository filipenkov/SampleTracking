package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * An element of the page that may be hidden (collapsed) and made visible by user action (e.g. a click on a particular
 * button)
 *
 * @param <T> target type of this collapsible
 * @since v4.3
 */
public interface Collapsible<T extends PageObject> extends PageObject
{

    /**
     * Queries whether this page object is visible in the current test context.
     *
     * @return timed condition querying visibility of this page object
     */
    TimedCondition isExpanded();

    /**
     * Queries whether this page object is collapsed (hidden) in the current test context. Equivalent to
     * <code>Conditions.not(isVisible())</code>.
     *
     * @return timed condition verifying, whether this page object is currently collapsed
     * @see #isExpanded()
     * @see com.atlassian.jira.webtest.framework.core.condition.Conditions#not(com.atlassian.jira.webtest.framework.core.condition.TimedCondition) 
     */
    TimedCondition isCollapsed();


    /**
     * Restore this collapsible to a visible mode. After this call, subsequent calls to {@link #isExpanded()} condition
     * should eventually return <code>true</code>.
     *
     * @return this collapsible instance
     */
    T expand();

    /**
     * Collapse this collapsible to a visible mode. After this call, subsequent calls to {@link #isCollapsed()} condition
     * should eventually return <code>true</code>.
     *
     * @return this collapsible instance
     */
    T collapse();

}
