package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Webwork action used to create a draft workflow.
 *
 * @since v3.13
 */
@WebSudoRequired
public class CreateDraftWorkflow extends JiraWebActionSupport
{
    private String draftWorkflowName;
    private final WorkflowService workflowService;

    public CreateDraftWorkflow(final WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    protected void doValidation()
    {
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        workflowService.createDraftWorkflow(getJiraServiceContext(), getDraftWorkflowName());
        if (hasAnyErrors())
        {
            return ERROR;
        }

        return getRedirect("ViewWorkflowSteps.jspa?workflowMode=draft&workflowName=" + JiraUrlCodec.encode(getDraftWorkflowName()));
    }

    public String getDraftWorkflowName()
    {
        return draftWorkflowName;
    }

    public void setDraftWorkflowName(final String draftWorkflowName)
    {
        this.draftWorkflowName = draftWorkflowName;
    }
}
