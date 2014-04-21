package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This check will update an issues status with the value in the workflow entry table if null.
 */
public class WorkflowIssueStatusNull extends CheckImpl
{

    private WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();

    public WorkflowIssueStatusNull(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.issue.status.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    private List doCheck(boolean correct) throws IntegrityException
    {
        List results = new ArrayList();

        HashMap statusKeysMap = new HashMap();
        HashMap statusCache = new HashMap();

        try
        {
            // get all the issueSteps with status of null
            final List issueSteps = ofBizDelegator.findByAnd("IssueWorkflowStepView", EasyMap.build("status", null));

            for (Iterator iterator = issueSteps.iterator(); iterator.hasNext();)
            {
                GenericValue genericValue = (GenericValue) iterator.next();
                String issueKey = genericValue.getString("issueKey");

                try
                {
                    GenericValue status = getStatusFromWorkflow(genericValue, statusCache);
                    String statusFromWorkflow = status.getString("id");
                    Long issueId = genericValue.getLong("issueid");
                    List issueIds = (List) statusKeysMap.get(statusFromWorkflow);
                    if(issueIds == null)
                    {
                        issueIds = new ArrayList();
                        statusKeysMap.put(statusFromWorkflow, issueIds);
                    }
                    issueIds.add(issueId);
                    if (correct)
                    {
                        results.add(new CheckAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.message", issueKey, status.getString("name")), "JRA-7428"));
                    }
                    else
                    {
                        // If we are just previewing then simply record the message
                        results.add(new CheckAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.preview", issueKey, status.getString("name")), "JRA-7428"));
                    }
                }
                catch(IllegalStateException ise)
                {
                    results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.unfixable", issueKey, ise.getMessage()), "JRA-7428"));
                }
            }

            if (correct)
            {
                // iterate through the map and perform a bulk update for each set of issues and status
                for (Iterator iterator = statusKeysMap.keySet().iterator(); iterator.hasNext();)
                {
                    String statusId = (String)iterator.next();
                    List issueIds = (List) statusKeysMap.get(statusId);
                    ofBizDelegator.bulkUpdateByPrimaryKey("Issue", EasyMap.build("status", statusId), issueIds);
                }
            }
        }
        catch (Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }

        return results;
    }

    private GenericValue getStatusFromWorkflow(GenericValue genericValue, HashMap statusCache) throws WorkflowException, IllegalStateException
    {
        GenericValue status;
        String cacheKey = genericValue.getLong("project") + ":" + genericValue.getString("type") + ":" + genericValue.getInteger("stepId");
        status = (GenericValue) statusCache.get(cacheKey);
        if(status == null)
        {
            JiraWorkflow workflow = workflowManager.getWorkflow(genericValue);
            if(workflow == null)
                throw new IllegalStateException("Workflow for project id " + genericValue.getLong("project") + " and issue type id " + genericValue.getString("type")+ " is not defined");

            Integer stepId = genericValue.getInteger("stepId");
            if (stepId == null)
                throw new IllegalStateException("Issue has no status, and status cannot be derived as the workflow step for this issue is missing.");

            StepDescriptor step = workflow.getDescriptor().getStep(stepId.intValue());
            if(step == null)
                throw new IllegalStateException("Can not resolve a step with id: " + stepId + " from workflow " + workflow.getName());

            status = workflow.getLinkedStatus(step);
            if(status == null)
                throw new IllegalStateException("Can not resolve a linked status for workflow step " + step.getName());

            statusCache.put(cacheKey, status);
        }
        return status;
    }

    public void setWorkflowManager(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

}
