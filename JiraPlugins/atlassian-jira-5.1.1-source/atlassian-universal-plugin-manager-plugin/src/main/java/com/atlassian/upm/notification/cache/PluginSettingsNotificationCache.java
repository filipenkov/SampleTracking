package com.atlassian.upm.notification.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.impl.NamespacedPluginSettings;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.NotificationCollection;
import com.atlassian.upm.notification.NotificationFactory;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;
import static com.atlassian.upm.notification.Notification.toNotificationPluginKey;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * {@link PluginSettings}-backed implementation for {@link NotificationCache}. This cache uses {@link ReadWriteLock} to
 * ensure threadsafe writes and performant reads.
 */
public class PluginSettingsNotificationCache implements NotificationCache
{
    private static final String KEY_PREFIX = "com.atlassian.upm:notifications:";
    private static final String KEY_NOTIFICATION_PREFIX = "notification-";
    private static final String KEY_DISMISSAL_PREFIX = "dismissal-";

    private final ManagedLock.ReadWrite lock = ManagedLocks.manageReadWrite(new ReentrantReadWriteLock());

    private final PluginSettingsFactory pluginSettingsFactory;
    private final NotificationFactory notificationFactory;

    public PluginSettingsNotificationCache(PluginSettingsFactory pluginSettingsFactory, NotificationFactory notificationFactory)
    {
        this.pluginSettingsFactory = checkNotNull(pluginSettingsFactory, "pluginSettingsFactory");
        this.notificationFactory = checkNotNull(notificationFactory, "notificationFactory");
    }

    private PluginSettings getPluginSettings()
    {
        //never cache our plugin settings
        return new NamespacedPluginSettings(pluginSettingsFactory.createGlobalSettings(), KEY_PREFIX);
    }

    @Override
    public Option<Notification> getNotification(NotificationType type, String username, String pluginKey)
    {
        //iterate over all notifications of this type - even dismissed notifications
        for (Notification notification : getNotifications(type, username, false))
        {
            if (notification.getPluginKey().equals(pluginKey))
            {
                return some(notification);
            }
        }

        return none(Notification.class);
    }

    @Override
    public Iterable<NotificationCollection> getNotifications()
    {
        //all notifications (including dismissed) for an anonymous user
        return getNotifications(null, false);
    }

    @Override
    public Iterable<NotificationCollection> getNotifications(final String username, final boolean hideDismissed)
    {
        return lock.read().withLock(new Supplier<Iterable<NotificationCollection>>()
        {
            @Override
            public Iterable<NotificationCollection> get()
            {
                ImmutableList.Builder<NotificationCollection> collections = ImmutableList.builder();
                for (NotificationType type : NotificationType.values())
                {
                    NotificationCollection notifications = getNotifications(type, username, hideDismissed);
                    if (notifications.getNotificationCount() > 0)
                    {
                        collections.add(notifications);
                    }
                }

                return collections.build();
            }
        });
    }

    @Override
    public NotificationCollection getNotifications(final NotificationType type, final String username, final boolean hideDismissed)
    {
        return lock.read().withLock(new Supplier<NotificationCollection>()
        {
            @Override
            public NotificationCollection get()
            {
                boolean typeDismissed = isNotificationTypeDismissed(type, username);

                Object storedValue = getPluginSettings().get(getNotificationKey(type));

                //return an empty notification if we're hiding dismissed notifications or no notifications exist
                if ((typeDismissed && hideDismissed) || storedValue == null || !(storedValue instanceof List) || ((List)storedValue).isEmpty())
                {
                    return notificationFactory.getNotifications(type, ImmutableList.<Pair<String, Boolean>>of(), typeDismissed);
                }

                Iterable<String> pluginKeys = (Iterable<String>) storedValue;
                if (hideDismissed)
                {
                    //since we're hiding dismissed notifications, let's remove plugins which have been individually dismissed
                    pluginKeys = filter(pluginKeys, not(getNotificationDismissalPredicate(type, username)));
                }

                Iterable<Pair<String, Boolean>> pluginDismissalPairs = transform(pluginKeys, getPluginDismissalPairs(type, username, typeDismissed));
                return notificationFactory.getNotifications(type, pluginDismissalPairs, typeDismissed);
            }
        });
    }

    private Function<String, Pair<String, Boolean>> getPluginDismissalPairs(NotificationType type, String username, boolean typeDismissed)
    {
        return new GetPluginDismissalPairs(type, username, typeDismissed);
    }

    private final class GetPluginDismissalPairs implements Function<String, Pair<String, Boolean>>
    {
        private final NotificationType type;
        private final String username;
        private final boolean typeDismissed;

        GetPluginDismissalPairs(NotificationType type, String username, boolean typeDismissed)
        {
            this.type = checkNotNull(type, "type");
            this.username = username; //user will be null if getting all notifications for the system
            this.typeDismissed = typeDismissed;
        }

        @Override
        public Pair<String, Boolean> apply(String pluginKey)
        {
            boolean notificationDismissed = isNotificationDismissed(type, username, pluginKey);
            return Pair.pair(pluginKey, notificationDismissed || typeDismissed);
        }
    }

    private Predicate<String> getNotificationDismissalPredicate(NotificationType type, String username)
    {
        return new NotificationDismissalPredicate(type, username);
    }

    private final class NotificationDismissalPredicate implements Predicate<String>
    {
        private final NotificationType type;
        private final String username;

        NotificationDismissalPredicate(NotificationType type, String username)
        {
            this.type = checkNotNull(type, "type");
            this.username = checkNotNull(username, "username");
        }

        @Override
        public boolean apply(String pluginKey)
        {
            return isNotificationDismissed(type, username, pluginKey);
        }
    }

    @Override
    public boolean isNotificationTypeDismissed(NotificationType type, String username)
    {
        return getStoredDismissedValues(getDismissalKey(type), username);
    }

    @Override
    public boolean isNotificationDismissed(NotificationType type, String username, String pluginKey)
    {
        return getStoredDismissedValues(getDismissalKey(type, pluginKey), username);
    }

    private boolean getStoredDismissedValues(final String pluginSettingsKey, final String username)
    {
        return lock.read().withLock(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                Object storedValue = getPluginSettings().get(pluginSettingsKey);

                if (username == null || storedValue == null || !(storedValue instanceof List) || ((List)storedValue).isEmpty())
                {
                    return false;
                }

                return contains((Iterable<String>) storedValue, username);
            }
        });
    }

    @Override
    public void setNotifications(final NotificationType type, final Iterable<String> pluginKeys)
    {
        lock.write().withLock(new Runnable()
        {
            @Override
            public void run()
            {
                NotificationCollection previousNotifications = getNotifications(type, null, false);
                getPluginSettings().put(getNotificationKey(type), newArrayList(pluginKeys));

                //reset dismissed notifications upon new plugins being added to the notification type
                boolean resetTypeDismissed = false;
                for (String pluginKey : pluginKeys)
                {
                    if (!contains(transform(previousNotifications, toNotificationPluginKey()), pluginKey))
                    {
                        resetTypeDismissed = true;
                        break;
                    }
                }

                if (resetTypeDismissed)
                {
                    resetNotificationTypeDismissal(type);
                    for (Notification previousNotification : previousNotifications)
                    {
                        //rather than leaving all sorts of plugin settings fragments in the db, let's delete it
                        getPluginSettings().remove(getDismissalKey(type, previousNotification.getPluginKey()));
                    }
                }
            }
        });
    }

    @Override
    public void setNotificationTypeDismissal(NotificationType type, String username, boolean dismissed)
    {
        setNotificationDismissal(getDismissalKey(type), username, dismissed);
    }

    @Override
    public void setNotificationDismissal(NotificationType type, String username, String pluginKey, boolean dismissed)
    {
        setNotificationDismissal(getDismissalKey(type, pluginKey), username, dismissed);
    }

    @Override
    public void resetNotificationTypeDismissal(final NotificationType type)
    {
        lock.write().withLock(new Runnable()
        {
            @Override
            public void run()
            {
                getPluginSettings().put(getDismissalKey(type), newArrayList());
            }
        });
    }

    @Override
    public void resetNotificationDismissal(final NotificationType type, final String pluginKey)
    {
        lock.write().withLock(new Runnable()
        {
            @Override
            public void run()
            {
                getPluginSettings().put(getDismissalKey(type, pluginKey), newArrayList());
            }
        });
    }

    private void setNotificationDismissal(final String pluginSettingsKey, final String username, final boolean dismissed)
    {
        lock.write().withLock(new Runnable()
        {
            @Override
            public void run()
            {
                Object storedValue = getPluginSettings().get(pluginSettingsKey);

                List<String> usernames = (storedValue == null) ? new ArrayList<String>() : (List<String>)storedValue;
                if (dismissed && !usernames.contains(username))
                {
                    //add the user to the dismissed list
                    usernames.add(username);
                    getPluginSettings().put(pluginSettingsKey, usernames);
                }
                else if (!dismissed && usernames.contains(username))
                {
                    //remove the user from the dismissed list
                    usernames.remove(username);
                    if (usernames.isEmpty())
                    {
                        getPluginSettings().remove(pluginSettingsKey);
                    }
                    else
                    {
                        getPluginSettings().put(pluginSettingsKey, usernames);
                    }
                }
            }
        });
    }

    private String getNotificationKey(NotificationType type)
    {
        return KEY_NOTIFICATION_PREFIX + type.getKey();
    }

    private String getDismissalKey(NotificationType type)
    {
        return KEY_DISMISSAL_PREFIX + type.getKey();
    }

    private String getDismissalKey(NotificationType type, String pluginKey)
    {
        return KEY_DISMISSAL_PREFIX + type.getKey() + ":" + pluginKey;
    }
}
