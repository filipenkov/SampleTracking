package com.atlassian.jira.permission;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v3.13.3
 */
public class MockPermissionContext extends PermissionContextImpl
{
    public MockPermissionContext(Issue issue, GenericValue project, Status status)
    {
        super(issue, project, status);
    }
}
