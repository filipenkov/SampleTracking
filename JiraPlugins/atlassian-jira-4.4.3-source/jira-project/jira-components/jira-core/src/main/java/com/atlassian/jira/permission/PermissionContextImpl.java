package com.atlassian.jira.permission;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Default {@link PermissionContext} implementation.
 */
public class PermissionContextImpl implements PermissionContext
{
    private static final Logger log = Logger.getLogger(PermissionContextImpl.class);

    final GenericValue project;
    final Issue issue;
    final Status status;

    protected PermissionContextImpl(Issue issue, GenericValue project, Status status)
    {
        this.project = project;
        this.issue = issue;
        this.status = status;
    }

    public final GenericValue getProject()
    {
        return project;
    }

    public Issue getIssue()
    {
        return issue;
    }

    public boolean isHasCreatedIssue()
    {
        return issue != null && issue.getIssueTypeObject() != null;
    }

    public Status getStatus()
    {
        return status;
    }

    /**
     * Given a Permission Context, get the relevant workflow step. This will either be the
     * {@link PermissionContextImpl#getIssue() issue's} or a step
     * {@link PermissionContextImpl#getStatus() set explicitly}.
     */
    public StepDescriptor getRelevantStepDescriptor()
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue not passed into workflow-based permission scheme");
        }
        JiraWorkflow workflow;
        try
        {
            WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
            workflow = workflowManager.getWorkflow(getIssue());
        }
        catch (WorkflowException e)
        {
            PermissionContextImpl.log.error("Could not find workflow for issue " + getIssue(), e);
            throw new RuntimeException("Could not find workflow for issue " + getIssue());
        }
        if (!getIssue().isCreated())
        {
            // The issue is being created; return the StepDescriptor of the initial step
            return getInitialStepDescriptor(workflow);
        }
        else
        {
            return workflow.getLinkedStep(getStatusGV());
        }
    }

    /**
     * Returns the issue's status generic value. The status is either set
     * explicitly or retrieved from the issue.
     *
     * @return the issue's status generic value
     * @throws RuntimeException if status not set explicitly nor on the issue
     */
    private GenericValue getStatusGV() throws RuntimeException
    {
        final Status status = getStatus();
        if (status != null) // status explicitly set
        {
            return status.getGenericValue();
        }
        else
        {
            final Status issueStatus = getIssue().getStatusObject();
            if (issueStatus == null)
            {
                throw new RuntimeException("Could not find workflow status for issue " + getIssue().getKey() + ".");
            }
            else
            {
                return issueStatus.getGenericValue();
            }
        }
    }

    private StepDescriptor getInitialStepDescriptor(JiraWorkflow workflow)
    {
        List initialActions = workflow.getDescriptor().getInitialActions();
        ActionDescriptor initialAction = (ActionDescriptor) initialActions.get(0);
        int initialStep = initialAction.getUnconditionalResult().getStep();
        return workflow.getDescriptor().getStep(initialStep);
    }

    public boolean hasIssuePermissions()
    {
        // For workflow-based permissions, we need the issue and a known type to look up a workflow
        return (issue != null && issue.getIssueTypeObject() != null);
    }
}
