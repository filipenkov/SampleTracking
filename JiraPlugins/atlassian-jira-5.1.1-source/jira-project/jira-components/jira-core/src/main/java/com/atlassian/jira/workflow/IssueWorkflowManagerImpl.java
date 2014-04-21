package com.atlassian.jira.workflow;

import com.atlassian.jira.issue.DocumentIssueImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueWorkflowManagerImpl implements IssueWorkflowManager
{
    private static final Logger log = Logger.getLogger(IssueWorkflowManagerImpl.class);

    private final IssueManager issueManager;
    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;

    public IssueWorkflowManagerImpl(IssueManager issueManager, WorkflowManager workflowManager, JiraAuthenticationContext authenticationContext)
    {
        this.issueManager = issueManager;
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Collection<ActionDescriptor> getAvailableActions(Issue issue)
    {
        int[] actionIds = getAvailableActionIds(issue);
        final Collection<ActionDescriptor> availableActions = new ArrayList<ActionDescriptor>(actionIds.length);
        final WorkflowDescriptor workflowDescriptor = workflowManager.getWorkflow(issue).getDescriptor();

        for (int actionId : actionIds)
        {
            final ActionDescriptor action = workflowDescriptor.getAction(actionId);
            if (action == null)
            {
                log.error("State of issue [" + issue + "] has an action [id=" + actionId
                        + "] which cannot be found in the workflow descriptor");
            }
            else
            {
                availableActions.add(action);
            }
        }

        return availableActions;
    }

    @Override
    public List<ActionDescriptor> getSortedAvailableActions(Issue issue)
    {
        List<ActionDescriptor> availableActions = Lists.newArrayList(getAvailableActions(issue));
        Collections.sort(availableActions, new Comparator<ActionDescriptor>()
        {
            @Override
            public int compare(ActionDescriptor actionDescriptor, ActionDescriptor actionDescriptor1)
            {
                return getSequenceFromAction(actionDescriptor).compareTo(getSequenceFromAction(actionDescriptor1));
            }
        });
        return availableActions;
    }

    private Integer getSequenceFromAction(ActionDescriptor action)
    {
        if (action == null)
        {
            return Integer.MAX_VALUE;
        }

        final Map metaAttributes = action.getMetaAttributes();
        if (metaAttributes == null)
        {
            return Integer.MAX_VALUE;
        }

        final String value = (String) metaAttributes.get("opsbar-sequence");

        if (value == null || StringUtils.isBlank(value) || !StringUtils.isNumeric(value))
        {
            return Integer.MAX_VALUE;
        }

        return Integer.valueOf(value);
    }

    @Override
    public boolean isValidAction(Issue issue, int actionid)
    {
        for (int id : getAvailableActionIds(issue))
        {
            if (id == actionid)
            {
                return true;
            }
        }
        return false;
    }

    private int[] getAvailableActionIds(Issue issue)
    {
        final Project project = issue.getProjectObject();
        final Issue originalIssue;

        if (issue instanceof DocumentIssueImpl)
        {
            issue = issueManager.getIssueObject(issue.getId());
            originalIssue = issue;
        }
        else
        {
            if (issue.getWorkflowId() == null)
            {
                log.error("!!! Issue " + issue.getKey() + " has no workflow ID !!! ");
                return new int[0];
            }
            originalIssue = issueManager.getIssueObject(issue.getId());
        }

        final Workflow workflow = workflowManager.makeWorkflow(authenticationContext.getLoggedInUser());
        final HashMap<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("pkey", project.getKey()); // Allows ${project.key} in condition args
        inputs.put("issue", issue);
        // The condition should examine the original issue object - put this in the transientvars
        // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
        inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
        return workflow.getAvailableActions(issue.getWorkflowId(), inputs);
    }
}
