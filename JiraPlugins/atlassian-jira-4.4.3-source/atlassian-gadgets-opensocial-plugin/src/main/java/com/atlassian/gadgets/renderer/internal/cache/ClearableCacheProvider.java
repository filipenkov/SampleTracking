package com.atlassian.gadgets.renderer.internal.cache;

import org.apache.shindig.common.cache.CacheProvider;

/**
 * A cache provider that can be cleared
 *
 * @since v3.0
 */
public interface ClearableCacheProvider extends CacheProvider
{
    /**
     * Calling this method will clear all caches that have been handed out
     * by this cache.  Implementations should wrap caches they hand out in a wrapper
     * that will clear the underlying caches.
     */
    void clear();
}
