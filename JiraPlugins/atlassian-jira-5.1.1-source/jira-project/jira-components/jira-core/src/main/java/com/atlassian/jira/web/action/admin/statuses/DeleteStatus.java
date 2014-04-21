package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;

@WebSudoRequired
public class DeleteStatus extends AbstractDeleteConstant
{
    private final StatusManager statusManager;
    private final WorkflowManager workflowManager;

    public DeleteStatus(final StatusManager statusManager, final WorkflowManager workflowManager)
    {
        this.statusManager = statusManager;
        this.workflowManager = workflowManager;
    }

    protected String getConstantEntityName()
    {
        return "Status";
    }

    protected String getNiceConstantName()
    {
        return "status";
    }

    protected String getIssueConstantField()
    {
        return "status";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getStatus(id);
    }

    protected String getRedirectPage()
    {
        return "ViewStatuses.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getStatuses();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    protected void doValidation()
    {
        try
        {
            if (getConstant() == null)
            {
                addErrorMessage(getText("admin.errors.no.constant.found", getNiceConstantName(), id));
            }
            else if (!getAssociatedWorkflows(getConstant()).isEmpty())
            {
                addErrorMessage(getText("admin.errors.constant.associated.with.workflow", getNiceConstantName(), getId()));
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred: " + e, e);
            addErrorMessage(getText("admin.errors.general.error.occurred", e));
        }
    }

    public Collection getAssociatedWorkflows(GenericValue statusGV)
    {
        Collection<JiraWorkflow> workflows = workflowManager.getWorkflowsIncludingDrafts();
        Collection<String> associatedWorkflows = new ArrayList<String>();

        for (JiraWorkflow workflow : workflows)
        {
            Collection<GenericValue> linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(statusGV))
            {
                associatedWorkflows.add(workflow.getName());
            }

        }
        return associatedWorkflows;
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            statusManager.removeStatus(id);
        }
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect(getRedirectPage());
        }
    }
}
