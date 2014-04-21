package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumClient;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.Futures;

import java.util.concurrent.Callable;

/**
 * Represents the new quick edit dialog.
 *
 * @since v5.0
 */
public class QuickEditDialog
{
    private static final int WAIT_TIME = 5000;
    private final SeleniumContext ctx;
    private Callable<Boolean> issueNav;

    public QuickEditDialog(SeleniumContext ctx)
    {
        this(ctx, Callables.returning(false));
    }

    public QuickEditDialog(SeleniumContext ctx, Callable<Boolean> isIssueNav)
    {
        this.ctx = ctx;
        issueNav = isIssueNav;
    }

    public QuickEditDialog waitUntilOpen()
    {
        ctx.assertions().visibleByTimeout("id=edit-issue-dialog", WAIT_TIME);
        ctx.assertions().visibleByTimeout("css=.qf-unconfigurable-form", WAIT_TIME);
        return this;
    }

    public QuickEditDialog toggleFullEditForm()
    {
        ctx.assertions().visibleByTimeout("id=edit-issue-dialog", WAIT_TIME);
        ctx.client().click("id=qf-field-picker-trigger");
        ctx.assertions().visibleByTimeout("css=.qf-picker-header", WAIT_TIME);
        ctx.client().click("css=.qf-picker-header .qf-unconfigurable");
        ctx.assertions().visibleByTimeout("css=.qf-container form.aui", WAIT_TIME);
        return this;
    }


    public void setFieldValue(String id, String value)
    {
        ctx.client().type(id, value);
    }

    public QuickEditDialog submit()
    {
        ctx.client().click("edit-issue-submit");
        waitForContentUpdate();
        return this;
    }

    public boolean isOpen()
    {
        return ctx.client().isVisible("id=edit-issue-dialog");
    }

    private void waitForContentUpdate()
    {
        final SeleniumClient client = ctx.client();
        final boolean kickass = client.getEval("with(selenium.browserbot.getCurrentWindow()){JIRA.Issues && JIRA.Issues.InlineEdit}") != null;
        try
        {
            if (!issueNav.call() && kickass) {
                client.waitForAjaxWithJquery(WAIT_TIME);
            } else {
                client.waitForPageToLoad(WAIT_TIME);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
