package com.atlassian.jira.cache;

import com.atlassian.util.concurrent.LazyReference;

/**
 * This class allows us to set up a cache that is lazy-loaded in a thread-safe way.
 *
 * <p> The type D is the type of the cache Data. It should normally be an immutable data object.
 * @since v4.0
 */
public class LazyLoadingCache<D>
{
    private final CacheLoader<D> cacheLoader;
    // reference must be volatile to ensure safe publication.
    private volatile DataReference reference;

    public LazyLoadingCache(CacheLoader<D> cacheLoader)
    {
        this.cacheLoader = cacheLoader;
        reset();
    }

    /**
     * Gets the cache data object.
     *
     * <p> Calling this method may cause the cache data to be loaded, if it has not been loaded yet.
     *
     * @return the cache data object.
     */
    public D getData() {
        return reference.get();
    }

    /**
     * This method will load the latest cache data, and then replace the existing cache data.
     *
     * <p> Note that it leaves the old cache data intact while the load is occuring in order to allow readers to continue
     * to work without blocking.
     *
     * <p> This method is synchronized in order to stop a possible race condition that could publish stale data.
     *
     * @see #reset()
     */
    public synchronized void reload() {
        // Create a second DataReference while the load is happening.
        DataReference tempReference = new DataReference();
        // force a load
        tempReference.get();
        // now swap in the loaded DataReference
        reference = tempReference;
    }

    /**
     * This method will throw away any existing cache data, and leave the LazyLoadingCache uninitialised.
     * This means that the cache will need to be loaded on the next call to getData(), and readers will be blocked
     * until the cache is reloaded.
     *
     * @see #reload()
     */
    public void reset() {
        reference = new DataReference();
    }

    public static interface CacheLoader<D>
    {
        D loadData();
    }

    private class DataReference extends LazyReference<D>
    {
        protected D create() throws Exception
        {
            return cacheLoader.loadData();
        }
    }
}
