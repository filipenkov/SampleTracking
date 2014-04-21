package com.atlassian.jira.event.notification;

/**
 *  Event indicating a notification scheme has been deleted.
 *
 * @since v5.0
 */
public class NotificationSchemeDeletedEvent
{
    private Long id;

    public NotificationSchemeDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
