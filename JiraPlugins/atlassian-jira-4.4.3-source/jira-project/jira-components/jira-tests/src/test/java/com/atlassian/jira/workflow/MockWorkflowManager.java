package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Mock WorkflowManager.
 *
 * @since v3.13
 */
public class MockWorkflowManager implements WorkflowManager
{
    private Map workflowMap = new HashMap();
    private Map draftWorkflowMap = new HashMap();
    private Collection<JiraWorkflow> activeWorkflows = new ArrayList();

    public Collection getWorkflows()
    {
        return null;
    }

    public List getWorkflowsIncludingDrafts()
    {
        return null;
    }

    public boolean isActive(JiraWorkflow workflow) throws WorkflowException
    {
        return false;
    }

    public boolean isSystemWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
        return false;
    }

    public Collection getActiveWorkflows() throws WorkflowException
    {
        return activeWorkflows;
    }

    public MockWorkflowManager addActiveWorkflows(JiraWorkflow workflow) throws WorkflowException
    {
        activeWorkflows.add(workflow);
        return this;
    }

    public JiraWorkflow getWorkflow(String name)
    {
        return (JiraWorkflow) workflowMap.get(name);
    }

    public JiraWorkflow getWorkflowClone(String name)
    {
        return (JiraWorkflow) workflowMap.get(name);
    }

    public JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        return (JiraWorkflow) draftWorkflowMap.get(parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(String username, String parentWorkflowName)
            throws IllegalStateException, IllegalArgumentException
    {
        return null;
    }

    public boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException
    {
        return false;
    }

    public JiraWorkflow updateDraftWorkflow(String username, String parentWorkflowName, JiraWorkflow workflow)
    {
        draftWorkflowMap.put(parentWorkflowName, workflow);
        return workflow;
    }

    public JiraWorkflow getWorkflow(GenericValue issue) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflow(Issue issue) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflow(Long projectId, String issueTypeId) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getWorkflowFromScheme(GenericValue scheme, String issueTypeId) throws WorkflowException
    {
        return null;
    }

    public Collection getWorkflowsFromScheme(GenericValue workflowScheme) throws WorkflowException
    {
        return null;
    }

    public JiraWorkflow getDefaultWorkflow() throws WorkflowException
    {
        return null;
    }

    public GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws WorkflowException
    {
        return null;
    }

    public void removeWorkflowEntries(GenericValue issue) throws GenericEntityException
    {
    }

    public void doWorkflowAction(WorkflowProgressAware from)
    {
    }

    public com.opensymphony.user.User getRemoteUser(Map transientVars) throws EntityNotFoundException
    {
        return null;
    }

    public WorkflowStore getStore() throws StoreException
    {
        return null;
    }

    public void createWorkflow(String username, JiraWorkflow workflow) throws WorkflowException
    {
    }

    @Override
    public void createWorkflow(com.opensymphony.user.User user, JiraWorkflow workflow) throws WorkflowException
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void createWorkflow(User creator, JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void saveWorkflow(User user, JiraWorkflow workflow) throws WorkflowException
    {
        workflowMap.put(workflow.getName(), workflow);
    }

    public void saveWorkflowWithoutAudit(JiraWorkflow workflow) throws WorkflowException
    {
    }

    public void deleteWorkflow(JiraWorkflow workflow) throws WorkflowException
    {
    }

    public ActionDescriptor getActionDescriptor(WorkflowProgressAware workflowProgressAware) throws Exception
    {
        return null;
    }

    public void migrateIssueToWorkflow(MutableIssue issue, JiraWorkflow newWorkflow, Status status)
            throws WorkflowException
    {
    }

    public void migrateIssueToWorkflow(GenericValue issue, JiraWorkflow newWorkflow, GenericValue status)
            throws WorkflowException
    {
    }

    public Workflow makeWorkflow(String userName)
    {
        return null;
    }

    public boolean workflowExists(String name) throws WorkflowException
    {
        return false;
    }

    public boolean isEditable(Issue issue)
    {
        return false;
    }

    public Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(JiraWorkflow workflow)
    {
        return null;
    }

    public String getStepId(long actionDescriptorId, String workflowName)
    {
        return null;
    }

    public void overwriteActiveWorkflow(String username, String workflowName)
    {
    }

    public void updateWorkflow(String username, JiraWorkflow workflow)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    public JiraWorkflow copyWorkflow(String username, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone)
    {
        return null;
    }

    public void updateWorkflowNameAndDescription(String username, JiraWorkflow currentWorkflow, String newName, String newDescription)
    {
    }

    @Override
    public void copyAndDeleteDraftWorkflows(com.opensymphony.user.User user, Set<JiraWorkflow> workflows)
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void copyAndDeleteDraftWorkflows(User user, Set workflows)
    {
    }

}
