package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.Map;

@WebSudoRequired
public class ViewWorkflowTransitionMetaAttributes extends AbstractViewWorkflowMetaAttributes
{
    private final StepDescriptor step;
    private final ActionDescriptor transition;

    public ViewWorkflowTransitionMetaAttributes(JiraWorkflow workflow, StepDescriptor step,
            ActionDescriptor transition, WorkflowService workflowService)
    {
        super(workflow, workflowService);
        this.step = step;
        this.transition = transition;

        initializeAttributes();
    }

    public ViewWorkflowTransitionMetaAttributes(JiraWorkflow workflow, ActionDescriptor transition,
            WorkflowService workflowService)
    {
        this(workflow, null, transition, workflowService);

        initializeAttributes();
    }

    protected Map getEntityMetaAttributes()
    {
        return transition.getMetaAttributes();
    }

    protected String getViewRidirect() throws Exception
    {
        if (getStep() == null)
        {
            return getRedirect("ViewWorkflowTransitionMetaAttributes.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) + 
                               "&workflowMode=" + getWorkflow().getMode() +
                               "&workflowTransition=" + getTransition().getId());
        }
        else
        {
            return getRedirect("ViewWorkflowTransitionMetaAttributes.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) +
                               "&workflowMode=" + getWorkflow().getMode() +
                               "&workflowStep=" + getStep().getId() +
                               "&workflowTransition=" + getTransition().getId());
        }
    }

    public String getRemoveAttributeUrl(String key)
    {
        if (getStep() == null)
        {
            return "RemoveWorkflowTransitionMetaAttribute.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) +
                   "&workflowMode=" + getWorkflow().getMode() +
                   "&workflowTransition=" + getTransition().getId() +
                   "&attributeKey=" + URLEncoder.encode(key);
        }
        else
        {
            return "RemoveWorkflowTransitionMetaAttribute.jspa?workflowName=" + URLEncoder.encode(getWorkflow().getName()) + 
                   "&workflowMode=" + getWorkflow().getMode() +
                   "&workflowStep=" + getStep().getId() +
                   "&workflowTransition=" + getTransition().getId() +
                   "&attributeKey=" + URLEncoder.encode(key);
        }
    }

    public StepDescriptor getStep()
    {
        return step;
    }

    public ActionDescriptor getTransition()
    {
        return transition;
    }
}
