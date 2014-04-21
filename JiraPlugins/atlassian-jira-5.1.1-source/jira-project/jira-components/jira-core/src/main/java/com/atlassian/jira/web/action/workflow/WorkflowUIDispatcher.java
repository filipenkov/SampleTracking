/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.workflow;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.issue.AbstractViewIssue;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Iterator;
import java.util.Map;

/**
 * A simple action to look at the incoming action + issue ID combination, and redirect to the correct web action.
 */
public class WorkflowUIDispatcher extends AbstractViewIssue implements WorkflowAwareAction
{
    public static final String INVALID_ACTION = "invalidworkflowaction";

    private final WorkflowManager workflowManager;
    private final IssueUtilsBean issueUtils;
    private int action;

    public WorkflowUIDispatcher(SubTaskManager subTaskManager, WorkflowManager workflowManager,
        IssueUtilsBean issueUtils)
    {
        super(subTaskManager);
        this.workflowManager = workflowManager;
        this.issueUtils = issueUtils;
    }


    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Checks if the user can still access the issue
        try
        {
            getIssue();
        }
        catch (IssuePermissionException e)
        {
            return "permissionviolation";
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above
            return ERROR;
        }

        if (!issueUtils.isValidAction(getIssueObject(), action))
        {
            return INVALID_ACTION;
        }

        final WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
        workflowTransitionUtil.setAction(action);
        workflowTransitionUtil.setIssue(getIssueObject());
        if (workflowTransitionUtil.hasScreen())
        {
            final UrlBuilder builder = new UrlBuilder("CommentAssignIssue!default.jspa");
            // add all the existing parameters to the redirected URL
            final Map existingParams = ActionContext.getParameters();
            for (Iterator iterator = existingParams.keySet().iterator(); iterator.hasNext();)
            {
                String paramName = (String) iterator.next();

                //Don't add the returnUrl, it is added below in forceRedirect.
                if (StringUtils.isNotBlank(paramName) && !RETURN_URL_PARAMETER.equals(paramName))
                {
                    String[] paramValues = (String[]) existingParams.get(paramName);
                    for (int i = 0; i < paramValues.length; i++)
                    {
                        String paramValue = paramValues[i];
                        builder.addParameter(paramName, paramValue);
                    }
                }
            }

            return forceRedirect(builder.asUrlString());
        }
        else
        {
            return SUCCESS;
        }
    }

    private ActionDescriptor getActionDescriptor()
    {
        String username = (getLoggedInUser() != null ? getLoggedInUser().getName() : null);
        Workflow wf = workflowManager.makeWorkflow(username);
        long workflowId = getIssue().getLong("workflowId");
        WorkflowDescriptor wd = wf.getWorkflowDescriptor(wf.getWorkflowName(workflowId));
        return wd.getAction(action);
    }

    public String getWorkflowTransitionDisplayName()
    {
        return getWorkflowTransitionDisplayName(getActionDescriptor());
    }
    
    public int getAction()
    {
        return action;
    }

    public void setAction(int action)
    {
        this.action = action;
    }
}
