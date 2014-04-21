package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Condition to checkif the current user is the assignee of the current issue
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class IsIssueAssignedToCurrentUserCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(IsIssueAssignedToCurrentUserCondition.class);

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {

        final String assigneeId = issue.getAssigneeId();
        return StringUtils.isNotBlank(assigneeId) && user != null &&  assigneeId.equals(user.getName());

    }

}