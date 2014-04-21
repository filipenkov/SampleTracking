package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.opensymphony.user.User;

/**
 * Checks if this user is logged in
 */
public class UserLoggedInCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return user != null;
    }
}
