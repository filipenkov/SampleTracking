package com.atlassian.gadgets.event;

/**
 * An event that a host application can throw to clear shindig's cache.  This may be required for example when a host
 * application updates its whitelist configuration, which changes which gadgets may be allowed or banned.
 *
 * @since v3.0
 */
public final class ClearHttpCacheEvent
{
    public static final ClearHttpCacheEvent INSTANCE = new ClearHttpCacheEvent();
}
