package com.atlassian.jira.event.bc.project.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating an project component has been updated
 *
 * @since v5.1
 */
public class ProjectComponentUpdatedEvent extends AbstractProjectComponentEvent
{
    public ProjectComponentUpdatedEvent(ProjectComponent projectComponent)
    {
        super(projectComponent);
    }
}
