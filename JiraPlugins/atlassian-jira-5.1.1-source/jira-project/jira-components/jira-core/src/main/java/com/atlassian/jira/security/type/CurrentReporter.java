/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

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

public class CurrentReporter extends SimpleIssueFieldSecurityType
{
    public static final String DESC = "reporter";
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporter(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.reporter");
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
        return DocumentConstants.ISSUE_AUTHOR;
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return true;
    }

    protected String getField()
    {
        return DESC;
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        Set result = new HashSet(1);
        if (ctx.getIssue() != null && ctx.getIssue().getReporter() != null) result.add(ctx.getIssue().getReporter());
        return result;
    }
}
