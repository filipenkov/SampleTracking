package com.atlassian.jira.portal;

import com.atlassian.event.api.EventListener;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.atlassian.jira.util.collect.LRUMap.newLRUMap;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching store for Portlet Configurations. The cache stores a id -> PortletConfigurationStore and a portalPage.id ->
 * id mapping.
 * <p/>
 * This class is not completely thread safe in that it is possible for the cache and the database to become
 * unsynchronized.
 *
 * @since 3.13
 */
@EventComponent
public class CachingPortletConfigurationStore implements FlushablePortletConfigurationStore
{
    private final PortletConfigurationStore delegateStore;

    /**
     * This lock must be held when accessing either of the caches. It lets us keep them in sync.
     */
    private final Lock cacheLock = new ReentrantLock(false);

    /**
     * Stores portalPage.id -> list[configuration.id]
     */
    private final Map<Long, List<Long>> cacheByPageId = newLRUMap(1000);

    /**
     * Stores configuration.id -> configuration.
     */
    private final Map<Long, PortletConfiguration> cacheById = newLRUMap(4000);

    public CachingPortletConfigurationStore(final PortletConfigurationStore delegateStore)
    {
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        try
        {
            cacheLock.lock();
            cacheByPageId.clear();
            cacheById.clear();
        }
        finally 
        {
            cacheLock.unlock();
        }
    }

    public List<PortletConfiguration> getByPortalPage(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        cacheLock.lock();
        List<Long> configIds = null;
        try
        {
            configIds = cacheByPageId.get(portalPageId);
        }
        finally
        {
            cacheLock.unlock();
        }

        final List<PortletConfiguration> returnList;
        if (configIds == null)
        {
            final List<PortletConfiguration> configsFromDatabase = delegateStore.getByPortalPage(portalPageId);

            cacheLock.lock();
            try
            {
                if (configsFromDatabase != null)
                {
                    configIds = new ArrayList<Long>(configsFromDatabase.size());
                    returnList = new ArrayList<PortletConfiguration>(configsFromDatabase.size());

                    for (final PortletConfiguration portletConfiguration : configsFromDatabase)
                    {
                        if (!cacheById.containsKey(portletConfiguration.getId()))
                        {
                            putInCacheById(portletConfiguration);
                        }

                        configIds.add(portletConfiguration.getId());
                        returnList.add(copyConfiguration(portletConfiguration));
                    }
                }
                else
                {
                    configIds = Collections.emptyList();
                    returnList = Collections.emptyList();
                }

                cacheByPageId.put(portalPageId, configIds);
            }
            finally
            {
                cacheLock.unlock();
            }
        }
        else
        {
            returnList = new ArrayList<PortletConfiguration>(configIds.size());
            for (final Object element : configIds)
            {
                final Long id = (Long) element;
                final PortletConfiguration portletConfiguration = getByPortletId(id);
                if (portletConfiguration != null)
                {
                    returnList.add(portletConfiguration);
                }
            }
        }

        return returnList;
    }    

    public PortletConfiguration getByPortletId(final Long portletId)
    {
        notNull("portletId", portletId);

        PortletConfiguration portletConfiguration;
        cacheLock.lock();
        try
        {
            portletConfiguration = cacheById.get(portletId);
        }
        finally
        {
            cacheLock.unlock();
        }

        if (portletConfiguration == null)
        {
            portletConfiguration = delegateStore.getByPortletId(portletId);
            if (portletConfiguration != null)
            {
                cacheLock.lock();
                try
                {
                    putInCacheById(portletConfiguration);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }

        return copyConfiguration(portletConfiguration);
    }

    public void delete(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());
        try
        {
            delegateStore.delete(pc);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                removePageFromCache(pc);
                cacheById.remove(pc.getId());
            }
            finally
            {
                cacheLock.unlock();
            }
        }
    }

    public void updateGadgetPosition(final Long gadgetId, final int row, final int column, final Long dashboardId)
    {
        notNull("gadgetId", gadgetId);
        notNull("dashboardId", dashboardId);

        cacheLock.lock();
        try
        {
            final PortletConfiguration portletConfiguration = cacheById.remove(gadgetId);
            Long existingDashboardId = portletConfiguration == null ? null : portletConfiguration.getDashboardPageId();
            //if the portletConfiguration wasn't cached previously, then look it up in the delegate store.
            if(existingDashboardId == null)
            {
                final PortletConfiguration pc = delegateStore.getByPortletId(gadgetId);
                existingDashboardId = pc.getDashboardPageId();
            }
            //clear both the source and destination dashboard caches.
            cacheByPageId.remove(existingDashboardId);
            cacheByPageId.remove(dashboardId);
            delegateStore.updateGadgetPosition(gadgetId, row, column, dashboardId);
        }
        finally
        {
            cacheLock.unlock();
        }
    }

    public void updateGadgetColor(final Long gadgetId, final Color color)
    {
        notNull("gadgetId", gadgetId);
        notNull("color", color);

        cacheLock.lock();
        try
        {
            cacheById.remove(gadgetId);
            delegateStore.updateGadgetColor(gadgetId, color);
        }
        finally
        {
            cacheLock.unlock();
        }
    }

    public void updateUserPrefs(final Long gadgetId, final Map<String, String> userPrefs)
    {
        notNull("gadgetId", gadgetId);
        notNull("userPrefs", userPrefs);

        cacheLock.lock();
        try
        {
            cacheById.remove(gadgetId);
            delegateStore.updateUserPrefs(gadgetId, userPrefs);
        }
        finally
        {
            cacheLock.unlock();
        }
    }

    public void store(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());
        try
        {
            delegateStore.store(pc);
        }
        finally
        {
            cacheLock.lock();
            try
            {
                cacheById.remove(pc.getId());
                // Remove the old page from the cache.
                removePageFromCache(pc);
                // The page it has moved to wont have it in the cache.  Need to remove the entry manually.
                cacheByPageId.remove(pc.getDashboardPageId());
            }
            finally
            {
                cacheLock.unlock();
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    public PortletConfiguration addGadget(final Long pageId, final Long portletConfigurationId, final Integer column, final Integer row,
            final URI gadgetXml, final Color color, final Map<String, String> userPreferences)
    {
        notNull("pageId", pageId);
        notNull("column", column);
        notNull("row", row);
        notNull("gadgetXml", gadgetXml);
        notNull("userPreferences", userPreferences);
        notNull("color", color);

        PortletConfiguration returnConfig = null;
        try
        {
            returnConfig = delegateStore.addGadget(pageId, portletConfigurationId, column, row, gadgetXml, color, userPreferences);
        }
        finally
        {
            if (returnConfig != null)
            {
                cacheLock.lock();
                try
                {
                    // Remove the cached page as this will now be stale and not contain the new entry.
                    cacheByPageId.remove(returnConfig.getDashboardPageId());
                    putInCacheById(returnConfig);
                }
                finally
                {
                    cacheLock.unlock();
                }
            }
        }
        return copyConfiguration(returnConfig);
    }

    /**
     * Flush the cache by removing all entries.
     */
    public void flush()
    {
        cacheLock.lock();
        try
        {
            cacheByPageId.clear();
            cacheById.clear();
        }
        finally
        {
            cacheLock.unlock();
        }
    }

    /**
     * This is a non-caching call.  Will delegate straight through to the db store.
     */
    public EnclosedIterable<PortletConfiguration> getAllPortletConfigurations()
    {
        return delegateStore.getAllPortletConfigurations();
    }

    /**
     * Remove all the cached portal pages that contain this portlet configuration. This method must only be called when
     * the #cacheLock is held.
     *
     * @param portletConfiguration the portlet configuration.
     */
    private void removePageFromCache(final PortletConfiguration portletConfiguration)
    {
        for (final Iterator<List<Long>> iterator = cacheByPageId.values().iterator(); iterator.hasNext();)
        {
            if (iterator.next().contains(portletConfiguration.getId()))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Add passed configuration to the cacheById cache. The #cacheLock should be held before calling this method.
     *
     * @param portletConfiguration the configuration to add to the cache.
     */
    private void putInCacheById(final PortletConfiguration portletConfiguration)
    {
        if (portletConfiguration.getId() != null)
        {
            cacheById.put(portletConfiguration.getId(), portletConfiguration);
        }
    }

    /**
     * Copy the passed portlet configuration.
     *
     * @param portletConfiguration the portlet configuration to copy.
     * @return the new deeply copied portlet configuration.  The underlying property set is cloned into a new memory
     *         property set.
     */
    private PortletConfiguration copyConfiguration(final PortletConfiguration portletConfiguration)
    {
        if (portletConfiguration != null)
        {
            return new PortletConfigurationImpl(portletConfiguration.getId(), portletConfiguration.getDashboardPageId(),
                    portletConfiguration.getColumn(), portletConfiguration.getRow(),
                    portletConfiguration.getGadgetURI(), portletConfiguration.getColor(), portletConfiguration.getUserPrefs());
        }
        else
        {
            return null;
        }
    }
}
