package com.atlassian.jira.event.notification;

import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 *  Event indicating a notification scheme has been updated.
 *
 * @since v5.0
 */
public class NotificationSchemeUpdatedEvent extends AbstractSchemeEvent
{
    public NotificationSchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
