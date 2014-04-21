/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.util.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.collect.Sized;

import java.util.Collection;

/**
 * Manage an index lifecycle.
 *
 * @since v3.13
 */
@PublicApi
public interface IndexLifecycleManager extends Sized, Shutdown
{
    /**
     * Reindex everything.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     */
    long reIndexAll(Context context);

    /**
     * Optimize the underlying indexes. Make the subsequent searching more efficient.
     *
     * @return the amount of time in millis this method took (because you are too lazy to time me), 0 if indexing is not enabled or -1 if we cannot
     *         obtain the index writeLock.
     * @throws IndexException if the indexes are seriously in trouble
     */
    long optimize();

    /**
     * Shuts down the indexing manager and closes its resources (if any).
     */
    void shutdown();

    /**
     * Activates search indexes.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms
     */
    long activate(Context context);

    /**
     * De-activates indexing (as happens from the admin page) and removes index directories.
     */
    void deactivate();

    /**
     * @return whether this index is enabled or true if all sub indexes are enabled
     */
    boolean isIndexingEnabled();

    /**
     * @return a collection of Strings that map to all paths that contain Lucene indexes. Must not be null.
     */
    Collection<String> getAllIndexPaths();

    /**
     * @return how many Entities will be re-indexed by {@link #reIndexAll(Context)}
     */
    int size();
}
