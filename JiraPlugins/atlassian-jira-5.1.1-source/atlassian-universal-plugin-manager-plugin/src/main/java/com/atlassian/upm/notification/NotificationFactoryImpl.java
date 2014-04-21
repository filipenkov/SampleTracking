package com.atlassian.upm.notification;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.util.Pair;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public class NotificationFactoryImpl implements NotificationFactory
{
    private final UserManager userManager;

    public NotificationFactoryImpl(UserManager userManager)
    {
        this.userManager = checkNotNull(userManager, "userManager");
    }

    public Notification getNotification(NotificationType type, String pluginKey, boolean dismissed)
    {
        return new Notification(type, pluginKey, getDismissedState(dismissed));
    }

    public NotificationCollection getNotifications(NotificationType type, Iterable<Pair<String, Boolean>> plugins, boolean typeDismissed)
    {
        ImmutableList.Builder<Notification> notifications = ImmutableList.builder();

        for (Pair<String, Boolean> plugin : plugins)
        {
            notifications.add(getNotification(type, plugin.first(), plugin.second()));
        }

        return new NotificationCollection(type, getDismissedState(typeDismissed), notifications.build());
    }

    private DismissedState getDismissedState(boolean dismissed)
    {
        return new DismissedState(userManager.getRemoteUsername(), dismissed);
    }
}
