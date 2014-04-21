package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.PageDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.dialog.PageDialog}.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumAuiPageDialog<D extends PageDialog<D,P>, P extends Page>
        extends AbstractSeleniumAuiDialog<D> implements PageDialog<D,P>
{
    private final P page;

    protected AbstractSeleniumAuiPageDialog(P page, SeleniumContext ctx, String dialogId)
    {
        super(ctx, dialogId);
        this.page = notNull("page", page);
    }

    @Override
    public P page()
    {
        return page;
    }

    /**
     * Timed condition checking if this dialog is openable in the current test context
     *
     * @return timed condition 'is openable in the current context?'
     */
    @Override
    protected TimedCondition isOpenableInContext()
    {
        return page().isReady();
    }
}
