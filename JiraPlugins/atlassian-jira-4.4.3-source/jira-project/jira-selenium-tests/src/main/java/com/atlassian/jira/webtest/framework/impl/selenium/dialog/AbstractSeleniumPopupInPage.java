package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Popup based on a page but not implementing page popup interface.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPopupInPage<D extends Dialog<D>> extends AbstractSeleniumPopup<D>
{
    private final Page page;

    protected AbstractSeleniumPopupInPage(Page page, SeleniumLocator openLinkLocator, SeleniumContext ctx,
            String dialogId, String windowId)
    {
        super(ctx, dialogId, windowId, openLinkLocator);
        this.page = notNull("page", page);
    }

    @Override
    protected TimedCondition isOpenableInContext()
    {
        return page.isAt();
    }
}
