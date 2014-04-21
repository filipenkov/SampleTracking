package com.atlassian.jira.webtest.selenium.framework.pages;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;

/**
 * <p>
 * Abstract implementation of the {@link com.atlassian.jira.webtest.selenium.framework.pages.Page}
 * interface.
 *
 * <p>
 * Implemented methods are templates defined in terms of an abstract method {@link #detector()},
 *
 * @since v4.2
 */
public abstract class AbstractPage extends AbstractSeleniumPageObject implements Page
{
    protected AbstractPage(SeleniumContext ctx)
    {
        super(ctx);
    }


    public final boolean isAt()
    {
        return client.isElementPresent(detector());
    }


    public final void assertReady(final long timeout)
    {
        assertThat.elementPresentByTimeout(detector(), timeout);
    }

    /**
     * Locator used to check if we are currently at the page
     *
     * @return locator indicative of the page
     */
    protected abstract String detector();

    protected final void checkOnPage()
    {
        if (!isAt())
        {
            throw new IllegalStateException("Not at this page. IsAt() check failed: " + isAt());
        }
    }


}
