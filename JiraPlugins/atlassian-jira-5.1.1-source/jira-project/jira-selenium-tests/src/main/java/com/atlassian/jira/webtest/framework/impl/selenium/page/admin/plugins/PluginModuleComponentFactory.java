package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginModuleComponent;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface PluginModuleComponentFactory<T extends PluginModuleComponent<T>>
{
    T create(String moduleKey);
}
