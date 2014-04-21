package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.Page;
import com.atlassian.jira.webtest.framework.page.SubmittableChildPage;

/**
 * Abstract implementation of the {@link com.atlassian.jira.webtest.framework.page.SubmittableChildPage} in the
 * Selenium World&trade;.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumSubmittableChildPage<P extends Page> extends AbstractSeleniumChildPage<P>
        implements SubmittableChildPage<P>
{
    protected AbstractSeleniumSubmittableChildPage(P parentPage, SeleniumContext ctx)
    {
        super(parentPage, ctx);
    }

    /**
     * A clickable component locator that will submit this page
     *
     * @return locator of the submit component of this page
     */
    protected abstract SeleniumLocator submitLocator();

    @Override
    public final P submit()
    {
        submitLocator().element().click();
        waitFor().pageLoad();
        return parentPage;
    }
}
