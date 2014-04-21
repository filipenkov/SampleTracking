package com.atlassian.jira.util.index;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexManager;
import com.atlassian.jira.task.context.Context;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Convenience class for managing known IndexManagers and calling them all.
 *
 * @since v5.0
 */
public class CompositeIndexLifecycleManager implements IndexLifecycleManager
{
    private static final Logger log = Logger.getLogger(CompositeIndexLifecycleManager.class);

    private final IndexLifecycleManager[] delegates;

    public CompositeIndexLifecycleManager(final IssueIndexManager issueIndexManager, final SharedEntityIndexManager sharedEntityIndexManager)
    {
        delegates = new IndexLifecycleManager[] { issueIndexManager, sharedEntityIndexManager };
    }

    public long optimize()
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

    public long reIndexAll(final Context context)
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
