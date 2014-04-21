/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

@WebSudoRequired
public class EditWorkflowSchemeEntities extends SchemeAwareWorkflowAction
{
    private final ProjectWorkflowSchemeHelper helper;
    private List<Project> usedIn;

    public EditWorkflowSchemeEntities(ProjectWorkflowSchemeHelper helper)
    {
        this.helper = helper;
    }

    public GenericValue getDefaultEntity() throws GenericEntityException
    {
        return ((WorkflowSchemeManager) getSchemeManager()).getDefaultEntity(getScheme());
    }

    public List getNonDefaultEntities() throws GenericEntityException
    {
        return ((WorkflowSchemeManager) getSchemeManager()).getNonDefaultEntities(getScheme());
    }

    public JiraWorkflow getWorkflow(String workflow) throws GenericEntityException
    {
        return ManagerFactory.getWorkflowManager().getWorkflow(workflow);
    }

    public String getRedirectURL()
    {
        return null;
    }

    public boolean isActive(GenericValue scheme) throws GenericEntityException
    {
        return ComponentAccessor.getWorkflowSchemeManager().getProjects(scheme).size() > 0;
    }

    public List<Project> getUsedIn()
    {
        if (usedIn == null)
        {
            usedIn = helper.getProjectsForScheme(getSchemeId());
        }
        return usedIn;
    }
}
