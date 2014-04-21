package com.atlassian.jira.event.notification;

/**
 * Event indicating a notification entity has been removed from a notification scheme.
 *
 * @since v5.0
 */
public class NotificationDeletedEvent
{
    private Long id;

    public NotificationDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
