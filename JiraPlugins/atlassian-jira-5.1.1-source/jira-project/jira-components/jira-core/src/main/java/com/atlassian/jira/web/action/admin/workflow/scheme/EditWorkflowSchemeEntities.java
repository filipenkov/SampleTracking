package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

@WebSudoRequired
public class EditWorkflowSchemeEntities extends SchemeAwareWorkflowAction
{
    private final ProjectWorkflowSchemeHelper helper;
    private final WorkflowManager workflowManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private List<Project> usedIn;

    public EditWorkflowSchemeEntities(final ProjectWorkflowSchemeHelper helper, final WorkflowManager workflowManager,
            final WorkflowSchemeManager workflowSchemeManager)
    {
        this.helper = helper;
        this.workflowManager = workflowManager;
        this.workflowSchemeManager = workflowSchemeManager;
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
        return workflowManager.getWorkflow(workflow);
    }

    public String getRedirectURL()
    {
        return null;
    }

    public boolean isActive(GenericValue scheme) throws GenericEntityException
    {
        return workflowSchemeManager.getProjects(scheme).size() > 0;
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
