package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// Ensure all issues have a valid workflow state.

public class WorkflowCurrentStepCheck extends CheckImpl
{
    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;

    public WorkflowCurrentStepCheck(OfBizDelegator ofBizDelegator, int id)
    {
        this(ofBizDelegator, id, ComponentAccessor.getConstantsManager(), ComponentAccessor.getWorkflowManager());
    }

    public WorkflowCurrentStepCheck(OfBizDelegator ofBizDelegator, int id, ConstantsManager constantsManager, WorkflowManager workflowManager)
    {
        super(ofBizDelegator, id);
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.current.step.desc");
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

        final String name = "WorkFlowCurrentStepCheck.doCheck";
        UtilTimerStack.push(name);

        try
        {
            Collection issueIds = getIssueIds();

            for (Iterator iterator = issueIds.iterator(); iterator.hasNext();)
            {
                Long issueId = (Long) iterator.next();
                GenericValue issue = ofBizDelegator.findById("Issue", issueId);

                if (issue != null)
                {
                    Long workflowEntryId = issue.getLong("workflowId");
                    String issueKey = issue.getString("key");

                    // Retrieve all currentsteps associated with this workflowentry - there should be only ONE
                    List currentSteps = ofBizDelegator.findByAnd("OSCurrentStep", EasyMap.build("entryId", workflowEntryId));

                    GenericValue status = getStatus(issue.getString("status"));
                    // Integrity checker should come back later and fail this
                    if (status != null)
                    {
                        JiraWorkflow workflow = workflowManager.getWorkflow(issue);
                        StepDescriptor stepInWorkflow = workflow.getLinkedStep(status);

                        if (stepInWorkflow != null)
                        {
                            int stepInWorkflowId = stepInWorkflow.getId();

                            switch (currentSteps.size())
                            {
                                // No corresponding step - create one if needed
                                case 0:
                                {
                                    // JRA-6721 - an issue may be associated with a 'dead-end' status - i.e. a status
                                    // without any outgoing transitions. In this case, the workflow entry associated with the
                                    // issue will not have an associated current step
                                    if (!stepInWorkflow.getActions().isEmpty())
                                    {
                                        createStep(correct, workflowEntryId, stepInWorkflowId, issue, results);
                                    }

                                    break;
                                }

                                // Only one currentstep exists - validate the stepId
                                case 1:
                                    validateStep(correct, currentSteps, stepInWorkflowId, issueKey, workflowEntryId, results);
                                    break;

                                    // Multiple currentsteps exist for this workflow entry
                                    // In this case we use the last updated currentstep (ensuring stepId is correct), and delete the others
                                default:
                                    deleteSteps(correct, currentSteps, stepInWorkflowId, issueKey, workflowEntryId, results);
                                    break;
                            }
                        }
                        else
                        {
                            results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR, getI18NBean().getText("admin.integrity.check.workflow.current.step.unfixable", issueKey, status.getString("name")), "JRA-8326"));
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            UtilTimerStack.pop(name);
        }

        return results;
    }

    // Ensure last currentstep has correct stepId value
    // Delete the other currentsteps
    private void deleteSteps(boolean correct, List currentSteps, int stepInWorkflowId, String issueKey, Long workflowEntryId, List results) throws GenericEntityException
    {
        String message;
        if (correct)
        {
            GenericValue step = (GenericValue) currentSteps.get(0);

            // Check if first currentstep has correct stepId
            if (step.getInteger("stepId").intValue() != stepInWorkflowId)
            {
                step.set("stepId", new Integer(stepInWorkflowId));
                step.store();

                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.message1", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }

            for (int j = 1; j < currentSteps.size(); j++)
            {
                step = (GenericValue) currentSteps.get(j);
                step.remove();
            }

            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.message2", issueKey, workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
        else
        {
            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.preview", issueKey, workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
    }

    //Validate that the exisiting step has the correct stepId
    private void validateStep(boolean correct, List currentSteps, int stepInWorkflowId, String issueKey, Long workflowEntryId, List results) throws GenericEntityException
    {
        GenericValue step = (GenericValue) currentSteps.get(0);
        String message;

        if (correct)
        {
            // Check if first currentstep has correct stepId
            if (step.getInteger("stepId") == null || step.getInteger("stepId").intValue() != stepInWorkflowId)
            {
                step.set("stepId", new Integer(stepInWorkflowId));
                step.store();

                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.validate.message", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }
        }
        else
        {
            if (step.getInteger("stepId") == null || step.getInteger("stepId").intValue() != stepInWorkflowId)
            {
                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }
        }
    }

    // Create a step
    private void createStep(boolean correct, Long workflowEntryId, int stepInWorkflowId, GenericValue issue, List results) throws StoreException
    {
        String message;
        if (correct)
        {
            WorkflowStore store = workflowManager.getStore();
            store.createCurrentStep(workflowEntryId.longValue(), stepInWorkflowId, null, issue.getTimestamp("created"), null, issue.getString("status"), null);

            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.correct.message", issue.getString("key"), workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
        else
        {
            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.correct.preview", issue.getString("key"), workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
    }

    private GenericValue getStatus(String status)
    {
        return constantsManager.getStatus(status);
    }

    public Collection getIssueIds()
    {
        Collection issueIds = new ArrayList();

        OfBizListIterator listIterator = null;

        try
        {
            // Retrieve all issues
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            listIterator = ofBizDelegator.findListIteratorByCondition("Issue", null, null, EasyList.build("id"), null, null);
            GenericValue issueIdGV = (GenericValue) listIterator.next();
            while (issueIdGV != null)
            {
                issueIds.add(issueIdGV.getLong("id"));
                issueIdGV = (GenericValue) listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return issueIds;
    }
}
