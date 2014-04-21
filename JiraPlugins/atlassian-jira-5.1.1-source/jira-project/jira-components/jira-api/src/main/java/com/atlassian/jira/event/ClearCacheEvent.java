package com.atlassian.jira.event;

/**
 * Thrown when JIRA should clear and reinit all of its caches.
 *
 * @since v4.1
 */
public class ClearCacheEvent
{
    public static final ClearCacheEvent INSTANCE = new ClearCacheEvent();

    private ClearCacheEvent() {}
}
