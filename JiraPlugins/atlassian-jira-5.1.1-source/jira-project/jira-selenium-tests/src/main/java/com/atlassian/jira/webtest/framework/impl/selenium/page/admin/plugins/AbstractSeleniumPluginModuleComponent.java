package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginModuleComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginModuleComponent;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class AbstractSeleniumPluginModuleComponent<T extends PluginModuleComponent<T>> extends AbstractLocatorBasedPageObject
        implements PluginModuleComponent<T>
{
    protected final String pluginKey;
    protected final String moduleKey;
    protected final Locator listLocator;
    protected final Locator mainLocator;
    private final Class<T> targetType;

    protected AbstractSeleniumPluginModuleComponent(SeleniumContext context, Locator listLocator, String pluginKey, String moduleKey, Class<T> managePluginModuleComponentClass)
    {
        super(context);
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.listLocator = listLocator;
        mainLocator = listLocator.combine(jQuery(".upm-module:contains("+moduleKey+")"));
        this.targetType = managePluginModuleComponentClass;
    }

    @Override
    public String getCompletePluginModuleKey()
    {
        return pluginKey+":"+moduleKey;
    }

    @Override
    public Locator locator()
    {
        return mainLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return (SeleniumLocator) mainLocator;
    }
}
