package com.atlassian.jira.webtest.framework.impl.webdriver.core;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.PageObjectFactory;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.impl.webdriver.locator.DefaultWebDriverLocator;
import com.atlassian.jira.webtest.framework.page.GlobalPage;

/**
 * WebDriver implementation of {@link com.atlassian.jira.webtest.framework.core.PageObjectFactory}.
 *
 * @since v4.2
 */
public class WebDriverPageObjectFactory extends WebDriverContextAware implements PageObjectFactory
{
    public WebDriverPageObjectFactory(final WebDriverContext context)
    {
        super(context);
    }

    public <T extends GlobalPage> T createGlobalPage(Class<T> pageType)
    {
        // TODO
        return null;
    }

    public Locator createLocator(LocatorType type, String value)
    {
        return new DefaultWebDriverLocator(type, value, context);
    }

    @Override
    public <P extends PageObject> P createPageObject(Class<P> type)
    {
        throw new UnsupportedOperationException("TODO");
    }
}
