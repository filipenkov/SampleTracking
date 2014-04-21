package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager} instead. Since v5.0.
 */
public class IssueUtilsBean
{
    private static final Logger log = Logger.getLogger(IssueUtilsBean.class);

    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueWorkflowManager issueWorkflowManager;

    public IssueUtilsBean(WorkflowManager workflowManager,
            JiraAuthenticationContext authenticationContext, IssueWorkflowManager issueWorkflowManager)
    {
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
        this.issueWorkflowManager = issueWorkflowManager;
    }

    /**
     * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager#getAvailableActions(Issue)} instead. Since v5.0.
     */
    public Map<Integer, ActionDescriptor> loadAvailableActions(Issue issueObject)
    {
        final Map<Integer, ActionDescriptor> availableActions = new LinkedHashMap<Integer, ActionDescriptor>();
        for (ActionDescriptor actionDescriptor : issueWorkflowManager.getAvailableActions(issueObject))
        {
            availableActions.put(actionDescriptor.getId(), actionDescriptor);
        }
        return availableActions;
    }

    /**
     * Is this a valid action for the issue in its current state
     *
     * @param issue  the issue
     * @param action the id of the action we want to transition
     * @return true if it is ok to use this transition
     *
     * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager#isValidAction(Issue, int)} instead. Since v5.0.
     */
    public boolean isValidAction(Issue issue, int action)
    {
        return loadAvailableActions(issue).containsKey(action);
    }

    /**
     * @deprecated This should have been private. Use {@link WorkflowManager#makeWorkflow(com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public Workflow getWorkflow()
    {
        return workflowManager.makeWorkflow(authenticationContext.getLoggedInUser() != null ? authenticationContext.getLoggedInUser().getName() : null);
    }

    @Deprecated
    public GenericValue setPriority(GenericValue issue, User remoteUser, String priority) throws Exception
    {
        return IssueUtils.setPriority(issue, remoteUser, priority);
    }
}
