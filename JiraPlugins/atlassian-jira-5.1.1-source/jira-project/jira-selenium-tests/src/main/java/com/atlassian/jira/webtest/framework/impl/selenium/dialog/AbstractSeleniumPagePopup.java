package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.PageDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.dialog.PageDialog} for
 * popups.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPagePopup<D extends PageDialog<D,P>, P extends Page>
        extends AbstractSeleniumPopup<D> implements PageDialog<D,P>
{
    private final P page;

    protected AbstractSeleniumPagePopup(P page, SeleniumLocator openLinkLocator, SeleniumContext ctx,
            String dialogId, String windowId)
    {
        super(ctx, dialogId, windowId, openLinkLocator);
        this.page = notNull("page", page);
    }

    @Override
    public P page()
    {
        return page;
    }

    @Override
    protected TimedCondition isOpenableInContext()
    {
        return page.isAt();
    }
}
