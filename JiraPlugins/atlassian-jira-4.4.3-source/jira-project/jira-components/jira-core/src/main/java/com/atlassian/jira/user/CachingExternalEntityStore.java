package com.atlassian.jira.user;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A caching wrapper around the delegateEntityStore.
 */
public class CachingExternalEntityStore implements ExternalEntityStore, Startable
{
    protected final ConcurrentHashMap<String, Long> nameIdMap = new ConcurrentHashMap<String, Long>();
    private final ExternalEntityStore delegateEntityStore;
    private final EventPublisher eventPublisher;

    /**
     * Creates a new instance of this class wrapping and caching the given {@link ExternalEntityStore}
     *
     * @param delegateEntityStore ExternalEntityStore to cache and delegate to
     * @param eventPublisher
     */
    public CachingExternalEntityStore(ExternalEntityStore delegateEntityStore, final EventPublisher eventPublisher)
    {
        this.delegateEntityStore = delegateEntityStore;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
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
        Long idForName = nameIdMap.get(name);
        if (idForName == null)
        {
            idForName = delegateEntityStore.createIfDoesNotExist(name);
            nameIdMap.putIfAbsent(name, idForName);
        }
        return idForName;
    }

}
