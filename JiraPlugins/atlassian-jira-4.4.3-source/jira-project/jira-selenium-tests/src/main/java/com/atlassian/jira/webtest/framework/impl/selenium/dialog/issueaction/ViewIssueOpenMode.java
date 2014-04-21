package com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction;

import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Open mode for {@link com.atlassian.jira.webtest.framework.impl.selenium.page.issue.SeleniumViewIssue}.
 *
 * @since v4.3
 */
public class ViewIssueOpenMode<D extends IssueActionDialog<D>> extends AbstractDialogOpenMode<D>
        implements ViewIssue.ViewIssueDialogOpenMode<D>
{
    private final ViewIssue viewIssue;

    public ViewIssueOpenMode(ViewIssue vi, D dialog, SeleniumContext context)
    {
        super(dialog, vi.dotDialog(), context);
        this.viewIssue = notNull("viewIssue", vi);
    }

    @Override
    public D byMenu()
    {
        viewIssue.menu().invoke(action);
        return dialog;
    }
}
