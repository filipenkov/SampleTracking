package com.atlassian.jira.sharing;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Map;

import static com.atlassian.jira.util.collect.LRUMap.synchronizedLRUMap;

/**
 * Cache is key on entityId:type. This is very broken, but no more broken than and
 * of the other caches.
 *
 * @since v3.13
 */
public class CachingSharePermissionStore implements SharePermissionStore, Startable
{
    private final SharePermissionStore delegateStore;

    /**
     * The actual cache. Randomly picked these numbers.
     */
    private final Map<Key, SharePermissions> cache = synchronizedLRUMap(3000);
    private final EventPublisher eventPublisher;

    public CachingSharePermissionStore(final SharePermissionStore delegateStore, final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        Assertions.notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public SharePermissions getSharePermissions(final SharedEntity entity)
    {
        validate(entity);

        final Key key = CachingSharePermissionStore.createKey(entity);
        SharePermissions sharePermissions = cache.get(key);
        if (sharePermissions == null)
        {
            //This is the major break point. Since the map is not synchronized when we do the read
            //other threads may be able put a stale value in the cache. For instance, this line
            //could be executed at the same time as a delete which can leave ids in the cache that
            //don't exist in the database.
            sharePermissions = delegateStore.getSharePermissions(entity);
            if (sharePermissions == null)
            {
                sharePermissions = SharePermissions.PRIVATE;
            }
            cache.put(key, sharePermissions);
        }

        return sharePermissions;
    }

    public int deleteSharePermissions(final SharedEntity entity)
    {

        validate(entity);
        try
        {
            return delegateStore.deleteSharePermissions(entity);
        }
        finally
        {
            cache.remove(createKey(entity));
        }
    }

    public int deleteSharePermissionsLike(final SharePermission permission)
    {
        Assertions.notNull("permission", permission);
        Assertions.notNull("permission.type", permission.getType());
        try
        {
            return delegateStore.deleteSharePermissionsLike(permission);
        }
        finally
        {
            cache.clear();
        }
    }

    public SharePermissions storeSharePermissions(final SharedEntity entity)
    {
        validate(entity);
        Assertions.notNull("permissions", entity.getPermissions());

        boolean addedToCache = false;
        try
        {
            SharePermissions sharePermissions = delegateStore.storeSharePermissions(entity);
            if (sharePermissions == null)
            {
                sharePermissions = SharePermissions.PRIVATE;
            }

            cache.put(createKey(entity), sharePermissions);
            addedToCache = true;

            return sharePermissions;
        }
        finally
        {
            if (!addedToCache)
            {
                cache.remove(createKey(entity));
            }
        }
    }

    private void validate(final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity.id", entity.getId());
        Assertions.notNull("entity.entityType", entity.getEntityType());
    }

    private static Key createKey(final SharedEntity entity)
    {
        return new Key(entity.getId().longValue(), entity.getEntityType().getName());
    }

    /**
     * Key object for the cache.
     */
    private static class Key
    {
        private final long id;
        private final String type;

        public Key(final long id, final String type)
        {
            Assertions.notBlank("type", type);

            this.id = id;
            this.type = type;
        }

        ///CLOVER:OFF
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

            if (id != key.id)
            {
                return false;
            }
            if (!type.equals(key.type))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = (int) (id ^ (id >>> 32));
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
        ///CLOVER:ON
    }
}
