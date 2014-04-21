package com.atlassian.jira.event.permission;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a permission scheme has been created.
 *
 * @since v5.0
 */
public class PermissionSchemeCreatedEvent extends AbstractSchemeEvent
{
    public PermissionSchemeCreatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
