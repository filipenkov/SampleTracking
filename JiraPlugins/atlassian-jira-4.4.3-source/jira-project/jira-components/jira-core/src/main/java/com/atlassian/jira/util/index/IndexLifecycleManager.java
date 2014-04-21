/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.util.index;

import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.collect.Sized;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Manage an index lifecycle.
 *
 * @since v3.13
 */
public interface IndexLifecycleManager extends Sized, Shutdown
{
    /**
     * Reindex everything.
     *
     * @param context used to report progress back to the user or to the logs. Must not be null.
     * @return Reindex time in ms.
     */
    long reIndexAll(Context context) throws IndexException;

    /**
     * Optimize the underlying indexes. Make the subsequent searching more efficient.
     *
     * @return the amount of time in millis this method took (because you are too lazy to time me), 0 if indexing is not enabled or -1 if we cannot
     *         obtain the index writeLock.
     * @throws IndexException if the indexes are seriously in trouble
     */
    long optimize() throws IndexException;

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

    /**
     * Convenience class for managing known IndexManagers and calling them all.
     *
     * @since v3.13
     */
    public class Composite implements IndexLifecycleManager
    {
        private static final Logger log = Logger.getLogger(Composite.class);

        private final IndexLifecycleManager[] delegates;

        public Composite(final IssueIndexManager issueIndexManager, final SharedEntityIndexManager sharedEntityIndexManager)
        {
            delegates = new IndexLifecycleManager[] { issueIndexManager, sharedEntityIndexManager };
        }

        public long optimize() throws IndexException
        {
            log.info("Optimize Indexes starting...");

            long result = 0;
            for (final IndexLifecycleManager delegate : delegates)
            {
                final long optimize = delegate.optimize();
                log.info("Optimize took: " + optimize + "ms. Indexer: " + delegate.toString());
                result += optimize;
            }
            log.info("Optimize Indexes complete. Total time: " + result + "ms.");
            return result;
        }

        public long reIndexAll(final Context context) throws IndexException
        {
            log.info("Reindex All starting...");
            long result = 0;
            for (final IndexLifecycleManager delegate : delegates)
            {
                final long reIndexAll = delegate.reIndexAll(context);
                log.info("Reindex took: " + reIndexAll + "ms. Indexer: " + delegate.toString());
                result += reIndexAll;
            }
            context.setName("");
            log.info("Reindex All complete. Total time: " + result + "ms.");
            return result;
        }

        public void shutdown()
        {
            for (final IndexLifecycleManager delegate : delegates)
            {
                delegate.shutdown();
            }
        }

        public long activate(final Context context)
        {
            long result = 0;
            for (final IndexLifecycleManager delegate : delegates)
            {
                result += delegate.activate(context);
            }
            return result;
        }

        public void deactivate()
        {
            for (final IndexLifecycleManager delegate : delegates)
            {
                delegate.deactivate();
            }
        }

        public boolean isIndexingEnabled()
        {
            return delegates[0].isIndexingEnabled();
        }

        public Collection<String> getAllIndexPaths()
        {
            final Collection<String> result = new ArrayList<String>();
            for (final IndexLifecycleManager delegate : delegates)
            {
                result.addAll(delegate.getAllIndexPaths());
            }
            return Collections.unmodifiableCollection(result);
        }

        public int size()
        {
            int result = 0;
            for (final IndexLifecycleManager delegate : delegates)
            {
                result += delegate.size();
            }
            return result;
        }

        public boolean isEmpty()
        {
            return size() == 0;
        }
    }
}
