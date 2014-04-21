/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

@WebSudoRequired
public class ViewStatuses extends AbstractViewConstants
{
    public static final String STATUS_ENTITY_NAME = "Status";
    private static final String NEW_STATUS_DEFAULT_ICON = "/images/icons/status_generic.gif";

    public ViewStatuses(TranslationManager translationManager)
    {
        super(translationManager);
        setIconurl(NEW_STATUS_DEFAULT_ICON);
    }

    protected String getConstantEntityName()
    {
        return STATUS_ENTITY_NAME;
    }

    protected String getNiceConstantName()
    {
        return "status";
    }

    protected String getIssueConstantField()
    {
        return getText("admin.issue.constant.status.lowercase");
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

    @RequiresXsrfCheck
    public String doAddStatus() throws Exception
    {
        if (!TextUtils.stringSet(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.must.specify.url.for.icon.of.status"));
        }

        return super.doAddConstant();
    }

    public Collection getAssociatedWorkflows(GenericValue statusGV)
    {
        WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
        Collection workflows = workflowManager.getWorkflowsIncludingDrafts();
        // We use a set here, because workflows and any associated drafts have the same name,
        // and we only want to record the workflow once.
        Collection associatedWorkflows = new HashSet();

        for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
        {
            JiraWorkflow workflow = (JiraWorkflow) iterator.next();
            Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(statusGV))
                associatedWorkflows.add(workflow.getName());

        }
        return associatedWorkflows;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewStatuses.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_STATUS;
    }

}
