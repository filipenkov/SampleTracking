package com.atlassian.jira.favourites;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.collect.LRUMap.synchronizedLRUMap;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Cache is keyed on username:type. This is very broken, but no more broken than any
 * of the other caches.
 *
 * @since v3.13
 */
@EventComponent
public class CachingFavouritesStore implements FavouritesStore
{
    /**
     * The actual cache. Randomly picked these numbers.
     */
    private final Map<Key, Collection<Long>> cache = synchronizedLRUMap(1000);

    private final FavouritesStore delegateStore;

    public CachingFavouritesStore(final FavouritesStore delegateStore)
    {
        this.delegateStore = notNull("delegateStore", delegateStore);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public boolean addFavourite(final User user, final SharedEntity entity)
    {
        try
        {
            return delegateStore.addFavourite(notNull("user", user), notNull("entity", entity));
        }
        finally
        {
            flushFavourites(user, entity.getEntityType());
        }
    }

    public boolean addFavourite(String username, SharedEntity entity)
    {
        try
        {
            return delegateStore.addFavourite(notNull("username", username), notNull("entity", entity));
        }
        finally
        {
            flushFavourites(username, entity.getEntityType());
        }
    }

    public boolean removeFavourite(final User user, final SharedEntity entity)
    {
        try
        {
            return delegateStore.removeFavourite(notNull("user", user), notNull("entity", entity));
        }
        finally
        {
            flushFavourites(user, entity.getEntityType());
        }
    }

    public boolean isFavourite(final User user, final SharedEntity entity)
    {
        notNull("user", user);
        notNull("entity", entity);

        final Collection<Long> ids = getFavouriteIds(user, entity.getEntityType());
        return ids.contains(entity.getId());
    }

    public Collection<Long> getFavouriteIds(final User user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        notNull("user", user);
        notNull("entityType", entityType);

        final Key key = new Key(user.getName(), entityType);
        Collection<Long> ids = cache.get(key);

        if (ids == null)
        {
            //This is the major break point. Since the map is not synchronized when we do the read
            //other threads may be able put a stale value in the cache. For instance, this line
            //could be executed at the same time as a delete which can leave ids in the cache that
            //don't exist in the database.

            ids = delegateStore.getFavouriteIds(user, entityType);
            if (ids == null)
            {
                ids = Collections.emptyList();
            }
            cache.put(key, ids);
        }

        return ids;
    }

    public void removeFavouritesForUser(final User user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        notNull("user", user);
        notNull("entityType", entityType);

        //We order it this way to ensure correctness.
        try
        {
            delegateStore.removeFavouritesForUser(user, entityType);
        }
        finally
        {
            flushFavourites(user, entityType);
        }
    }

    public void removeFavouritesForEntity(final SharedEntity entity)
    {
        try
        {
            delegateStore.removeFavouritesForEntity(entity);
        }
        finally
        {
            //loop through the cache and remove entries. We thought this may be quicker than
            //a flush and because I wanted to write some extra code.
            synchronized (cache)
            {
                for (final Iterator<Map.Entry<Key, Collection<Long>>> iterator = cache.entrySet().iterator(); iterator.hasNext();)
                {
                    final Map.Entry<Key, Collection<Long>> entry = iterator.next();
                    final Key entryKey = entry.getKey();
                    if (entryKey.getType().equals(entity.getEntityType()))
                    {
                        if (entry.getValue().contains(entity.getId()))
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    public void updateSequence(final User user, final List<? extends SharedEntity> favouriteEntities)
    {
        try
        {
            delegateStore.updateSequence(user, favouriteEntities);
        }
        finally
        {
            if (!favouriteEntities.isEmpty())
            {
                final SharedEntity entity = favouriteEntities.get(0);
                flushFavourites(user, entity.getEntityType());
            }
        }
    }

    private void flushFavourites(final User user, final SharedEntity.TypeDescriptor<?> typeDescriptor)
    {
        flushFavourites(user.getName(), typeDescriptor);
    }

    private void flushFavourites(final String username, final SharedEntity.TypeDescriptor<?> typeDescriptor)
    {
        cache.remove(new Key(username, typeDescriptor));
    }

    /**
     * Key object for the cache.
     *
     * @since v3.13
     */
    private static class Key
    {
        private final String username;
        private final SharedEntity.TypeDescriptor<?> type;

        public Key(final String username, final SharedEntity.TypeDescriptor<?> type)
        {
            Assertions.notBlank("username", username);
            Assertions.notNull("type", type);

            this.username = IdentifierUtils.toLowerCase(username);
            this.type = type;
        }

        public String getUsername()
        {
            return username;
        }

        public SharedEntity.TypeDescriptor<?> getType()
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
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final Key key = (Key) o;

            if (!type.equals(key.type))
            {
                return false;
            }
            if (!username.equals(key.username))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = username.hashCode();
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
