package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.atlassian.jira.util.collect.LRUMap.newLRUMap;

/**
 * Caching store for {@link SearchRequest}. The cache stores a id -> SearchRequest and a searchrequest.owner -> id mapping. <p/> This
 * class is not completely thread safe in that it is possible for the cache and the database to become unsynchronized.
 *
 * @since v3.13
 */
public class CachingSearchRequestStore implements SearchRequestStore, Startable
{
    private final SearchRequestStore delegateStore;

    /**
     * This lock must be held when accessing either of the caches. It lets us keep them in sync.
     */
    private final Lock cacheLock = new ReentrantLock(false);

    /**
     * Stores searchrequest.owner -> set[searchrequest.id]
     */
    private final Map<String, Set<Long>> cacheByUser = newLRUMap(500);

    /**
     * Stores searchrequest.id -> searchrequest
     */
    private final Map<Long, SearchRequest> cacheById = newLRUMap(2000);
    private final EventPublisher eventPublisher;

    public CachingSearchRequestStore(final SearchRequestStore delegateStore, final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        Assertions.notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        try
        {
            cacheLock.lock();
            cacheById.clear();
            cacheByUser.clear();
        }
        finally
        {
            cacheLock.unlock();
        }
    }

    public Collection<SearchRequest> getAllRequests()
    {
        return delegateStore.getAllRequests();
    }

    public EnclosedIterable<SearchRequest> get(final RetrievalDescriptor descriptor)
    {
        return delegateStore.get(descriptor);
    }

    public EnclosedIterable<SearchRequest> getAll()
    {
        return delegateStore.getAll();
    }

    public EnclosedIterable<IndexableSharedEntity<SearchRequest>> getAllIndexableSharedEntities()
    {
        return delegateStore.getAllIndexableSharedEntities();
    }

    public Collection<SearchRequest> getAllOwnedSearchRequests(final User owner)
    {
        Assertions.notNull("owner", owner);
        Assertions.notNull("owner.username", owner.getName());

        final String ownerName = owner.getName();
        Collection<Long> ownedSearchRequestIds = null;
        cacheLock.lock();
        try
        {
            ownedSearchRequestIds = cacheByUser.get(ownerName);
        }
        finally
        {
            cacheLock.unlock();
        }

        final Collection<SearchRequest> returnPages;

        if (ownedSearchRequestIds == null)
        {
            final Collection<SearchRequest> requestsFromDatabase = delegateStore.getAllOwnedSearchRequests(owner);

            cacheLock.lock();
            try
            {
                if (requestsFromDatabase != null)
                {
                    ownedSearchRequestIds = new ArrayList<Long>(requestsFromDatabase.size());
                    returnPages = new ArrayList<SearchRequest>(requestsFromDatabase.size());

                    for (final SearchRequest searchRequest : requestsFromDatabase)
                    {
                        if (!cacheById.containsKey(searchRequest.getId()))
                        {
                            cacheById.put(searchRequest.getId(), searchRequest);
                        }

                        ownedSearchRequestIds.add(searchRequest.getId());
                        returnPages.add(copySearch(searchRequest));
                    }
                    cacheByUser.put(ownerName, new CopyOnWriteArraySet<Long>(ownedSearchRequestIds));
                }
                else
                {
                    returnPages = null;
                }
            }
            finally
            {
                cacheLock.unlock();
            }
        }
        else
        {
            returnPages = new ArrayList<SearchRequest>(ownedSearchRequestIds.size());
            for (final Long id : ownedSearchRequestIds)
            {
                final SearchRequest searchRequest = getSearchRequest(id);
                if (searchRequest != null)
                {
                    returnPages.add(searchRequest);
                }
            }
        }

        return returnPages;
    }

    public SearchRequest getRequestByAuthorAndName(final User author, final String name)
    {
        return delegateStore.getRequestByAuthorAndName(author, name);
    }

    public SearchRequest getSearchRequest(final Long searchRequestId)
    {
        Assertions.notNull("searchRequestId", searchRequestId);

        SearchRequest searchRequest;

        cacheLock.lock();
        try
        {
            searchRequest = cacheById.get(searchRequestId);
        }
        finally
        {
            cacheLock.unlock();
        }

        if (searchRequest == null)
        {
            searchRequest = delegateStore.getSearchRequest(searchRequestId);
            if (searchRequest != null)
            {
                cacheLock.lock();
                try
                {
                    addToCache(searchRequest);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }

        return copySearch(searchRequest);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    public SearchRequest create(final SearchRequest request)
    {
        Assertions.notNull("request", request);

        SearchRequest savedRequest = null;
        try
        {
            savedRequest = delegateStore.create(request);
        }
        finally
        {
            if (savedRequest != null)
            {
                cacheLock.lock();
                try
                {
                    addToCache(savedRequest);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }
        return copySearch(savedRequest);
    }

    public SearchRequest update(final SearchRequest request)
    {
        Assertions.notNull("request", request);
        Assertions.notNull("request.id", request.getId());

        SearchRequest returnRequest = null;

        try
        {
            returnRequest = delegateStore.update(request);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                updateCache(request.getId(), returnRequest);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
        return copySearch(returnRequest);
    }

    public SearchRequest adjustFavouriteCount(final Long searchRequestId, final int incrementValue)
    {
        Assertions.notNull("searchRequestId", searchRequestId);

        SearchRequest returnRequest = null;
        try
        {
            returnRequest = delegateStore.adjustFavouriteCount(searchRequestId, incrementValue);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                updateCache(searchRequestId, returnRequest);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
        return copySearch(returnRequest);
    }

    public void delete(final Long searchId)
    {
        Assertions.notNull("searchId", searchId);

        try
        {
            delegateStore.delete(searchId);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                removeFromCache(searchId);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
    }

    public EnclosedIterable<SearchRequest> getSearchRequests(final Project project)
    {
        return delegateStore.getSearchRequests(project);
    }

    public EnclosedIterable<SearchRequest> getSearchRequests(final Group group)
    {
        return delegateStore.getSearchRequests(group);
    }

    /**
     * Add the passed search request to the cache. This should only be called
     * when the lock is held on the cache.
     *
     * @param searchRequest the search request to add to the cache.
     */
    private void addToCache(final SearchRequest searchRequest)
    {
        cacheById.put(searchRequest.getId(), searchRequest);

        final String ownerName = searchRequest.getOwnerUserName();
        if (ownerName != null)
        {
            final Collection<Long> ids = cacheByUser.get(ownerName);
            if (ids != null)
            {
                ids.add(searchRequest.getId());
            }
        }
    }

    /**
     * Update the cache after the passed search request has been updated. Should only be called
     * with the cache lock held.
     *
     * @param requestId  the id of the search request updated.
     * @param newRequest the updated search request. Can be null if an error occurred.
     */
    private void updateCache(final Long requestId, final SearchRequest newRequest)
    {
        if (newRequest != null)
        {
            final SearchRequest cachedRequest = cacheById.get(requestId);
            final String cachedUserName = cachedRequest == null ? null : cachedRequest.getOwnerUserName();
            if ((cachedRequest != null) && (cachedUserName != null))
            {
                final String newUserName = newRequest.getOwnerUserName();
                if ((newUserName == null) || !cachedUserName.equals(newUserName))
                {
                    //username has changed
                    final Collection<Long> ids = cacheByUser.get(cachedUserName);
                    if (ids != null)
                    {
                        ids.remove(cachedRequest.getId());
                    }
                    addToCache(newRequest);
                }
                else
                {
                    //user has not changed, just replace in main cache.
                    cacheById.put(newRequest.getId(), newRequest);
                }
            }
            else
            {
                //we didn't add it to the user cache last time because it was null. Lets try an do that now.
                addToCache(newRequest);
            }
        }
        else
        {
            //update did not work. Lets clear the cache and go back to the database.
            removeFromCache(requestId);
        }
    }

    /**
     * Remove the search identified by the passed id from the cache. Should only be called while the cache
     * lock is held.
     *
     * @param searchId the id of the search to be removed from the cache.
     */
    private void removeFromCache(final Long searchId)
    {
        // need to loop over all users to find who has this search
        for (final Set<Long> ids : cacheByUser.values())
        {
            // only remove specified page rather than blow away entire entry
            ids.remove(searchId);
        }
        cacheById.remove(searchId);
    }

    /**
     * Make a copy of the search request that we can cache.
     *
     * @param searchRequest the search request to copy.
     *
     * @return the copied search request.
     */
    private SearchRequest copySearch(final SearchRequest searchRequest)
    {
        return searchRequest != null ? new SearchRequest(searchRequest) : null;
    }
}
