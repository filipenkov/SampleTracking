package com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction;

import com.atlassian.jira.webtest.framework.component.CommentInput;
import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.dialog.issueaction.LinkIssueDialog;
import com.atlassian.jira.webtest.framework.form.issueaction.LinkIssueForm;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.form.issueaction.SeleniumLinkIssueForm;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.model.DefaultIssueActions;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.dialog.issueaction.LinkIssueDialog}.
 *
 * @since v4.3
 */
public class SeleniumLinkIssueDialog extends AbstractIssueActionDialog<LinkIssueDialog> implements LinkIssueDialog
{
    private static final String DIALOG_ID = "link-issue-dialog";

    private final LinkIssueForm form;
    private final SeleniumLocator submitButtonLocator;

    public SeleniumLinkIssueDialog(IssueActionsParent parent, IssueActionDialogOpener opener, SeleniumContext context)
    {
        super(parent, context, DIALOG_ID, opener, LinkIssueDialog.class);
        this.form = new SeleniumLinkIssueForm(this, context());
        this.submitButtonLocator = locatorFor(locator().combine(id("issue-link-submit")));
    }

    @Override
    public IssueOperation action()
    {
        return DefaultIssueActions.LINK_ISSUE;
    }

    @Override
    public Select linkTypeSelect()
    {
        return form.linkTypeSelect();
    }

    @Override
    public IssuePicker issuePicker()
    {
        return form.issuePicker();
    }

    @Override
    public CommentInput comment()
    {
        return form.comment();
    }


    @Override
    public IssueActionsParent submit()
    {
        if (isOpen().byDefaultTimeout())
        {
            submitButtonLocator.element().click();
            return parent;
        }
        throw new IllegalStateException("Not open");
    }

    @Override
    public IssueActionsParent cancel()
    {
        if (isOpen().byDefaultTimeout())
        {
            cancelLinkLocator().element().click();
            return parent;
        }
        throw new IllegalStateException("Not open");
    }
}
