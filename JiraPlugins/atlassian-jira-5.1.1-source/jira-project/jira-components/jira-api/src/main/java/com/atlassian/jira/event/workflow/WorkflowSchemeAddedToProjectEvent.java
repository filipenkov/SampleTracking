package com.atlassian.jira.event.workflow;

/**
 * Event indicating a workflow scheme has associated with a project.
 *
 * @since v5.0
 */
public class WorkflowSchemeAddedToProjectEvent
{
    private Long projectId;
    private Long schemeId;

    public WorkflowSchemeAddedToProjectEvent(Long projectId, Long schemeId)
    {
        this.projectId = projectId;
        this.schemeId = schemeId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }
}
