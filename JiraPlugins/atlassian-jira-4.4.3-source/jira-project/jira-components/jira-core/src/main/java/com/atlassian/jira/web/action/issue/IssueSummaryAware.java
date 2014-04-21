package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.component.issuesummary.IssueSummaryLayoutBean;
import com.opensymphony.user.User;

/**
 * If an action wishes to use the 'issuesummary' decorator, then it must implement IssueSummaryAware
 */
public interface IssueSummaryAware
{
    /**
     * @return  The issue to display on the left hand side
     * @throws IssuePermissionException     If the remote user does not have the permission to view this issue
     */
    public Issue getSummaryIssue() throws IssuePermissionException;

    public User getRemoteUser();

    public IssueSummaryLayoutBean getLayoutBean();
}
