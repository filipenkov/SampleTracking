package com.atlassian.jira.user;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.LRUMap;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Nullable;
import com.atlassian.util.concurrent.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Caching implementation of {@link com.atlassian.jira.user.UserHistoryStore}.
 * This is very broken, but no more broken than any of the other caches.
 *
 * @since v4.0
 */
@EventComponent
public class CachingUserHistoryStore implements UserHistoryStore
{
    private static final int DEFAULT_MAX_THRESHOLD = 10;
    private static final int DEFAULT_MAX_ITEMS = 20;
    private static final Logger log = Logger.getLogger(CachingUserHistoryStore.class);

    /**
     * Lock on the user name.
     */
    private final Function<ApplicationUser, ManagedLock> lockManager = ManagedLocks.weakManagedLockFactory(new Function<ApplicationUser, String>()
    {
        public String get(final ApplicationUser input)
        {
            return input.getKey();
        }
    });

    private final OfBizUserHistoryStore delegatingStore;
    private final ApplicationProperties applicationProperties;
    private final Cache cache = new Cache();
    private final int maxThreshold;

    public CachingUserHistoryStore(@NotNull final OfBizUserHistoryStore delegatingStore, @NotNull final ApplicationProperties applicationProperties)
    {
        this(delegatingStore, applicationProperties, DEFAULT_MAX_THRESHOLD);
    }

    CachingUserHistoryStore(@NotNull final OfBizUserHistoryStore delegatingStore, @NotNull final ApplicationProperties applicationProperties,
            int maxThreshold)
    {
        this.delegatingStore = notNull("delegatingStore", delegatingStore);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.maxThreshold = maxThreshold;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public void addHistoryItem(@NotNull final ApplicationUser user, @NotNull final UserHistoryItem historyItem)
    {
        notNull("user", user);
        notNull("historyItem", historyItem);

        lockManager.get(user).withLock(new Runnable()
        {
            public void run()
            {
                final UserHistoryItem.Type type = historyItem.getType();
                final List<UserHistoryItem> history = cache.get(new Key(user, type));
                final int index = getIndexOfHistoryItem(history, historyItem);
                if (index == -1) // new item
                {
                    history.add(0, historyItem);
                    delegatingStore.addHistoryItemNoChecks(user, historyItem);

                    final int maxItems = getMaxItems(historyItem.getType(), applicationProperties);
                    // don't prune every time, wait until it gets MAX_THRESHOLD more than max
                    if (history.size() > maxItems + maxThreshold)
                    {
                        final Collection<String> entitiesToDelete = new ArrayList<String>();
                        // only keep first 50 issues.
                        while (history.size() > maxItems)
                        {
                            final UserHistoryItem item = history.remove(maxItems);
                            entitiesToDelete.add(item.getEntityId());
                        }
                        delegatingStore.expireOldHistoryItems(user, type, entitiesToDelete);
                    }
                }
                else
                {
                    // existing item
                    history.remove(index);
                    history.add(0, historyItem);
                    delegatingStore.updateHistoryItemNoChecks(user, historyItem);
                }
            }
        });
    }

    private int getIndexOfHistoryItem(@Nullable final List<UserHistoryItem> history, @Nullable final UserHistoryItem historyItem)
    {
        if (history != null)
        {
            for (int i = 0; i < history.size(); i++)
            {
                final UserHistoryItem currentHistoryItem = history.get(i);
                if (currentHistoryItem.getEntityId().equals(historyItem.getEntityId()) && currentHistoryItem.getType().equals(historyItem.getType()))
                {
                    return i;
                }
            }
        }
        return -1;
    }

    @NotNull
    public List<UserHistoryItem> getHistory(@NotNull final UserHistoryItem.Type type, @NotNull final ApplicationUser user)
    {
        notNull("user", user);
        notNull("type", type);
        return lockManager.get(user).withLock(new Supplier<List<UserHistoryItem>>()
        {
            public List<UserHistoryItem> get()
            {
                return unmodifiableList(cache.get(new Key(user, type)));
            }
        });
    }

    public Set<UserHistoryItem.Type> removeHistoryForUser(@NotNull final ApplicationUser user)
    {
        notNull("user", user);
        return lockManager.get(user).withLock(new Supplier<Set<UserHistoryItem.Type>>()
        {
            public Set<UserHistoryItem.Type> get()
            {
                final Set<UserHistoryItem.Type> typesRemoved = delegatingStore.removeHistoryForUser(user);
                for (final UserHistoryItem.Type type : typesRemoved)
                {
                    flushCache(type, user);
                }
                return unmodifiableSet(typesRemoved);
            }
        });
    }

    private void flushCache(final UserHistoryItem.Type type, final ApplicationUser user)
    {
        cache.remove(new Key(user, type));
    }

    public static int getMaxItems(final UserHistoryItem.Type type, final ApplicationProperties applicationProperties)
    {
        final String maxItemsForTypeStr = applicationProperties.getDefaultBackedString("jira.max." + type.getName() + ".history.items");
        final int maxItems = DEFAULT_MAX_ITEMS;
        try
        {
            if (StringUtils.isNotBlank(maxItemsForTypeStr))
            {
                return Integer.parseInt(maxItemsForTypeStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max." + type.getName() + ".history.items'.  Should be a number.");
        }

        final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
        try
        {
            if (StringUtils.isNotBlank(maxItemsStr))
            {
                return Integer.parseInt(maxItemsStr);
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
        }
        return maxItems;
    }

    /**
     * Get from the underlying store if not already cached.
     */
    private final class Cache implements Function<Key, List<UserHistoryItem>>
    {
        /**
         * The actual cache. Randomly picked these limits.
         * (User, History item type) -> List (User history Item)
         */
        private final Map<Key, List<UserHistoryItem>> map = LRUMap.synchronizedLRUMap(2000);

        public List<UserHistoryItem> get(final Key key)
        {
            if (!map.containsKey(key))
            {
                List<UserHistoryItem> history = delegatingStore.getHistory(key.type, key.user);
                if (history == null)
                {
                    history = new CopyOnWriteArrayList<UserHistoryItem>();
                }
                map.put(key, history);
            }
            return map.get(key);
        }

        public void remove(final Key key)
        {
            map.remove(key);
        }

        public void clear()
        {
            map.clear();
        }
    }

    /**
     * Key object for the cache.
     *
     * @since v4.0
     */
    private static final class Key
    {
        private final ApplicationUser user;
        private final UserHistoryItem.Type type;

        public Key(final ApplicationUser user, final UserHistoryItem.Type type)
        {
            notNull("user", user);
            notNull("type", type);

            this.user = user;
            this.type = type;
        }

        public String getUserKey()
        {
            return user.getKey();
        }

        public UserHistoryItem.Type getType()
        {
            return type;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }

            final Key key = (Key) o;

            if (!type.equals(key.type))
            {
                return false;
            }
            return getUserKey().equals(key.getUserKey());
        }

        @Override
        public int hashCode()
        {
            int result;
            result = getUserKey().hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }
}
