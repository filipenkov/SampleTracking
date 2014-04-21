package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.page.admin.plugins.AuditLogTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.DeveloperTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.InstallPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.JIRAUpgradeCheckPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.UpgradePlugins;

import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumPlugins extends AbstractSeleniumPage implements Plugins
{

    private final Map<Class<?>,PluginsTab> pluginTabMappings;

    public SeleniumPlugins(SeleniumContext ctx)
    {
        super(ctx);
        pluginTabMappings = MapBuilder.<Class<?>,PluginsTab>newBuilder()
            .add(ManageExistingPlugins.class, new SeleniumManageExistingPlugins(context,this))
                .add(UpgradePlugins.class, new SeleniumUpgradePlugins(context,this))
                .add(InstallPlugins.class, new SeleniumInstallPlugins(context,this))
                .add(JIRAUpgradeCheckPlugins.class, new SeleniumJIRAUpgradeCheckPlugins(context,this))
                .add(DeveloperTab.class, new SeleniumDeveloperTab(context,this))
                .add(AuditLogTab.class, new SeleniumAuditLogTab(context,this))
            .toMap();
    }

    @Override
    public Plugins enableSystemPlugin(String pluginKey)
    {
        return null;
    }

    @Override
    public Plugins disableSystemPlugin(String pluginKey)
    {
        return null;
    }

    @Override
    public <T extends PluginsTab<T>> T openTab(Class<T> tabClass)
    {
        return pluginTab(tabClass).open();
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public <T extends PluginsTab<T>> T pluginTab(Class<T> tabClass)
    {
        return (T) pluginTabMappings.get(tabClass);
    }

    @Override
    public Locator adminLinkLocator()
    {
        return id("upm-admin-link");
    }

    @Override
    protected SeleniumLocator detector()
    {
        return id("upm-title");
    }
}
