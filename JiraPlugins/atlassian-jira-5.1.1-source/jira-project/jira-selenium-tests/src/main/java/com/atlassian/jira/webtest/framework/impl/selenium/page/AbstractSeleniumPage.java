package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.Page;

/**
 * <p>
 * Abstract implementation of the {@link com.atlassian.jira.webtest.selenium.framework.pages.Page}
 * interface, in terms of the main page locator.
 *
 * @see com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject
 * @since v4.2
 */
public abstract class AbstractSeleniumPage extends AbstractLocatorBasedPageObject implements Page
{
    protected AbstractSeleniumPage(SeleniumContext context)
    {
        super(context);
    }

    /**
     * {@inheritDoc}
     *
     * This method assmues eqivalence between {@link #isReady()} and being at the page. This behaviour may
     * be refined/overridden by subclasses.
     *
     * @return {@inheritDoc}
     */
    public TimedCondition isAt()
    {
        return isReady();
    }

}
