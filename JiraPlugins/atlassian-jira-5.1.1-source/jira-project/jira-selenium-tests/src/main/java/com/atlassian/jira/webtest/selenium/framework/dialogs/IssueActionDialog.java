package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Represents 'issue action' dialog accessible from View Issue and Issue Navigator.
 * An 'issue action' dialog is a dialog offering functionality to perform some actions
 * over issue(s) in JIRA. It may be accessed either from the View Issue page, or
 * from the Issue Navigator.
 *
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog} instead
 */
@Deprecated
public interface IssueActionDialog extends Dialog
{
    // TODO this sucks, there are different methods of opening dialogs from view issue / issue navigator and we have to
    // TODO account for them in the API!!!

    /**
     * Open the dialog from View Issue.
     *
     * @return this dialog instance
     */
    IssueActionDialog openFromViewIssue();

    /**
     * Open the dialog from the Issue Navigator for given <tt>issueId</tt>.
     *
     * @param issueId issue ID
     * @return this dialog instance
     */
    IssueActionDialog openFromIssueNav(int issueId);

    /**
     * Returns issue operation associated with this particular dialog.
     *
     * @return issue operation performed by this dialog
     */
    LegacyIssueOperation issueOperation();


    // TODO get rid of it in favour of #issueOperation()
    /**
     * Gets name of the dialog in the Actions Dialog list.
     *
     * @return name of the action performed by this dialog
     * @deprecated use data in this dialog's {@link com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation} instead
     */
    @Deprecated
    String actionName();

}
