package com.atlassian.jira.event.workflow;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a workflow scheme has been updated.
 *
 * @since v5.0
 */
public class WorkflowSchemeUpdatedEvent extends AbstractSchemeEvent
{
    public WorkflowSchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
