package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * Generic representation of plugins list in the UPM UI.
 *
 * @since v4.3
 */
public interface PluginsList<T extends PluginComponent<T>> extends PageObject, Localizable
{
    /**
     * Check if this list has component with given <tt>pluginKey</tt>.
     *
     * @param pluginKey plugin key to check
     * @return timed condition checking if a component with given key exists in this list
     */
    public TimedCondition hasPluginComponent(String pluginKey);

    /**
     * Check if this list has at least one component matching <tt>pluginKeyRegex</tt>.
     *
     * @param pluginKeyRegex regular expression of the plugin key to check
     * @return timed condition checking if a component with key matching given expression exists in this list
     */
    public TimedCondition hasPluginComponentMatching(String pluginKeyRegex);

    /**
     * Retrieve component with given <tt>pluginKey</tt>.
     *
     * @param pluginKey plugin key
     * @return query for component with given plugin key in this list
     */
    public TimedQuery<T> findPluginComponent(String pluginKey);
}
