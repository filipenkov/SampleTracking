package com.atlassian.crowd.directory;

/**
 * Constants representing synchronisable directory properties.
 */
public class SynchronisableDirectoryProperties
{
    /**
     * Property key for the current synchronisation's start time in milliseconds from epoch.
     */
    public static final String CURRENT_START_SYNC_TIME = "com.atlassian.crowd.directory.sync.currentstartsynctime";

    /**
     * Property key for the last synchronisation's start time in milliseconds from epoch.
     */
    public static final String LAST_START_SYNC_TIME = "com.atlassian.crowd.directory.sync.laststartsynctime";

    /**
     * Property key for the last synchronisation's duration in milliseconds.
     */
    public static final String LAST_SYNC_DURATION_MS = "com.atlassian.crowd.directory.sync.lastdurationms";

    /**
     * Property key for the interval in seconds when the local cache should be synchronised with the remote directory.
     */
    public static final String CACHE_SYNCHRONISE_INTERVAL = "directory.cache.synchronise.interval";

    /**
     * Property key for the synchronisation status.
     * <p>
     * This property should be unset before the first synchronisation has
     * started after directory creation or configuration change.
     */
    public static final String IS_SYNCHRONISING = "com.atlassian.crowd.directory.sync.issynchronising";

    /**
     * Property key for enabling incremental sync
     */
    public static final String INCREMENTAL_SYNC_ENABLED = "crowd.sync.incremental.enabled";

    private SynchronisableDirectoryProperties() {}
}
