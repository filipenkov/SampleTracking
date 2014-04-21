package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginComponent;

/**
 * Factory of plugin components.
 *
 * @since v4.3
 */
public interface PluginComponentFactory<T extends PluginComponent<T>>
{
    T create(String pluginKey);
}
