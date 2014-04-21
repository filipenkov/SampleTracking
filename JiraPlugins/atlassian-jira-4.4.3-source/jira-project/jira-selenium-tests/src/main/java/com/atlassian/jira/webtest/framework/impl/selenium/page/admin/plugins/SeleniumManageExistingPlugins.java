package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsList;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumManageExistingPlugins extends AbstractSeleniumPluginsTab<ManageExistingPlugins> implements ManageExistingPlugins
{
    private final Locator showSystemPluginsLocator;
    private final Locator hideSystemPluginsLocator;
    private final PluginsList<ManagePluginComponent> userPlugins;
    private final PluginsList<ManagePluginComponent> systemPlugins;


    public SeleniumManageExistingPlugins(SeleniumContext context, Plugins plugins)
    {
        super(context, plugins, "upm-panel-manage", "upm-tab-manage");
        this.systemPlugins = new SeleniumPluginsList<ManagePluginComponent> (context,"upm-system-plugins",new ManagePluginsComponentFactory());
        this.userPlugins = new SeleniumPluginsList<ManagePluginComponent> (context,"upm-user-plugins",new ManagePluginsComponentFactory());
        this.showSystemPluginsLocator = id("upm-manage-show-system").withDefaultTimeout(Timeouts.AJAX_ACTION);
        this.hideSystemPluginsLocator = id("upm-manage-hide-system").withDefaultTimeout(Timeouts.AJAX_ACTION);
    }

    @Override
    public ManageExistingPlugins showSystemPlugins()
    {
        showSystemPluginsLocator.element().click();
        return this;
    }

    @Override
    public ManageExistingPlugins hideSystemPlugins()
    {
        hideSystemPluginsLocator.element().click();
        return this;
    }

    @Override
    public TimedCondition isSystemPluginsVisible()
    {
        return hideSystemPluginsLocator.element().isVisible();
    }

    @Override
    public PluginsList<ManagePluginComponent> userPlugins()
    {
        return this.userPlugins;
    }

    @Override
    public PluginsList<ManagePluginComponent> systemPlugins()
    {
        return this.systemPlugins;
    }

    private class ManagePluginsComponentFactory implements PluginComponentFactory<ManagePluginComponent>
    {

        @Override
        public ManagePluginComponent create( String pluginKey)
        {
            return new SeleniumManagePluginComponent(context,pluginKey);
        }
    }
}
