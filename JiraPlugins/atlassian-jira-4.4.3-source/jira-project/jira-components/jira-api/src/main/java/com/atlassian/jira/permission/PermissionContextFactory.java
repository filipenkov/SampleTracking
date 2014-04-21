package com.atlassian.jira.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import org.ofbiz.core.entity.GenericValue;

/**
 */
public interface PermissionContextFactory
{
    PermissionContext getPermissionContext(GenericValue projectOrIssue);

    PermissionContext getPermissionContext(Issue issue);


    PermissionContext getPermissionContext(OperationContext operationContext, Issue issue);
}
