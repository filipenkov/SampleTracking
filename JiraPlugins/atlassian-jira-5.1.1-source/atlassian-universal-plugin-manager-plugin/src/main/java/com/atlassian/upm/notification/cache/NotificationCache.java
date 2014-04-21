package com.atlassian.upm.notification.cache;

import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.NotificationCollection;
import com.atlassian.upm.notification.NotificationType;

/**
 * Service to access and update notification data. 
 * Notification data is cached to prevent recalculations for every request.
 */
public interface NotificationCache
{
    /**
     * Returns all notifications of all types. Since this is non-specific for any given user,
     * none of the notifications are dismissed.
     * @return all notifications of all types
     */
    Iterable<NotificationCollection> getNotifications();

    /**
     * Returns notifications of all types for a given user.
     *
     * @param username the user
     * @param hideDismissed true if dismissed notifications should be hidden, false otherwise
     * @return notifications of all types for a given user
     */
    Iterable<NotificationCollection> getNotifications(String username, boolean hideDismissed);

    /**
     * Returns notifications of a specific type for a given user.
     *
     * @param type the notification type
     * @param username the user
     * @param hideDismissed true if dismissed notifications should be hidden, false otherwise
     * @return notifications of a specific type for a given user
     */
    NotificationCollection getNotifications(NotificationType type, String username, boolean hideDismissed);

    /**
     * Returns a single notification, if one exists, of the given notification type and plugin.
     *
     * @param type the notification type
     * @param username the user viewing the notification
     * @param pluginKey the plugin
     * @return a single notification, if one exists, of the given notification type and plugin
     */
    Option<Notification> getNotification(NotificationType type, String username, String pluginKey);

    /**
     * Returns true if all notifications of the given type have been dismissed by the given user.
     *
     * @param type the notification type
     * @param username the user
     * @return true if all notifications of the given type have been dismissed by the given user.
     */
    boolean isNotificationTypeDismissed(NotificationType type, String username);

    /**
     * Returns true if an individual notification has been dismissed by a user
     *
     * @param type the type of the notification
     * @param username the user
     * @param pluginKey the plugin of the notification
     * @return true if an individual notification has been dismissed by a user
     */
    boolean isNotificationDismissed(NotificationType type, String username, String pluginKey);

    /**
     * Sets the given plugins as being associated with the given notification type.
     *
     * @param type the notification type
     * @param pluginKeys the plugins
     */
    void setNotifications(NotificationType type, Iterable<String> pluginKeys);

    /**
     * Sets the dismissal state for the given notification type and user.
     *
     * @param type the notification type
     * @param username the user
     * @param dismissed the dismissal state
     */
    void setNotificationTypeDismissal(NotificationType type, String username, boolean dismissed);

    /**
     * Sets the dismissal state for the given individual notification (type + plugin key).
     *
     * @param type the notification type
     * @param username the user
     * @param pluginKey the plugin
     * @param dismissed the dismissal state
     */
    void setNotificationDismissal(NotificationType type, String username, String pluginKey, boolean dismissed);

    /**
     * Resets the dismissal state for the given notification type. Any users who had previously dismissed
     * this notification type will now have their dismissal state toggled. This does not affect individual notifications dismissals.
     *
     * @param type the notification type
     */
    void resetNotificationTypeDismissal(NotificationType type);

    /**
     * Resets the dismissal state for the given notification. Any users who had previously dismissed
     * this notification will now have their dismissal state toggled.
     *
     * @param type the notification type
     * @param pluginKey the plugin
     */
    void resetNotificationDismissal(NotificationType type, String pluginKey);
}