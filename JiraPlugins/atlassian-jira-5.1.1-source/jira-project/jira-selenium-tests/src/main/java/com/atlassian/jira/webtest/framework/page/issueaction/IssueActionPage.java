package com.atlassian.jira.webtest.framework.page.issueaction;

import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.ChildPage;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

/**
 * A 'satellite' page around View Issue and Issue Navigator that serves to perform a particular issue operation.
 * Those are mainly opened as dialogs, but may still be opened as standalone pages if necessary.
 *
 * @since v4.3
 */
public interface IssueActionPage extends ChildPage<IssueActionsParent>
{
    /**
     * Issue operation associated with this page.
     *
     * @return issue operation
     */
    IssueOperation issueOperation();
}
