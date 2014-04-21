package com.atlassian.jira.event.permission;

import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Event indicating a permission entity has been added to a permission scheme.
 *
 * @since v5.0
 */
public class PermissionAddedEvent extends AbstractSchemeEntityEvent
{
    public PermissionAddedEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        super(schemeId, schemeEntity);
    }
}
