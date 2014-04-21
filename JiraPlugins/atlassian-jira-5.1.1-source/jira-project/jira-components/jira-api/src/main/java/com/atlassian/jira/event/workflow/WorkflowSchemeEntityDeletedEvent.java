package com.atlassian.jira.event.workflow;

/**
 * Event indicating a workflow scheme entity has been deleted from a workflow scheme.
 *
 * @since v5.0
 */
public class WorkflowSchemeEntityDeletedEvent
{
    private Long id;

    public WorkflowSchemeEntityDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
