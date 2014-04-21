/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.issuetypes.DeleteIssueType;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;

@WebSudoRequired
public class DeleteSubTaskIssueType extends DeleteIssueType
{
    public DeleteSubTaskIssueType(FieldLayoutManager fieldLayoutManager, ProjectManager projectManager, WorkflowManager workflowManager, WorkflowSchemeManager workflowSchemeManager,
                                  IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, CustomFieldManager customFieldManager, IssueTypeSchemeManager issueTypeSchemeManager,
                                  FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        super(fieldLayoutManager, projectManager, workflowManager, workflowSchemeManager, issueTypeScreenSchemeManager, customFieldManager, issueTypeSchemeManager, fieldConfigSchemeManager);
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        return super.doExecute();
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.subtask.issuetype.lowercase");
    }

    protected String getRedirectPage()
    {
        return "ManageSubTasks.jspa";
    }

    protected Collection getConstants()
    {
        return getConstantsManager().getSubTaskIssueTypes();
    }
}
