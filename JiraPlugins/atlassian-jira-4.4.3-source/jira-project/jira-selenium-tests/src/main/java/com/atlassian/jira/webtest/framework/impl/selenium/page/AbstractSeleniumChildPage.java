package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.ChildPage;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract implementation of {@link com.atlassian.jira.webtest.framework.page.ChildPage}.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumChildPage<P extends Page> extends AbstractSeleniumPage implements ChildPage<P>
{
    protected final P parentPage;

    protected AbstractSeleniumChildPage(P parentPage, SeleniumContext ctx)
    {
        super(ctx);
        this.parentPage = notNull("parentPage", parentPage);
    }

    /**
     * Locator of a clickable component that will take the test back to the parent page.
     *
     * @return back component locator
     */
    protected abstract Locator backLocator();
    

    @Override
    public final P back()
    {
        backLocator().element().click();
        waitFor().pageLoad();
        return parentPage;
    }
}
