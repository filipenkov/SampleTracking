/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class CurrentAssigneeHasAssignablePermission extends SimpleIssueFieldSecurityType
{
    private JiraAuthenticationContext authenticationContext;

    public CurrentAssigneeHasAssignablePermission(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.authenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.permission.types.current.assignee.has.assignable.perm");
    }

    public String getType()
    {
        return "assigneeassignable";
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
     * Is valid for all permissions except "Assignable".
     * <p/>
     * Because we rely on the permissions for the "Assignable" function, then not only does it not make
     * sense to add this role to "Assignable", it would actually cause an infinite loop. see JRA-13315
     * </p>
     *
     * @param permissionId permission id.
     * @return false for Permissions.ASSIGNABLE_USER, true otherwise.
     */
    public boolean isValidForPermission(int permissionId)
    {
        return permissionId != Permissions.ASSIGNABLE_USER;
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user, issueCreation);
    }

    PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }

    protected String getField()
    {
        return "assignee";
    }
}
