package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;

/**
 * Event that is triggered when a project is created.
 */
@PublicApi
public final class ProjectCreatedEvent extends AbstractEvent
{
    private final Long projectId;

    public ProjectCreatedEvent(Long id)
    {

        this.projectId = id;
    }

    /**
     * @return the project ID
     */
    public Long getId()
    {
        return projectId;
    }
}
