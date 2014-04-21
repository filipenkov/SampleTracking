package com.atlassian.jira.workflow;

/**
 * A Factory class to create WorkflowTransitionUtil objects.
 *
 * @since v4.4
 */
public interface WorkflowTransitionUtilFactory
{
    /**
     * Creates a new instance of WorkflowTransitionUtil.
     *
     * @return a new instance of WorkflowTransitionUtil.
     */
    WorkflowTransitionUtil create();
}
