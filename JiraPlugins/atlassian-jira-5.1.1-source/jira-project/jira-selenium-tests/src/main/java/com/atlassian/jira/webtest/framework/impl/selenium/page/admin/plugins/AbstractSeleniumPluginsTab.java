package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPageSection;
import com.atlassian.jira.webtest.framework.page.PageSection;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsList;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsTab;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPluginsTab<T extends PluginsTab<T>> extends AbstractSeleniumPageSection<Plugins> implements PluginsTab<T>
{
    protected final String tabId;
    protected final Locator openLinkLocator;
    protected final SeleniumLocator openTabLocator;

    protected AbstractSeleniumPluginsTab(SeleniumContext context, Plugins plugins, String tabId, String linkId)
    {
        super(plugins, context);
        this.tabId = tabId;
        openTabLocator = css("#"+this.tabId+".upm-selected");
        openLinkLocator = id(linkId);
    }

    @Override
    public TimedCondition isOpen()
    {
        return isReady();
    }

    @Override
    public TimedCondition isClosed()
    {
        return Conditions.not(isOpen());
    }

    @Override
    public TimedCondition isOpenable()
    {
        return Conditions.and(page().isAt(),isClosed());
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public T open()
    {
        openLinkLocator.element().click();
        return (T) this;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return openTabLocator;
    }
}