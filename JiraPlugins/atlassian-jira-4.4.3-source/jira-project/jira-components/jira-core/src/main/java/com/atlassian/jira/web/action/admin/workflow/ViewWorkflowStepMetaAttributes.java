package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.Map;

@WebSudoRequired
public class ViewWorkflowStepMetaAttributes extends AbstractViewWorkflowMetaAttributes
{
    private final StepDescriptor step;

    public ViewWorkflowStepMetaAttributes(JiraWorkflow workflow, StepDescriptor step, WorkflowService workflowService)
    {
        super(workflow, workflowService);
        this.step = step;

        initializeAttributes();
    }

    protected Map getEntityMetaAttributes()
    {
        return step.getMetaAttributes();
    }

    protected String getViewRidirect() throws Exception
    {
        return getRedirect("ViewWorkflowStepMetaAttributes.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) + 
                           "&workflowMode=" + getWorkflow().getMode() +
                           "&workflowStep=" + getStep().getId());
    }

    public String getRemoveAttributeUrl(String key)
    {
        return "RemoveWorkflowStepMetaAttribute.jspa?atl_token=" + getXsrfToken() +
               "&workflowName=" + URLEncoder.encode(getWorkflow().getName()) +
               "&workflowMode=" + getWorkflow().getMode() +
               "&workflowStep=" + getStep().getId() +
               "&attributeKey=" + URLEncoder.encode(key);
    }

    public StepDescriptor getStep()
    {
        return step;
    }
}
