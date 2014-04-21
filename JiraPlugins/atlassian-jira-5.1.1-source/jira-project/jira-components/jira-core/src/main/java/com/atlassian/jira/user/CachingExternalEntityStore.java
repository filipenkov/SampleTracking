package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching wrapper around the delegateEntityStore.
 */
@EventComponent
public class CachingExternalEntityStore implements ExternalEntityStore
{
    protected final ConcurrentHashMap<String, Long> nameIdMap = new ConcurrentHashMap<String, Long>();
    private final ExternalEntityStore delegateEntityStore;

    /**
     * Creates a new instance of this class wrapping and caching the given {@link ExternalEntityStore}
     *
     * @param delegateEntityStore ExternalEntityStore to cache and delegate to
     *
     */
    public CachingExternalEntityStore(ExternalEntityStore delegateEntityStore)
    {
        this.delegateEntityStore = delegateEntityStore;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        nameIdMap.clear();
    }

    /**
     * Checks the cache first and if does not exist in the cache, delegates to the wrapped entity store.
     *
     * @param name profile name to create
     * @return Long id, this is the created or existing id for the name.
     * @throws IllegalArgumentException if the given name is null
     */
    public Long createIfDoesNotExist(String name) throws IllegalArgumentException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("ExternalEntity user name must not be null.");
        }
        final String lowercaseName = IdentifierUtils.toLowerCase(name);
        Long idForName = nameIdMap.get(lowercaseName);
        if (idForName == null)
        {
            idForName = delegateEntityStore.createIfDoesNotExist(name);
            nameIdMap.putIfAbsent(lowercaseName, idForName);
        }
        return idForName;
    }

}
