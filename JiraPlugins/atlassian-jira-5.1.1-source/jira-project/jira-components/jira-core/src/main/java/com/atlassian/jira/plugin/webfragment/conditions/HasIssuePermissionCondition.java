package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Condition to check a permission against a given issue for the current user.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class HasIssuePermissionCondition extends AbstractJiraPermissionCondition
{
    private static final Logger log = Logger.getLogger(HasIssuePermissionCondition.class);


    public HasIssuePermissionCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();

        final Issue issue = (Issue) params.get("issue");

        if (issue == null)
        {
            log.warn("Trying to run permission condition on an issue, but no issue exists");
            return false;
        }

        return permissionManager.hasPermission(permission, issue, user);
    }

}
