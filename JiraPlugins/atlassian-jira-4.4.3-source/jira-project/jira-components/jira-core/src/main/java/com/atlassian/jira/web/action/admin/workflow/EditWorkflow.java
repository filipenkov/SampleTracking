package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Action to edit a workflows name and description
 *
 * @since v3.13
 */
@WebSudoRequired
public class EditWorkflow extends JiraWebActionSupport
{
    private String workflowName;//original worflow name
    private String newWorkflowName;
    private String description;

    private final JiraWorkflow workflow;
    private final WorkflowService workflowService;

    public EditWorkflow(final JiraWorkflow jiraWorkflow, final WorkflowService workflowService)
    {
        workflow = jiraWorkflow;
        this.workflowService = workflowService;
    }

    public String doDefault() throws Exception
    {
        if (workflow.isEditable())
        {
            newWorkflowName = workflow.getName();
            description = workflow.getDescription();
            return INPUT;
        }
        else
        {
            addErrorMessage(getText("admin.errors.workflow.cannot.be.edited.as.it.is.not.editable"));
        }

        return ERROR;
    }

    protected void doValidation()
    {
        //draft workflows are not allowed to change name!
        if (workflow.isDraftWorkflow())
        {
            newWorkflowName = workflow.getName();
        }
        workflowService.validateUpdateWorkflowNameAndDescription(getJiraServiceContext(), workflow, newWorkflowName);
    }

    @RequiresXsrfCheck
    public String doExecute() throws WorkflowException
    {
        workflowService.updateWorkflowNameAndDescription(getJiraServiceContext(), workflow, newWorkflowName, description);
        if (isInlineDialogMode())
        {
            //looks like we were editing from the the workflow designer in a dialog.  Lets return back to that page
            //after the edit.
            return returnCompleteWithInlineRedirect("/secure/admin/WorkflowDesigner.jspa?wfName=" + JiraUrlCodec.encode(newWorkflowName, getApplicationProperties().getEncoding()) + "&workflowMode=" + workflow.getMode());
        }
        return getRedirect("ListWorkflows.jspa");
    }

    public String getNewWorkflowName()
    {
        return newWorkflowName;
    }

    public void setNewWorkflowName(final String newWorkflowName)
    {
        this.newWorkflowName = newWorkflowName;
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(final String workflowName)
    {
        this.workflowName = workflowName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }
}
