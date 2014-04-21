package com.atlassian.upm.notification;

import com.atlassian.upm.api.util.Pair;

/**
 * Factory to build notifications.
 */
public interface NotificationFactory
{
    /**
     * Returns a single {@link Notification} for a given notification type and plugin.
     *
     * @param type the notification type
     * @param pluginKey the plugin
     * @param dismissed true if the notification is dismissed, false if not
     * @return a single {@link Notification} for a given notification type and plugin
     */
    Notification getNotification(NotificationType type, String pluginKey, boolean dismissed);

    /**
     * Returns a collection of {@link Notification}s for a given notification type.
     * @param type the notification type
     * @param plugins an {@link Iterable} or plugin keys and dismissal states
     * @param typeDismissed true if the entire type has been dismissed (overrules individual plugin dismissal states), false otherwise
     * @returna collection of {@link Notification}s for a given notification type
     */
    NotificationCollection getNotifications(NotificationType type, Iterable<Pair<String, Boolean>> plugins, boolean typeDismissed);
}
