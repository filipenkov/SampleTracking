package com.atlassian.upm.notification.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.upm.notification.NotificationType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Json representation for a group of notifications all of the same type.
 */
public class NotificationGroupRepresentation
{
    @JsonProperty private final String notificationType;
    @JsonProperty private final Collection<NotificationRepresentation> notifications;
    @JsonProperty private final int notificationCount;
    @JsonProperty private final Boolean dismissed;
    @JsonProperty private final String title;
    @JsonProperty private final String message;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    public NotificationGroupRepresentation(
        @JsonProperty("notificationType") String notificationType,
        @JsonProperty("notifications") Collection<NotificationRepresentation> notifications,
        @JsonProperty("dismissed") Boolean dismissed,
        @JsonProperty("title") String title,
        @JsonProperty("message") String message,
        @JsonProperty("links") Map<String, URI> links)
    {
        this(NotificationType.fromKey(notificationType), notifications, dismissed, title, message, links);
    }

    public NotificationGroupRepresentation(
        NotificationType notificationType,
        Collection<NotificationRepresentation> notifications,
        Boolean dismissed,
        String title,
        String message,
        Map<String, URI> links)
    {
        this.notificationType = checkNotNull(notificationType, "notificationType").getKey();
        this.notifications = ImmutableList.copyOf(notifications);
        this.notificationCount = notifications.size();
        this.dismissed = dismissed; //will be null if not user-specific
        this.title = checkNotNull(title, "title");
        this.message = checkNotNull(message, "message");
        this.links = ImmutableMap.copyOf(links);
    }

    public String getNotificationType()
    {
        return notificationType;
    }

    public Collection<NotificationRepresentation> getNotifications()
    {
        return notifications;
    }

    public int getNotificationCount()
    {
        return notificationCount;
    }

    public Boolean isDismissed()
    {
        return dismissed;
    }

    public String getTitle()
    {
        return title;
    }

    public String getMessage()
    {
        return message;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }
}
