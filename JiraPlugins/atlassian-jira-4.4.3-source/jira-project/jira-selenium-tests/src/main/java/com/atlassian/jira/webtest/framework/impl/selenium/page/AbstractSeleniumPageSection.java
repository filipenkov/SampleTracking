package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.Page;
import com.atlassian.jira.webtest.framework.page.PageSection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract Selenium implementation of the {@link com.atlassian.jira.webtest.framework.page.PageSection} interface.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPageSection<P extends Page> extends AbstractLocatorBasedPageObject implements PageSection<P>
{
    private final P page;

    protected AbstractSeleniumPageSection( P page, SeleniumContext context)
    {
        super(context);
        this.page = notNull("page", page);
    }

    @Override
    public final P page()
    {
        return page;
    }

    @Override
    public P parent()
    {
        return page;
    }
}
