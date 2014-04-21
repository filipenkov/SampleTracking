/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@WebSudoRequired
public class AddWorkflowSchemeEntity extends SchemeAwareWorkflowAction
{
    private String type;
    private String workflow;

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getWorkflowSchemeManager();
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
                addErrorMessage(getText("admin.errors.workflows.must.select.scheme"));
            if (!TextUtils.stringSet(workflow))
                addError("workflowname", getText("admin.errors.workflows.must.select.workflow.to.assign"));
            if (!TextUtils.stringSet(getType()))
                addErrorMessage(getText("admin.errors.workflows.must.select.type"));
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.workflows.error.occured", e.getMessage()));
        }
    }

    protected String doExecute() throws Exception
    {
        ManagerFactory.getWorkflowSchemeManager().addWorkflowToScheme(getScheme(), getWorkflow(), getType());
        return getRedirect(getRedirectURL() + getSchemeId());
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getIssueTypes() throws GenericEntityException
    {
        Map types = new ListOrderedMap();

        // if we don't have a default workflow yet, add it to the list
        if (getSchemeManager().getEntities(getScheme(), "0").size() == 0)
            types.put("0", getText("admin.schemes.workflows.all.unassigned"));

        // Maintain order of issue types and sub task types in drop-down menu
        for (Iterator iterator = getConstantsManager().getRegularIssueTypeObjects().iterator(); iterator.hasNext();)
        {
            IssueType issueType = (IssueType) iterator.next();

            // add all types that we don't currently have assigned
            if (getSchemeManager().getEntities(getScheme(), issueType.getId()).size() == 0)
                types.put(issueType.getId(), issueType.getNameTranslation());
        }

        // Only show subtasks if they are enabled
        if (ComponentManager.getInstance().getSubTaskManager().isSubTasksEnabled())
        {
            for (Iterator iterator = getConstantsManager().getSubTaskIssueTypeObjects().iterator(); iterator.hasNext();)
            {
                IssueType subTaskType = (IssueType) iterator.next();

                // add all sub task types that we don't currently have assigned
                if (getSchemeManager().getEntities(getScheme(), subTaskType.getId()).size() == 0)
                    types.put(subTaskType.getId(), subTaskType.getNameTranslation());
            }
        }

        return types;
    }

    public Collection getWorkflows() throws WorkflowException
    {
        return ManagerFactory.getWorkflowManager().getWorkflows();
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
