package com.atlassian.jira.event.workflow;

import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a workflow scheme has been copied.
 *
 * @since v5.0
 */
public class WorkflowSchemeCopiedEvent extends AbstractSchemeCopiedEvent
{
    public WorkflowSchemeCopiedEvent(Scheme fromScheme, Scheme toScheme)
    {
        super(fromScheme, toScheme);
    }
}
