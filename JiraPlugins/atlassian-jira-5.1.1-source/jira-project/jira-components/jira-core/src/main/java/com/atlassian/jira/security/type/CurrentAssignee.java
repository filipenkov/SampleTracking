package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CurrentAssignee extends SimpleIssueFieldSecurityType
{
    public static final String DESC = "assignee";

    private JiraAuthenticationContext authenticationContext;

    public CurrentAssignee(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.permission.types.current.assignee");
    }

    public String getType()
    {
        return DESC;
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    protected String getFieldName()
    {
        return DocumentConstants.ISSUE_ASSIGNEE;
    }

    /**
     * This should return two different values depending on where it is called from. If we are creating an Issue we want
     * to return FALSE but otherwise TRUE
     */
    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project)
    {
        return !issueCreation;
    }

    protected String getField()
    {
        return DESC;
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        Set<User> result = new HashSet<User>(1);
        if (ctx.getIssue() != null && ctx.getIssue().getAssignee() != null)
        {
            result.add(ctx.getIssue().getAssignee());
        }
        return result;
    }
}
