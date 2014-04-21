package com.atlassian.jira.event.issue.security;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating an issue security scheme has been updated.
 *
 * @since v5.0
 */
public class IssueSecuritySchemeUpdatedEvent extends AbstractSchemeEvent
{
    public IssueSecuritySchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
