/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.project.version;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A caching implementation of the VersionStore that relies on a delegate to do the DB operations.
 *
 * <p>
 * This class needs some thread synchronisation for thread safety, to avoid loading a stale copy of the Versions into the cache.
 * However, it is optimised such that readers will never be blocked by writers, and writers only block each other for the
 * minimum necessary time.
 * Because pre-loading the cache would add overhead to JIRA's startup, the cache is lazily initialised, and so the first
 * call will load the cache for the first time. This is the only time that readers may block, waiting for the cache to
 * be initialised.
 * </p>
 */
public class CachingVersionStore implements VersionStore, Startable
{
    private static final Logger log = Logger.getLogger(CachingVersionStore.class);

    private final VersionStore delegate;
    private final EventPublisher eventPublisher;

    /**
     * This holds the cached Versions.
     * Initially this is null, so readers must use getVersionCache() to ensure the cache is loaded if necessary.
     * Anyone altering this value must first synchronize on "this".
     * The versionCache must be volatile to guarantee that other threads pick up changes.
     */
    private volatile Map versionCache;

    public CachingVersionStore(final VersionStore delegate, final EventPublisher eventPublisher)
    {
        this.delegate = delegate;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refreshCache();
    }

    public List getAllVersions()
    {
        return Collections.unmodifiableList(new ArrayList(getVersionCache().values()));
    }

    public GenericValue getVersion(final Long id)
    {
        return (GenericValue) getVersionCache().get(id);
    }

    public GenericValue createVersion(final Map versionParams)
    {
        // We can call through to the delegate to make the DB changes in multi-threaded mode.
        final GenericValue version = delegate.createVersion(versionParams);
        // Now we force a cache refresh - this is synchronised with other writers, but readers can still get the old
        // cache up until the new cache is fully populated, and swapped into memory.
        refreshCache();
        return version;
    }

    public void storeVersion(final Version version)
    {
        delegate.storeVersion(version);
        refreshCache();
    }

    public void storeVersions(final Collection<Version> versions)
    {
        delegate.storeVersions(versions);
        refreshCache();
    }

    public void deleteVersion(final GenericValue versionGV)
    {
        delegate.deleteVersion(versionGV);
        refreshCache();
    }

    /**
     * Use this method to get a hold of the version cache.
     * <p>
     * The cache is NOT pre-loaded, so it will initially be null.
     * This method will intialise the cache exactly once if it is still null when it is called.
     * </p>
     * <p>
     * The method will not block, and continue to return the "current" cache even while a write operation
     * holds the synchronize lock and is in the middle of building the "next" cache. This is intentional, the "new"
     * values are not considered to be "committed" until they set the versionCache variable.
     * The only time this method can block is when we are loading the very first copy of the cache (ie when <code>versionCache == null<code>)
     * </p>
     * <p>
     * Note that it could be theoretically possible that this method may never need to load the cache, if a write operation
     * happens before the first read operation.
     * </p>
     *
     * @return The version cache, guaranteed not to be null.
     */
    private Map getVersionCache()
    {
        // Store the versionCache in a variable. This is for safety in case someone else sets it to null after the nullness check.
        // The current code should not do this, but better to be safe than sorry.
        final Map currentVersionCache = versionCache;
        // Check if the cache is initialised.
        if (currentVersionCache == null)
        {
            log.debug("VersionCache is null - getting a lock to initialise the cache.");
            // We are not initialised. Get a lock so that only one thread actually initialises the cache.
            synchronized (this)
            {
                // Check if we still need to load the cache.
                if (versionCache == null)
                {
                    // We got in here first, so we load the cache.
                    loadCache();
                }
                else
                {
                    // Some other thread has loaded the cache since our last check - we don't have any work to do.
                    log.debug("VersionCache was created by another thread - no need to initialise.");
                }
                // Anyone who alters the versionCache variable must have the lock, so we know no-one else has set this to null.
                return versionCache;
            }
        }
        return currentVersionCache;
    }

    private synchronized void refreshCache()
    {
        log.debug("Versions changed - cache refresh required.");
        loadCache();
    }

    /**
     * Loads a new cache object from the latest DB values.
     * This method builds the new cache in a separate object to <code>versionCache</code>, in order that reader methods
     * can continue to read the <em>current versionCache</em> up until the new one created by this method is saved to
     * versionCache.
     * <p>
     * Note that, for thread safety, this method uses a synchronized lock on (this).
     * The creating of the new cache, and setting <code>versionCache</code> to the new cache must all occur within
     * this synchronized block in order that we can't set the cache to stale values.
     * </p>
     */
    private synchronized void loadCache()
    {
        log.debug("Loading the cache.");
        // We load the cache into a new object.
        final Map newVersionCache = new LinkedHashMap();
        final List versions = delegate.getAllVersions();
        for (final Iterator iterator = versions.iterator(); iterator.hasNext();)
        {
            final GenericValue version = (GenericValue) iterator.next();
            newVersionCache.put(version.getLong("id"), version);
        }
        // Finally we save this as the "committed" cache and it will be seen by readers.
        log.debug("Committing new cache.");
        versionCache = Collections.unmodifiableMap(newVersionCache);
        log.debug("Cache load complete.");
    }
}
