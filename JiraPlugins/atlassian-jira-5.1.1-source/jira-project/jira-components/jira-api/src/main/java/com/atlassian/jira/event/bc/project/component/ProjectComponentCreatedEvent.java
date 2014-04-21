package com.atlassian.jira.event.bc.project.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Event indicating an project component has been created
 *
 * @since v5.1
 */
public class ProjectComponentCreatedEvent extends AbstractProjectComponentEvent
{
    public ProjectComponentCreatedEvent(ProjectComponent projectComponent)
    {
        super(projectComponent);
    }
}
