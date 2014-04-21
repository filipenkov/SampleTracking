/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@WebSudoRequired
public class DeleteStatus extends AbstractDeleteConstant
{
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

    protected Collection getConstants()
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
        WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
        Collection workflows = workflowManager.getWorkflowsIncludingDrafts();
        Collection associatedWorkflows = new ArrayList();

        for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
        {
            JiraWorkflow workflow = (JiraWorkflow) iterator.next();
            Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(statusGV))
            {
                associatedWorkflows.add(workflow.getName());
            }

        }
        return associatedWorkflows;
    }
}
