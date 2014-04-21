package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import static com.atlassian.jira.web.action.admin.workflow.WorkflowViewMode.parseFromAction;

/**
 * Dispatcher in the form of an action that redirects requests to the appropriate URL for editing a workflow.
 *
 * If the workflow is inactive, edit it directly.
 * If the workflow is active, edit its draft.
 * If a draft does not exist, create one before redirecting.
 *
 * @since v5.1
 */
@WebSudoRequired
public class EditWorkflowDispatcher extends JiraWebActionSupport
{
    private String wfName;
    private final WorkflowService workflowService;

    public EditWorkflowDispatcher(final WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    @RequiresXsrfCheck
    public String doExecute()
    {
        JiraWorkflow workflow = workflowService.getWorkflow(getJiraServiceContext(), getWfName());

        if (hasAnyErrors() || workflow == null)
        {
            return ERROR;
        }

        if (workflow.isSystemWorkflow())
        {
            UrlBuilder builder = new UrlBuilder("ViewWorkflowSteps.jspa")
                    .addParameter("workflowMode", "live").addParameter("workflowName", workflow.getName());
            return getRedirect(builder.asUrlString());
        }
        else if (!workflow.isActive())
        {
            return redirectToEdit(workflow);
        }
        else
        {
            if (!workflow.hasDraftWorkflow())
            {
                workflow = workflowService.createDraftWorkflow(getJiraServiceContext(), workflow.getName());
            }
            else
            {
                workflow = workflowService.getDraftWorkflow(getJiraServiceContext(), workflow.getName());
            }

            if (hasAnyErrors() || workflow == null)
            {
                return ERROR;
            }
            else
            {
                return redirectToEdit(workflow);
            }
        }
    }

    private String redirectToEdit(JiraWorkflow workflow)
    {
        final String mode = workflow.isDraftWorkflow() ? "draft" : "live";
        if (parseFromAction(this) == WorkflowViewMode.DIAGRAM)
        {
            UrlBuilder builder = new UrlBuilder("WorkflowDesigner.jspa")
                    .addParameter("workflowMode", mode).addParameter("wfName", workflow.getName());
            return getRedirect(builder.asUrlString());
        }
        else
        {
            UrlBuilder builder = new UrlBuilder("ViewWorkflowSteps.jspa")
                    .addParameter("workflowMode", mode).addParameter("workflowName", workflow.getName());
            return getRedirect(builder.asUrlString());
        }
    }

    public String getWfName()
    {
        return wfName;
    }

    public void setWfName(final String wfName)
    {
        this.wfName = wfName;
    }
}
