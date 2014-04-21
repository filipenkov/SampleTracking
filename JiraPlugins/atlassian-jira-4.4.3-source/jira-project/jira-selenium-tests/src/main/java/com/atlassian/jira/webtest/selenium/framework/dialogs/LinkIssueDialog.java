package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.IssuePicker;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Representation of the link issue dialog. 
 *
 * @since v4.2
 */
public final class LinkIssueDialog extends AbstractIssueDialog<LinkIssueDialog>
{
    private final IssuePicker issuePicker;

    public LinkIssueDialog(SeleniumContext ctx)
    {
        super(LegacyIssueOperation.LINK_ISSUE, LinkIssueDialog.class, ActionType.NEW_PAGE, ctx);
        this.issuePicker = new IssuePicker(VISIBLE_DIALOG_CONTENT_SELECTOR, ctx);
    }

    @Override
    protected String dialogContentsReadyLocator()
    {
        return issuePicker.inputAreaLocator();
    }

    // TODO move contents of the dialog to a LinkIssue page object

    public IssuePicker issuePicker()
    {
        return issuePicker;
    }
}
