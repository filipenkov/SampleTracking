/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;

/**
 * Used to Show the local instances Contants (Issue types, priorities etc)
 */
public class ShowConstantsHelp extends JiraWebActionSupport
{
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final PermissionManager permissionManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public ShowConstantsHelp(ConstantsManager constantsManager, SubTaskManager subTaskManager, PermissionManager permissionManager, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.permissionManager = permissionManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    // Protected -----------------------------------------------------
    protected String doExecute() throws Exception
    {
        if (!permissionManager.hasProjects(Permissions.BROWSE, getRemoteUser()))
        {
            return ERROR;
        }
        return super.doExecute();
    }

    public boolean isSubTasksEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public Collection getIssueTypes()
    {
        return constantsManager.getIssueTypes();
    }

    public IssueType getIssueType(String id)
    {
        return constantsManager.getIssueTypeObject(id);
    }

    public Collection getSubTaskIssueTypes()
    {
        if (!isSubTasksEnabled())
        {
            throw new IllegalStateException("Should not call this method as subtasks are disabled");
        }

        return constantsManager.getSubTaskIssueTypes();
    }

    public Collection getPriorities()
    {
        return constantsManager.getPriorityObjects();
    }

    public Collection getStatuses()
    {
        return constantsManager.getStatusObjects();
    }

    public Collection getResolutions()
    {
        return constantsManager.getResolutionObjects();
    }

    public Collection getSecurityLevels()
    {
        try
        {
            return issueSecurityLevelManager.getUsersSecurityLevels(getSelectedProject(), getRemoteUser());
        }
        catch (GenericEntityException e)
        {
            log.error("Could not get user security levels for project", e);
        }

        return null;
    }
}