package com.atlassian.upm.notification;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a single notification associated with a unique pair of notification type and plugin key.
 */
public class Notification
{
    private final NotificationType type;
    private final String pluginKey;
    private final DismissedState dismissedState;

    Notification(NotificationType type, String pluginKey, DismissedState dismissedState)
    {
        this.type = checkNotNull(type, "type");
        this.pluginKey = checkNotNull(pluginKey, "pluginKey");
        this.dismissedState = checkNotNull(dismissedState, "dismissedState");
    }

    public NotificationType getType()
    {
        return type;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public DismissedState getDismissedState()
    {
        return dismissedState;
    }

    public static Function<Notification, String> toNotificationPluginKey()
    {
        return new Function<Notification, String>()
        {
            @Override
            public String apply(Notification notification)
            {
                return notification.getPluginKey();
            }
        };
    }

    @Override
    public boolean equals(Object o)
    {
        return o != null
            && o instanceof Notification
            && ((Notification)o).getPluginKey().equals(getPluginKey());
    }

    @Override
    public String toString()
    {
        return "Notification<" + getPluginKey() + ">";
    }
}
