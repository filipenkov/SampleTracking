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
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsList;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsList}
 * interface.
 *
 * @since v4.3
 */
public class SeleniumPluginsList<T extends PluginComponent<T>> extends AbstractLocatorBasedPageObject implements PluginsList<T>
{
    private final SeleniumLocator mainLocator;
    private final PluginComponentFactory<T> componentFactory;

    protected SeleniumPluginsList(SeleniumContext context,String mainId, PluginComponentFactory<T> componentFactory)
    {
        super(context);
        this.componentFactory = componentFactory;
        mainLocator = id(mainId);
    }

    @Override
    public TimedCondition hasPluginComponent(String pluginKey)
    {
        return componentFactory.create(pluginKey).locator().element().isPresent();
    }

    @Override
    public TimedCondition hasPluginComponentMatching(String pluginKeyRegex)
    {
        return null;
    }

    @Override
    public TimedQuery<T> findPluginComponent(String pluginKey)
    {
        T answer = componentFactory.create(pluginKey);
        return Queries.conditionalQuery(answer, answer.locator().element().isPresent()).defaultTimeout(timeouts.timeoutFor(Timeouts.AJAX_ACTION))
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    @Override
    protected SeleniumLocator detector()
    {
        return mainLocator;
    }

    @Override
    public Locator locator()
    {
        return mainLocator;
    }
}
