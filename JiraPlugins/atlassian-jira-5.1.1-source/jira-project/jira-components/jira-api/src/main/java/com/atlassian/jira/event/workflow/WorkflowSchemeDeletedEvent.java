package com.atlassian.jira.event.workflow;

/**
 * Event indicating a workflow scheme has been deleted.
 *
 * @since v5.0
 */
public class WorkflowSchemeDeletedEvent
{
    private Long id;

    public WorkflowSchemeDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
