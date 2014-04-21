package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

@WebSudoRequired
public class AddWorkflowSchemeEntity extends SchemeAwareWorkflowAction
{
    private String type;
    private String workflow;

    private final WorkflowSchemeManager workflowSchemeManager;
    private final SubTaskManager subTaskManager;
    private final WorkflowManager workflowManager;

    public AddWorkflowSchemeEntity(final WorkflowSchemeManager workflowSchemeManager,
            final SubTaskManager subTaskManager, final WorkflowManager workflowManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.subTaskManager = subTaskManager;
        this.workflowManager = workflowManager;
    }

    public SchemeManager getSchemeManager()
    {
        return workflowSchemeManager;
    }

    public String getRedirectURL()
    {
        return "EditWorkflowSchemeEntities!default.jspa?schemeId=";
    }

    protected void doValidation()
    {
        try
        {
            if (getSchemeId() == null || getScheme() == null)
            {
                addErrorMessage(getText("admin.errors.workflows.must.select.scheme"));
            }
            if (isBlank(workflow))
            {
                addError("workflowname", getText("admin.errors.workflows.must.select.workflow.to.assign"));
            }
            if (isBlank(getType()))
            {
                addErrorMessage(getText("admin.errors.workflows.must.select.type"));
            }
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.workflows.error.occured", e.getMessage()));
        }
    }

    protected String doExecute() throws Exception
    {
        workflowSchemeManager.addWorkflowToScheme(getScheme(), getWorkflow(), getType());
        return returnCompleteWithInlineRedirect(getRedirectURL() + getSchemeId());
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map<String, String> getIssueTypes() throws GenericEntityException
    {
        Map<String, String> types = Maps.newLinkedHashMap();

        // if we don't have a default workflow yet, add it to the list
        if (getSchemeManager().getEntities(getScheme(), "0").size() == 0)
        {
            types.put("0", getText("admin.schemes.workflows.all.unassigned"));
        }

        // Maintain order of issue types and sub task types in drop-down menu
        for (IssueType issueType : getConstantsManager().getRegularIssueTypeObjects())
        {
            // add all types that we don't currently have assigned
            if (getSchemeManager().getEntities(getScheme(), issueType.getId()).size() == 0)
            { types.put(issueType.getId(), issueType.getNameTranslation()); }
        }

        // Only show subtasks if they are enabled
        if (subTaskManager.isSubTasksEnabled())
        {
            for (IssueType subTaskType : getConstantsManager().getSubTaskIssueTypeObjects())
            {
                // add all sub task types that we don't currently have assigned
                if (getSchemeManager().getEntities(getScheme(), subTaskType.getId()).size() == 0)
                { types.put(subTaskType.getId(), subTaskType.getNameTranslation()); }
            }
        }

        return types;
    }

    public Collection<JiraWorkflow> getWorkflows()
    {
        return workflowManager.getWorkflows();
    }

    public String getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(String workflow)
    {
        this.workflow = workflow;
    }
}
