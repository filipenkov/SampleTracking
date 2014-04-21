package com.atlassian.jira.event.workflow;

import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a workflow scheme entity has been added to a workflow scheme.
 *
 * @since v5.0
 */
public class WorkflowSchemeEntityAddedEvent extends AbstractSchemeEntityEvent
{
    public WorkflowSchemeEntityAddedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }
}
