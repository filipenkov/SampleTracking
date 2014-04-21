package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * Represents 'Manage Existing plugins' tab of the UPM page.
 *
 * @since v4.3
 */
public interface ManageExistingPlugins extends PluginsTab<ManageExistingPlugins>
{

    public ManageExistingPlugins showSystemPlugins();

    public ManageExistingPlugins hideSystemPlugins();

    public TimedCondition isSystemPluginsVisible();


    public PluginsList<ManagePluginComponent> userPlugins();

    public PluginsList<ManagePluginComponent> systemPlugins();
}
