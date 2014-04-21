/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Iterator;
import java.util.List;

@WebSudoRequired
public class ViewWorkflowSteps extends AbstractWorkflowStep
{
    private final ProjectWorkflowSchemeHelper helper;
    private List<Project> projects;

    public ViewWorkflowSteps(JiraWorkflow workflow, ConstantsManager constantsManager, WorkflowService workflowService, ProjectWorkflowSchemeHelper helper)
    {
        super(workflow, constantsManager, workflowService);
        this.helper = helper;
    }

    @RequiresXsrfCheck
    public String doAddStep() throws Exception
    {
        if (TextUtils.stringSet(getStepName()))
        {
            List existingSteps = getWorkflow().getDescriptor().getSteps();
            for (Iterator iterator = existingSteps.iterator(); iterator.hasNext();)
            {
                StepDescriptor existingStep = (StepDescriptor) iterator.next();
                if (getStepName().equalsIgnoreCase(existingStep.getName()))
                {
                    addError("stepName", getText("admin.errors.step.with.name.already.exists"));
                }

                if (getStepStatus().equalsIgnoreCase((String) existingStep.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
                {
                    addError("stepStatus", getText("admin.errors.existing.step.already.linked"));
                }
            }
        }
        else
        {
            addError("stepName", getText("admin.errors.step.name.must.be.specified"));
        }

        if (!invalidInput())
        {
            StepDescriptor newStep = DescriptorFactory.getFactory().createStepDescriptor();
            newStep.setName(getStepName());
            newStep.setId(WorkflowUtil.getNextId(getWorkflow().getDescriptor().getSteps()));
            newStep.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, getStepStatus());

            newStep.setParent(getWorkflow().getDescriptor());
            getWorkflow().getDescriptor().addStep(newStep);

            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return SUCCESS;
    }

    public boolean isTransitionWithoutStepChange(ActionDescriptor transition)
    {
        return transition.getUnconditionalResult().getStep() == JiraWorkflow.ACTION_ORIGIN_STEP_ID;
    }

    public Status getStatus(String id)
    {
        return getConstantsManager().getStatusObject(id);
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            JiraWorkflow workflow = getWorkflow();
            projects = helper.getProjectsForWorkflow(workflow.getName());
        }
        return projects;
    }
}