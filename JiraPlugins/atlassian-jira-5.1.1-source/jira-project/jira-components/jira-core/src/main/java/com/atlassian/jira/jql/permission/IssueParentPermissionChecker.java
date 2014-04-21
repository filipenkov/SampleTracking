package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;

/**
 * Checks to see if subtasks are enabled or disabled to determine if the issue parent handler can be seen.
 *
 * @since v4.0
 */
public class IssueParentPermissionChecker implements ClausePermissionChecker
{
    private final SubTaskManager subTaskManager;

    public IssueParentPermissionChecker(final SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return subTaskManager.isSubTasksEnabled();
    }
}
