package com.atlassian.jira.avatar;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A caching implementation of the AvatarStore.
 *
 * @since v4.2
 */
@EventComponent
public class CachingAvatarStore implements AvatarStore
{
    //discussed using an LRU map here, but since Avatar objects are quite lightweight it should be
    //ok to cache them all (potentially)
    private final Map<Long, Avatar> cache = new ConcurrentHashMap<Long, Avatar>();

    private final AvatarStore delegate;

    public CachingAvatarStore(final AvatarStore delegate)
    {
        this.delegate = delegate;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public Avatar getById(final Long avatarId) throws DataAccessException
    {
        notNull("avatarId", avatarId);

        Avatar avatar = cache.get(avatarId);
        if (avatar == null)
        {
            avatar = delegate.getById(avatarId);
            if (avatar != null)
            {
                cache.put(avatarId, avatar);
            }
        }
        return avatar;
    }

    public boolean delete(final Long avatarId) throws DataAccessException
    {
        notNull("avatarId", avatarId);

        try
        {
            return delegate.delete(avatarId);
        }
        finally
        {
            //regardless of what happens, clear this cache entry!
            cache.remove(avatarId);
        }
    }

    public void update(final Avatar avatar) throws DataAccessException
    {
        notNull("avatar", avatar);

        try
        {
            delegate.update(avatar);
        }
        finally
        {
            //since the update method doesn't return the newly stored avatar, simply clear
            //its cached value and *don't* cache the passed in avatar to avoid caching something inconsistent with the
            // db!
            if (avatar.getId() != null)
            {
                cache.remove(avatar.getId());
            }
        }
    }

    public Avatar create(final Avatar avatar) throws DataAccessException
    {
        notNull("avatar", avatar);

        final Avatar createdAvatar = delegate.create(avatar);
        cache.put(createdAvatar.getId(), createdAvatar);
        return createdAvatar;
    }

    public List<Avatar> getAllSystemAvatars(final Avatar.Type type) throws DataAccessException
    {
        //this method only gets called very infrequently so no need to cache this
        return delegate.getAllSystemAvatars(type);
    }

    public List<Avatar> getCustomAvatarsForOwner(final Avatar.Type type, final String ownerId)
            throws DataAccessException
    {
        //this method only gets called very infrequently so no need to cache this
        return delegate.getCustomAvatarsForOwner(type, ownerId);
    }
}
