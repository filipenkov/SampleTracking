package com.atlassian.jira.event.notification;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 *  Event indicating a notification scheme has been created.
 *
 * @since v5.0
 */
public class NotificationSchemeCreatedEvent extends AbstractSchemeEvent
{
    public NotificationSchemeCreatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
