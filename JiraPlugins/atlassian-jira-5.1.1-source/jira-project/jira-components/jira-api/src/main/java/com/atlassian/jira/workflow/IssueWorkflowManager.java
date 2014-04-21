package com.atlassian.jira.workflow;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Works with workflows on Issues.
 * <p>
 * While {@link WorkflowManager} deals with the global administration of Workflows, this Manager supplies operations
 * that work on the wokrflow and current state of an individual Issue.
 *
 * @since v5.0
 *
 * @see com.atlassian.jira.issue.IssueManager
 * @see WorkflowManager
 */
@PublicApi
public interface IssueWorkflowManager
{
    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state.
     *
     * @param issue the Issue
     * @return the Workflow actions that are valid for the given Issue in its current state.
     */
    public Collection<ActionDescriptor> getAvailableActions(Issue issue);

    /**
     * Returns the Workflow actions that are valid for the given Issue in its current state.
     * The list is sorted by the sequence number.
     *
     * @param issue  the Issue
     * @return the Workflow actions that are valid for the given Issue in its current state.
     */
    public List<ActionDescriptor> getSortedAvailableActions(Issue issue);

    /**
     * Returns true if the given transition ID is valid for the given issue.
     *
     * @param issue the Issue
     * @param action the id of the action we want to transition
     * @return true if it is ok to use the given transition on this issue.
     */
    public boolean isValidAction(Issue issue, int action);
}
