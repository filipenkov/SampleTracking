package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.UpgradePlugins;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumUpgradePlugins extends AbstractSeleniumPluginsTab<UpgradePlugins> implements UpgradePlugins
{
    protected SeleniumUpgradePlugins(SeleniumContext context, Plugins plugins)
    {
        super(context, plugins, "upm-panel-upgrade", "upm-tab-upgrade");
    }
}
