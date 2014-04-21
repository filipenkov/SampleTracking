package com.atlassian.upm.notification;

import java.util.Iterator;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

/**
 * An {@link Iterable} of {@link Notification}s where all {@link Notification}s are of the same {@link NotificationType}.
 */
public class NotificationCollection implements Iterable<Notification>
{
    private final NotificationType type;
    private final Iterable<Notification> notifications;
    private final DismissedState dismissedState;

    public NotificationCollection(NotificationType type, DismissedState dismissedState, Iterable<Notification> notifications)
    {
        this.type = checkNotNull(type, "type");
        this.notifications = ImmutableList.copyOf(filter(notifications, isExpectedType()));
        this.dismissedState = checkNotNull(dismissedState, "dismissedState");
    }

    @Override
    public Iterator<Notification> iterator()
    {
        return notifications.iterator();
    }

    public NotificationType getType()
    {
        return type;
    }

    public DismissedState getDismissedState()
    {
        return dismissedState;
    }

    public int getNotificationCount()
    {
        return size(notifications);
    }

    private Predicate<Notification> isExpectedType()
    {
        return new IsExpectedType();
    }

    private final class IsExpectedType implements Predicate<Notification>
    {
        @Override
        public boolean apply(Notification notification)
        {
            if (!notification.getType().equals(type))
            {
                throw new IllegalArgumentException("Expected all to be of type " + type
                    + ", found type " + notification.getType());
            }
            return true;
        }
    }
}
