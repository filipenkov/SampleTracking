package com.atlassian.upm.notification.rest.representations;

import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.NotificationCollection;

/**
 * Factory to build json notification representations for a given user.
 */
public interface NotificationRepresentationFactory
{
    NotificationGroupCollectionRepresentation getNotificationGroupCollection(Iterable<NotificationCollection> notificationCollections, Option<String> username);

    NotificationGroupRepresentation getNotificationGroup(NotificationCollection notificationCollection, Option<String> username);

    NotificationRepresentation getNotification(Notification notification, Option<String> username);
}
