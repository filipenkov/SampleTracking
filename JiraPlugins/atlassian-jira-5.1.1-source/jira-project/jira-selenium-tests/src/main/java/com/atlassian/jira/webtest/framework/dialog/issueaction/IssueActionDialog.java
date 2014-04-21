package com.atlassian.jira.webtest.framework.dialog.issueaction;

import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.model.IssueOperation;

/**
 * <p>
 * Represents an 'issue action' dialog accessible from View Issue and Issue Navigator.
 * Such dialog offers functionality to perform some actions over issue(s) in JIRA. It may be opened by various
 * ways from the mentioned pages (e.g. links, keyboard shortcuts etc.).
 *
 * <p>
 * Each dialog is associated with an {@link com.atlassian.jira.webtest.framework.model.IssueOperation} that describes
 * the particular action performed by the dialog.
 *
 * <P>
 * TODO on the #open() method
 *
 * @see com.atlassian.jira.webtest.framework.model.IssueOperation
 * @since v4.3
 */
public interface IssueActionDialog<T extends IssueActionDialog<T>> extends Dialog<T>, IssueAware
{
    /**
     * Associated issue action.
     *
     * @return issue action
     */
    IssueOperation action();

}
