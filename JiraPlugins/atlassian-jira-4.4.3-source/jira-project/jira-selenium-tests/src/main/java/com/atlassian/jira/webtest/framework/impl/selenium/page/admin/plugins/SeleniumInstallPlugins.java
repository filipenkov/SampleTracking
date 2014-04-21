package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.admin.plugins.InstallPluginDialog;
import com.atlassian.jira.webtest.framework.page.admin.plugins.InstallPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.UpgradePlugins;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumInstallPlugins extends AbstractSeleniumPluginsTab<InstallPlugins> implements InstallPlugins
{
    protected SeleniumInstallPlugins(SeleniumContext context, Plugins plugins)
    {
        super(context, plugins, "upm-panel-install", "upm-tab-install");
    }

    @Override
    public InstallPluginDialog openInstallPluginDialog()
    {
        return null;
    }

    @Override
    public InstallPluginDialog installPluginDialog()
    {
        return null;
    }
}