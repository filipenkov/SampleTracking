package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginModuleComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginModulesList;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsList;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumPluginModulesList<T extends PluginModuleComponent<T>> extends AbstractLocatorBasedPageObject implements PluginModulesList<T>
{
    private final Locator mainLocator;
    private final PluginModuleComponentFactory<T> componentFactory;

    protected SeleniumPluginModulesList(SeleniumContext context,Locator pluginModuleLocator, PluginModuleComponentFactory<T> componentFactory)
    {
        super(context);
        this.componentFactory = componentFactory;
        mainLocator = pluginModuleLocator.combine(css(".upm-plugin-modules"));
    }

    @Override
    public TimedCondition hasPluginModuleComponent(String moduleKey)
    {

        return componentFactory.create(moduleKey).locator().element().isPresent();
    }


    @Override
    public TimedQuery<T> findPluginModuleComponent(String moduleKey)
    {
        T answer = componentFactory.create(moduleKey);
        return Queries.conditionalQuery(answer, answer.locator().element().isPresent()).defaultTimeout(timeouts.timeoutFor(Timeouts.COMPONENT_LOAD))
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    @Override
    protected SeleniumLocator detector()
    {
        return (SeleniumLocator) mainLocator;
    }

    @Override
    public Locator locator()
    {
        return mainLocator;
    }
}