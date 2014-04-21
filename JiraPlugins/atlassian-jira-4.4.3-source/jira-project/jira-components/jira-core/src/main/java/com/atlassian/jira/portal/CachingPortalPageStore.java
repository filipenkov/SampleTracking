package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.atlassian.jira.util.collect.LRUMap.newLRUMap;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching store for {@link com.atlassian.jira.portal.PortalPage}. The cache stores a id -> PortalPage and a
 * portalPage.owner -> id mapping. <p/> This class is not completely thread safe in that it is possible for the cache
 * and the database to become unsynchronized.
 *
 * @since v3.13
 */
public class CachingPortalPageStore implements PortalPageStore, Startable
{
    private final PortalPageStore delegateStore;

    /**
     * This lock must be held when accessing either of the caches. It lets us keep them in sync.
     */
    private final Lock cacheLock = new ReentrantLock(false);

    /**
     * Stores portalPage.owner -> list[portalPage.id]
     */
    private final Map<String, List<Long>> cacheByUser = newLRUMap(500);

    /**
     * Stores portalPage.id -> portalPage
     */
    private final Map<Long, PortalPage> cacheById = newLRUMap(1000);

    /**
     * The id of the System Default Portal Page.
     */
    private volatile Long systemDefaultPortalPageId = null;
    private final EventPublisher eventPublisher;

    public CachingPortalPageStore(final PortalPageStore delegateStore, final EventPublisher eventPublisher)
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
        flush();
    }

    public EnclosedIterable<PortalPage> get(final RetrievalDescriptor ids)
    {
        return delegateStore.get(ids);
    }

    public EnclosedIterable<PortalPage> getAll()
    {
        return delegateStore.getAll();
    }

    public PortalPage getSystemDefaultPortalPage()
    {
        // this can be called by two threads at the same time. They should both return the same value so
        // we don't need to stop them.
        if (systemDefaultPortalPageId == null)
        {
            final PortalPage page = delegateStore.getSystemDefaultPortalPage();
            if (page != null)
            {
                systemDefaultPortalPageId = page.getId();
            }
        }
        if (systemDefaultPortalPageId != null)
        {
            return getPortalPage(systemDefaultPortalPageId);
        }
        else
        {
            return null;
        }
    }

    public Collection<PortalPage> getAllOwnedPortalPages(final User owner)
    {
        Assertions.notNull("owner", owner);
        Assertions.notNull("owner.username", owner.getName());

        final String ownerName = owner.getName();
        List<Long> ownedPageIds = null;
        cacheLock.lock();
        try
        {
            ownedPageIds = cacheByUser.get(ownerName);
        }
        finally
        {
            cacheLock.unlock();
        }

        final List<PortalPage> returnPages;

        if (ownedPageIds == null)
        {
            final Collection<PortalPage> pagesFromDatabase = delegateStore.getAllOwnedPortalPages(owner);

            cacheLock.lock();
            try
            {
                if (pagesFromDatabase != null)
                {
                    ownedPageIds = new ArrayList<Long>(pagesFromDatabase.size());
                    returnPages = new ArrayList<PortalPage>(pagesFromDatabase.size());

                    for (final PortalPage portalPage : pagesFromDatabase)
                    {
                        if (!cacheById.containsKey(portalPage.getId()))
                        {
                            cacheById.put(portalPage.getId(), portalPage);
                        }

                        ownedPageIds.add(portalPage.getId());
                        returnPages.add(portalPage);
                    }
                    cacheByUser.put(ownerName, ownedPageIds);
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
            returnPages = new ArrayList<PortalPage>(ownedPageIds.size());
            for (final Long id : ownedPageIds)
            {
                final PortalPage portalPage = getPortalPage(id);
                if (portalPage != null)
                {
                    returnPages.add(portalPage);
                }
            }
        }

        return returnPages;
    }

    public PortalPage getPortalPageByOwnerAndName(final User owner, final String portalPageName)
    {
        // We let this pass directly through to the store as this is not used very often.
        return delegateStore.getPortalPageByOwnerAndName(owner, portalPageName);
    }

    public PortalPage getPortalPage(final Long portalPageId)
    {
        Assertions.notNull("portalPageId", portalPageId);

        PortalPage portalPage;

        cacheLock.lock();
        try
        {
            portalPage = cacheById.get(portalPageId);
        }
        finally
        {
            cacheLock.unlock();
        }

        if (portalPage == null)
        {
            portalPage = delegateStore.getPortalPage(portalPageId);
            if (portalPage != null)
            {
                cacheLock.lock();
                try
                {
                    cacheById.put(portalPage.getId(), portalPage);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }

        return portalPage;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    public PortalPage create(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.name", portalPage.getName());
        Assertions.notNull("portalPage.ownerusername", portalPage.getOwnerUserName());

        PortalPage returnPage = null;
        try
        {
            returnPage = delegateStore.create(portalPage);
        }
        finally
        {
            if (returnPage != null)
            {
                cacheLock.lock();
                try
                {
                    cacheByUser.remove(portalPage.getOwnerUserName());
                    cacheById.put(returnPage.getId(), returnPage);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }
        return returnPage;
    }

    public PortalPage update(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.id", portalPage.getId());
        final String newOwnerUserName = portalPage.getOwnerUserName();
        if (!portalPage.isSystemDefaultPortalPage())
        {
            Assertions.notNull("portalPage.ownerusername", newOwnerUserName);
        }

        PortalPage returnPage = null;

        try
        {
            returnPage = delegateStore.update(portalPage);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                if (returnPage != null)
                {
                    // JRADEV-6810 Updating owner doesn't get reflected in UI
                    if (!portalPage.isSystemDefaultPortalPage())
                    {
                        String oldOwnerUserName = cacheById.get(returnPage.getId()).getOwnerUserName();
                        if (!newOwnerUserName.equals(oldOwnerUserName))
                        {
                            cacheByUser.remove(oldOwnerUserName);
                            cacheByUser.remove(newOwnerUserName);
                        }
                    }
                    cacheById.put(returnPage.getId(), returnPage);
                }
                else
                {
                    cacheById.remove(portalPage.getId());
                    if (!portalPage.isSystemDefaultPortalPage())
                    {
                        cacheByUser.remove(portalPage.getOwnerUserName());
                    }
                }
            }
            finally
            {
                cacheLock.unlock();
            }
        }
        return returnPage;
    }

    public boolean updatePortalPageOptimisticLock(final Long portalPageId, final Long currentVersion)
    {
        Assertions.notNull("portalPageId", portalPageId);
        Assertions.notNull("currentVersion", currentVersion);

        try
        {
            return delegateStore.updatePortalPageOptimisticLock(portalPageId, currentVersion);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                cacheById.remove(portalPageId);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
    }

    public PortalPage adjustFavouriteCount(final SharedEntity portalPage, final int incrementValue)
    {
        notNull("portalPage", portalPage);
        notNull("portalPage.id", portalPage.getId());

        PortalPage returnPage = null;
        try
        {
            returnPage = delegateStore.adjustFavouriteCount(portalPage, incrementValue);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                if (returnPage != null)
                {
                    cacheById.put(returnPage.getId(), returnPage);
                }
                else
                {
                    cacheById.remove(portalPage.getId());
                    if (portalPage.getOwnerUserName() != null)
                    {
                        cacheByUser.remove(portalPage.getOwnerUserName());
                    }
                }

            }
            finally
            {
                cacheLock.unlock();
            }
        }

        return returnPage;
    }

    public void delete(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);
        try
        {
            delegateStore.delete(portalPageId);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                // need to loop over all users to find who has this portal
                for (final Collection<Long> ids : cacheByUser.values())
                {
                    // only remove specified page rather than blow away entire entry
                    ids.remove(portalPageId);
                }
                cacheById.remove(portalPageId);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
    }

    public void flush()
    {
        cacheLock.lock();
        try
        {
            cacheById.clear();
            cacheByUser.clear();
        }
        finally
        {
            cacheLock.unlock();
        }
    }
}
