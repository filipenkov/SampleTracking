package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.opensymphony.user.User;

/**
 * Checks if there are any history issue's
 */
public class UserHasIssueHistoryCondition extends AbstractJiraCondition
{
    final private UserIssueHistoryManager userHistoryManager;

    public UserHasIssueHistoryCondition(UserIssueHistoryManager userHistoryManager)
    {
        this.userHistoryManager = userHistoryManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return userHistoryManager.hasIssueHistory(user);
    }
}
