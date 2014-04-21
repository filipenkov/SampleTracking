package com.atlassian.jira.event.permission;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a permission scheme has been updated.
 *
 * @since v5.0
 */
public class PermissionSchemeUpdatedEvent extends AbstractSchemeEvent
{
    public PermissionSchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
