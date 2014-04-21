/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class CurrentReporterHasCreatePermission extends SimpleIssueFieldSecurityType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporterHasCreatePermission(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.current.reporter.has.create.perm");
    }

    public String getType()
    {
        return "reportercreate";
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    /**
     * Is valid for all permissions except "Create Issue".
     * <p/>
     * Because we rely on the permissions for the "Create Issue" function, then not only does it not make sense to add
     * this role to "Create Issue", it would actually cause an infinite loop. see JRA-13315
     * </p>
     *
     * @param permissionId permission id.
     * @return false for Permissions.CREATE_ISSUE, true otherwise.
     */
    public boolean isValidForPermission(int permissionId)
    {
        return permissionId != Permissions.CREATE_ISSUE;
    }

    protected String getFieldName()
    {
        return DocumentConstants.ISSUE_AUTHOR;
    }

    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue gvProject)
    {
        return getPermissionManager().hasPermission(Permissions.CREATE_ISSUE, gvProject, user, issueCreation);
    }

    PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }


    protected String getField()
    {
        return "reporter";
    }
}
