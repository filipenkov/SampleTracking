package com.atlassian.jira.webtest.framework.dialog.issueaction;

import com.atlassian.jira.webtest.framework.dialog.SubmittableDialog;
import com.atlassian.jira.webtest.framework.form.issueaction.LinkIssueForm;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

/**
 * Represents the link issue dialog.
 *
 * @since v4.3
 */
public interface LinkIssueDialog extends IssueActionDialog<LinkIssueDialog>,
        SubmittableDialog<LinkIssueDialog, IssueActionsParent>, LinkIssueForm
{
}
