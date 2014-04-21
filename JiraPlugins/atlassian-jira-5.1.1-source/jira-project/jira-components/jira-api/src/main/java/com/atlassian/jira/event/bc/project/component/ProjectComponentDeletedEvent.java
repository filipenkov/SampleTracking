package com.atlassian.jira.event.bc.project.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating an project component has been deleted
 *
 * @since v5.1
 */
public class ProjectComponentDeletedEvent
{

    private Long id;

    public ProjectComponentDeletedEvent(Long projectComponentId)
    {
        this.id = projectComponentId;
    }

    public Long getId()
    {
        return id;
    }
}
