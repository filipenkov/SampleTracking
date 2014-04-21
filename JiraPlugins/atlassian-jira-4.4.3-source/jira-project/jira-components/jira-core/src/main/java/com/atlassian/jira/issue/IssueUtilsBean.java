package com.atlassian.jira.issue;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.user.User;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IssueUtilsBean
{
    private static final Logger log = Logger.getLogger(IssueUtilsBean.class);

    private final IssueManager issueManager;
    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;

    public IssueUtilsBean(IssueManager issueManager, WorkflowManager workflowManager,
                          JiraAuthenticationContext authenticationContext)
    {
        this.issueManager = issueManager;
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
    }

    public Map<Integer, ActionDescriptor> loadAvailableActions(Issue issueObject)
    {
        final Project project = issueObject.getProjectObject();
        final Map<Integer, ActionDescriptor> availableActions = new LinkedHashMap<Integer, ActionDescriptor>();
        final Issue originalIssue;

        if (issueObject instanceof DocumentIssueImpl)
        {
            issueObject = issueManager.getIssueObject(issueObject.getId());
            originalIssue = issueObject;

        }
        else
        {
            if (issueObject.getWorkflowId() == null)
            {
                log.warn("!!! Issue " + issueObject.getKey() + " has no workflow ID !!! ");
                return availableActions;
            }
            originalIssue = issueManager.getIssueObject(issueObject.getId());
        }

        try
        {
            final Workflow wf = getWorkflow();
            final WorkflowDescriptor wd = workflowManager.getWorkflow(issueObject).getDescriptor();

            final HashMap<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("pkey", project.getKey()); // Allows ${project.key} in condition args
            inputs.put("issue", issueObject);
            // The condition should examine the original issue object - put this in the transientvars
            // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
            int[] actionIds = wf.getAvailableActions(issueObject.getWorkflowId(), inputs);

            for (int actionId : actionIds)
            {
                final ActionDescriptor action = wd.getAction(actionId);
                if (action == null)
                {
                    log.error("State of issue [" + issueObject + "] has an action [id=" + actionId
                            + "] which cannot be found in the workflow descriptor");
                }
                else
                {
                    availableActions.put(actionId, action);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
        }

        return availableActions;
    }

    /**
     * Is this a valid action for the issue in its current state
     *
     * @param issue  the issue
     * @param action the id of the action we want to transition
     * @return true if it is ok to use this transition
     */
    public boolean isValidAction(Issue issue, int action)
    {
        return loadAvailableActions(issue).containsKey(action);
    }

    public Workflow getWorkflow()
    {
        return workflowManager.makeWorkflow(authenticationContext.getUser() != null ? authenticationContext.getUser().getName() : null);
    }

    @Deprecated
    public GenericValue setPriority(GenericValue issue, User remoteUser, String priority) throws Exception
    {
        return IssueUtils.setPriority(issue, remoteUser, priority);
    }
}
