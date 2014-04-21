package com.atlassian.jira.util.cache;

/**
 * An interface that defines a cached manager. Doesn't do a lot at the moment, more a of a marker interface for the future
 */
public interface JiraCachedManager
{
    long getCacheSize();
    void refreshCache();
}
