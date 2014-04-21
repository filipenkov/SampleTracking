/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.util.DatabaseIssuesIterable;
import com.atlassian.jira.issue.util.IssueGVsIssueIterable;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Lists;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIndexManager implements IssueIndexManager
{
    private static final Logger log = Logger.getLogger(DefaultIndexManager.class);

    public static final Analyzer ANALYZER_FOR_SEARCHING = JiraAnalyzer.ANALYZER_FOR_SEARCHING;
    public static final Analyzer ANALYZER_FOR_INDEXING = JiraAnalyzer.ANALYZER_FOR_INDEXING;
    public static final String COMMENTS_SUBDIR = "comments";
    public static final String ISSUES_SUBDIR = "issues";
    public static final String PLUGINS_SUBDIR = "plugins";

    // ---------------------------------------------------------------------------------------------------------- members

    private final AtomicInteger indexUpdateCount = new AtomicInteger(0);
    private final AtomicBoolean isOptimizing = new AtomicBoolean();
    private final ReadWriteLock indexLock = new ReentrantReadWriteLock();

    private final IssueIndexer issueIndexer;
    private final IndexPathManager indexPathManager;
    private final IndexingConfiguration indexConfig;
    private final ReindexMessageManager reindexMessageManager;
    private final EventPublisher eventPublisher;
    private final ListenerManager listenerManager;
    private final ProjectManager projectManager;

    private final ThreadLocal<Boolean> indexingHeld = new ThreadLocal<Boolean>();
    private final ThreadLocal<Set<Issue>> heldIssues = new ThreadLocal<Set<Issue>>()
    {
        @Override
        protected Set<Issue> initialValue()
        {
            return new HashSet<Issue>();
        }
    };

    @Override
    public Analyzer getAnalyzerForSearching()
    {
        return ANALYZER_FOR_SEARCHING;
    }

    @Override
    public Analyzer getAnalyzerForIndexing()
    {
        return ANALYZER_FOR_INDEXING;
    }

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> issueSearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.getIssueSearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexingEnabled());
            }
        }
    };

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> commentSearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.getCommentSearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexingEnabled());
            }
        }
    };

    // responsible for getting the actual searcher when required
    private final Supplier<IndexSearcher> changeHistorySearcherSupplier = new Supplier<IndexSearcher>()
    {
        public IndexSearcher get()
        {
            try
            {
                return issueIndexer.getChangeHistorySearcher();
            }
            catch (final RuntimeException e)
            {
                throw new SearchUnavailableException(e, indexConfig.isIndexingEnabled());
            }
        }
    };

    // ------------------------------------------------------------------------------------------------------------ ctors

    public DefaultIndexManager(final IndexingConfiguration indexProperties, final IssueIndexer issueIndexer, final IndexPathManager indexPath,
            ReindexMessageManager reindexMessageManager, EventPublisher eventPublisher, ListenerManager listenerManager, ProjectManager projectManager)
    {
        this.eventPublisher = notNull("eventPublisher", eventPublisher);
        indexConfig = notNull("indexProperties", indexProperties);
        this.issueIndexer = notNull("issueIndexer", issueIndexer);
        indexPathManager = notNull("indexPath", indexPath);
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.listenerManager = listenerManager;
        this.projectManager = projectManager;
    }

    // ---------------------------------------------------------------------------------------------------------- methods

    public void deactivate()
    {
        listenerManager.deleteListener(IssueIndexListener.class);
        // Turn indexing off
        indexConfig.disableIndexing();

        issueIndexer.shutdown();

        // Flush the ThreadLocal searcher to ensure that the index files are closed.
        // This method is called from DataImport, which flushes the mail queue. If the mail queue contains
        // any items that run a Lucene search (e.g. subscription mail queue items), they will initialise the
        // thread local searcher which will stop the index files from being deleted in Windows. Windows does not
        // allow to delete open files.
        // This code is hear as it is a good idea to ensure that the searcher in the thread local is closed
        // before the index files are deleted.
        flushThreadLocalSearchers();

        eventPublisher.publish(new IndexDeactivatedEvent());
    }

    // Activates the indexing path currently configured by the IndexPathManager.
    public long activate(final Context context)
    {
        Assertions.notNull("context", context);
        // Test if indexing is turned off
        if (isIndexingEnabled())
        {
            throw new IllegalStateException("Cannot activate indexing as it is already active.");
        }
        if (log.isDebugEnabled())
        {
            log.debug("Activating indexes in '" + indexPathManager.getIndexRootPath() + "'.");
        }

        // Create Index listener
        listenerManager.createListener(IssueIndexListener.NAME, IssueIndexListener.class);

        // Turn indexing on
        indexConfig.enableIndexing();

        return reIndexAll(context);
    }

    public boolean isIndexingEnabled()
    {
        return indexConfig.isIndexingEnabled();
    }

    /**
     * @return The number of milliseconds taken to reindex everything, or -1 if not indexing
     * @throws IndexException
     */
    public long reIndexAll() throws IndexException
    {
        return reIndexAll(Contexts.nullContext());
    }

    public long reIndexAll(final Context context)
    {
        Assertions.notNull("context", context);
        context.setName("Issue");
        log.info("Reindexing all issues");
        eventPublisher.publish(new ReindexAllStartedEvent());
        final long startTime = System.currentTimeMillis();
        boolean restartScheduler = false;
        final Scheduler scheduler = ComponentAccessor.getScheduler();
        {
            final Awaitable writeLock = new Awaitable()
            {
                public boolean await(final long time, final TimeUnit unit) throws InterruptedException
                {
                    return indexLock.writeLock().tryLock(time, unit);
                }
            };
            if (!obtain(writeLock))
            {
                return -1;
            }
        }
        try
        {
            // Stop the scheduler if it is running
            try
            {
                if (!scheduler.isShutdown() && !scheduler.isPaused())
                {
                    scheduler.pause();
                    restartScheduler = true;
                }
            }
            catch (final SchedulerException e)
            {
                log.warn("The scheduler is not available, unable to pause it before reindexing.", e);
            }

            // Recreate the index as we are about to reindex all issues
            issueIndexer.deleteIndexes();

            // if these fail, we don't have an index...
            final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();

            // Index the issues one project at a time.  This stops various database drivers sucking all issues into
            // memory at once.
            final List<Project> projects = projectManager.getProjectObjects();
            for (Project project : projects)
            {
                EntityCondition condition =  new EntityExpr(IssueFieldConstants.PROJECT, EntityOperator.EQUALS, project.getId());
                final IssuesIterable issuesIterable = new DatabaseIssuesIterable(delegator, getIssueFactory(), condition);
                // do not timeout on reindexAll
                issueIndexer.indexIssuesBatchMode(issuesIterable, context).await();
            }

            // optimise logic, passes 'true' for 'recreateIndex', which forces the optimize
            optimize0();

            // clear any reindex messages
            reindexMessageManager.clear();
        }
        finally
        {
            indexLock.writeLock().unlock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // seacher without another try/finally here.
            flushThreadLocalSearchers();

            // reactivate the scheduler if we need to
            if (restartScheduler)
            {
                try
                {
                    scheduler.start();
                }
                catch (final SchedulerException e)
                {
                    log.error("Unable to unpause the scheduler after reindex", e);
                }
            }
        }

        final long totalTime = (System.currentTimeMillis() - startTime);
        if (log.isDebugEnabled())
        {
            log.debug("ReindexAll took : " + totalTime + "ms");
        }

        eventPublisher.publish(new ReindexAllCompletedEvent(totalTime));

        return totalTime;
    }

    public long reIndexIssues(final Collection<GenericValue> issues) throws IndexException
    {
        return reIndexIssues(new IssueGVsIssueIterable(issues, getIssueFactory()), Contexts.nullContext());
    }

    public long reIndexIssueObjects(final Collection<? extends Issue> issueObjects) throws IndexException
    {
        // We would like to transform the Issue object to GenericValues before re-indexing to ensure that there
        // are no discrepancies between them. Once we move the entire system to Issue objects this will be unnecessary.
        // Until then, please do *not* change this behaviour.
        @SuppressWarnings ( { "unchecked" })
        final Collection<GenericValue> genericValues = CollectionUtils.collect(issueObjects, IssueFactory.TO_GENERIC_VALUE);
        return reIndexIssues(genericValues);
    }

    public void reIndex(final Issue issue) throws IndexException
    {
        final List<Issue> issues = Lists.newArrayList(issue);
        reIndexIssueObjects(issues);
    }

    public void reIndex(final GenericValue issueGV) throws IndexException
    {
        if ("Issue".equals(issueGV.getEntityName()))
        {
            final List<GenericValue> genericValues = Lists.newArrayList(issueGV);
            reIndexIssues(genericValues);
        }
        else
        {
            log.error("Entity is not an issue " + issueGV.getEntityName());
        }
    }

    public void hold()
    {
        indexingHeld.set(Boolean.TRUE);
    }

    public boolean isHeld()
    {
        return indexingHeld.get() != null && indexingHeld.get();
    }

    public long release() throws IndexException
    {
        indexingHeld.set(Boolean.FALSE);
        try
        {
            final Set<Issue> queue = heldIssues.get();
            if (queue.size() > 0)
            {
                IssuesIterable issuesIterable = new IssueObjectIssuesIterable(queue);
                return reIndexIssues(issuesIterable, Contexts.nullContext());
            }
            return 0;
        }
        finally
        {
            heldIssues.remove();
            indexingHeld.remove();
        }
    }

    public long reIndexIssues(final IssuesIterable issuesIterable, final Context context) throws IndexException
    {
        // if indexing is currently held on this thread just save up the issues for later
        if (isHeld())
        {
            final Set<Issue> queue = heldIssues.get();
            issuesIterable.foreach(new Consumer<Issue>()
            {
                @Override
                public void consume(@NotNull Issue element)
                {
                    queue.add(element);
                }
            });
            return 0;
        }

        Assertions.notNull("issues", issuesIterable);
        Assertions.notNull("context", context);

        eventPublisher.publish(new ReindexIssuesStartedEvent());
        final OpTimer opTimer = Instrumentation.pullTimer(InstrumentationName.ISSUE_INDEX_WRITES);
        if (!getIndexLock())
        {
            log.error("Could not reindex: " + issuesIterable.toString());
            return -1;
        }

        try
        {
            await(issueIndexer.reindexIssues(issuesIterable, context));
            optimizeIfNecessary(issuesIterable.size());
        }
        finally
        {
            releaseIndexLock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // seacher without another try/finally here.
            flushThreadLocalSearchers();

            opTimer.end();
        }

        final long totalTime = opTimer.snapshot().getMillisecondsTaken();
        if (log.isDebugEnabled())
        {
            log.debug("Reindexed " + issuesIterable.size() + " issues in " + totalTime + "ms.");
        }
        eventPublisher.publish(new ReindexIssuesCompletedEvent(totalTime));
        return totalTime;
    }

    public int size()
    {
        return new DatabaseIssuesIterable(ComponentAccessor.getOfBizDelegator(), getIssueFactory()).size();
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Optimize if the threshhold for optimizing has been reached or the size of the number of issues indexed means we might as well optimize.
     *
     * @param issueNum how many issues were indexed
     * @throws IndexException if things go pear shaped.
     */
    @GuardedBy("index read lock")
    private void optimizeIfNecessary(final int issueNum) throws IndexException
    {
        try
        {
            indexUpdateCount.addAndGet(issueNum);
            final int issuesToForceOptimize = indexConfig.getIssuesToForceOptimize();
            final int maxReindexes = indexConfig.getMaxReindexes();

            // force optimise if over threshhold # of issues indexed at once or if we are recreating index
            if ((issuesToForceOptimize > 0) && (issueNum >= issuesToForceOptimize))
            {
                optimizeAfterIssuesToForceOptimizeExceeded(issueNum, issuesToForceOptimize);
            }
            else if ((maxReindexes > 0) && (indexUpdateCount.get() >= maxReindexes))
            {
                optimizeAfterMaxReindexesExceeded(maxReindexes);
            }
        }
        catch (final Exception e)
        {
            throw new IndexException("Error: " + e, e);
        }
    }

    private void optimizeAfterIssuesToForceOptimizeExceeded(int issueNum, int issuesToForceOptimize)
    {
        // JRA-23490 we don't want to optimize too aggressively. If any optimization is running as a result of exceeded threshold
        // (as opposed to explicit client requests), let's just give up
        if (isOptimizing.compareAndSet(false, true)) {
            try {
                log.info("Optimizing the index because " + issueNum + " issues were re-indexed in bulk. Threshold is " + issuesToForceOptimize + ".");
                final long optimizeTime = optimize0();
                log.info("Optimize index completed in " + optimizeTime + "ms.");
            } finally {
                isOptimizing.set(false);
            }
        }
    }

    private void optimizeAfterMaxReindexesExceeded(int maxReindexes)
    {
        // JRA-23490 we don't want to optimize too aggressively. If any optimization is running as a result of exceeded threshold
        // (as opposed to explicit client requests), let's just give up
        if (isOptimizing.compareAndSet(false, true)) {
            try {
                log.info("Optimizing the index because " + indexUpdateCount.get() + " issues have been re-indexed since last optimize. Threshold is " + maxReindexes + ".");
                final long optimizeTime = optimize0();
                log.info("Optimize index completed in " + optimizeTime + "ms.");
            } finally {
                isOptimizing.set(false);
            }
        }
    }

    public long optimize()
    {
        if (!isIndexingEnabled())
        {
            return 0;
        }
        if (!getIndexLock())
        {
            return -1;
        }
        try
        {
            return optimize0();
        }
        finally
        {
            releaseIndexLock();
        }
    }

    /**
     * Optimizes the index and resets the dirtyness count. Should only be called if the index read lock is obtained.
     *
     * @return optimization time in milliseconds
     */
    @GuardedBy("index read lock")
    private long optimize0()
    {
        // JRA-23490 we want to reset the update count just before the optimization starts so that newly indexed issues
        // (if any) are counted towards next scheduled optimization
        indexUpdateCount.set(0);
        final long startTime = System.currentTimeMillis();
        // do not timeout on optimize
        issueIndexer.optimize().await();
        return System.currentTimeMillis() - startTime;
    }

    public void deIndex(final Issue issue) throws IndexException
    {
        deIndex(issue.getGenericValue());
    }

    public void deIndex(final GenericValue entity) throws IndexException
    {
        if (!"Issue".equals(entity.getEntityName()))
        {
            log.error("Entity is not an issue " + entity.getEntityName());
            return;
        }
        if (!getIndexLock())
        {
            log.error("Could not deindex: " + entity.getString("key"));
            return;
        }

        try
        {
            final List<GenericValue> genericValues = Lists.newArrayList(entity);
            await(issueIndexer.deindexIssues(new IssueGVsIssueIterable(genericValues, getIssueFactory()), Contexts.nullContext()));
        }
        finally
        {
            releaseIndexLock();
            // Releasing the lock should never throw an exception - so we are safe to flush the
            // searcher without another try/finally here.
            flushThreadLocalSearchers();
        }
    }

    private void await(final Index.Result result)
    {
        obtain(new Awaitable()
        {
            public boolean await(final long time, final TimeUnit unit) throws InterruptedException
            {
                return result.await(time, unit);
            }
        });
    }

    private void releaseIndexLock()
    {
        indexLock.readLock().unlock();
    }

    /**
     * @return true if got the lock, false otherwise
     */
    boolean getIndexLock()
    {
        if (StringUtils.isBlank(indexPathManager.getIndexRootPath()))
        {
            log.error("File path not set - not indexing");
            return false;
        }

        // Attempt to acquire read lock on index operations.
        return obtain(new Awaitable()
        {
            public boolean await(final long time, final TimeUnit unit) throws InterruptedException
            {
                return indexLock.readLock().tryLock(time, unit);
            }
        });
    }

    private boolean obtain(final Awaitable waitFor)
    {
        try
        {
            if (waitFor.await(indexConfig.getIndexLockWaitTime(), TimeUnit.MILLISECONDS))
            {
                return true;
            }
        }
        catch (final InterruptedException ie)
        {
            log.error("Wait attempt interrupted.", new IndexException("Wait attempt interrupted.", ie));
            return false;
        }
        // We failed to acquire a lock after waiting the configured time (default=30s), so give up.
        final String errorMessage = "Wait attempt timed out - waited " + indexConfig.getIndexLockWaitTime() + " milliseconds";
        log.error(errorMessage, new IndexException(errorMessage));
        return false;
    }

    public String getPluginsRootPath()
    {
        return indexPathManager.getPluginIndexRootPath();
    }

    public List<String> getExistingPluginsPaths()
    {
        final File pluginRootPath = new File(getPluginsRootPath());

        if (pluginRootPath.exists() && pluginRootPath.isDirectory() && pluginRootPath.canRead())
        {
            final String[] listing = pluginRootPath.list();
            // Find all sub-directories of the plugins root path, as each plugin should have its own
            // sub-directory under the root plugin index plath
            if (listing != null)
            {
                final List<String> subdirs = new ArrayList<String>();
                for (final String element : listing)
                {
                    final File f = new File(pluginRootPath, element);
                    if (f.exists() && f.canRead() && f.isDirectory())
                    {
                        subdirs.add(f.getAbsolutePath());
                    }
                }
                return Collections.unmodifiableList(subdirs);
            }
        }

        return Collections.emptyList();
    }

    public Collection<String> getAllIndexPaths()
    {
        final List<String> paths = new ArrayList<String>();
        paths.addAll(issueIndexer.getIndexPaths());
        paths.addAll(getExistingPluginsPaths());
        return Collections.unmodifiableList(paths);
    }

    public IndexSearcher getIssueSearcher()
    {
        return SearcherCache.getThreadLocalCache().retrieveIssueSearcher(issueSearcherSupplier);
    }

    public IndexSearcher getCommentSearcher()
    {
        return SearcherCache.getThreadLocalCache().retrieveCommentSearcher(commentSearcherSupplier);
    }

    public IndexSearcher getChangeHistorySearcher()
    {
        return SearcherCache.getThreadLocalCache().retrieveChangeHistorySearcher(changeHistorySearcherSupplier);
    }

    public void shutdown()
    {
        eventPublisher.publish(new IndexingShutdownEvent());
        flushThreadLocalSearchers();
        issueIndexer.shutdown();
    }

    /**
     * Get the count of reindexes since the last call to optimize. <p/> Note: package private as be used in tests.
     *
     * @return the count of reindexes since the last call to optimize
     */
    int getReindexesSinceOptimize()
    {
        return indexUpdateCount.get();
    }

    IssueFactory getIssueFactory()
    {
        // the reason that this is not done in the constructor is that IssueFactory depends on IssueLinkManager which
        // depends on IssueIndexManager
        // and therefore is a cyclic dependency
        return ComponentManager.getComponentInstanceOfType(IssueFactory.class);
    }

    @Override
    public String toString()
    {
        return "DefaultIndexManager: paths: " + getAllIndexPaths();
    }

    public static void flushThreadLocalSearchers()
    {
        try
        {
            SearcherCache.getThreadLocalCache().closeSearchers();
        }
        catch (final IOException e)
        {
            log.error("Error while resetting searcher: " + e, e);
        }
    }

    private interface Awaitable
    {
        /**
         * See if we can wait successfully for this thing.
         * @param time how long to wait
         * @param unit the unit in which time is specified
         * @return true if the thing was obtained.
         * @throws InterruptedException if someone hits the interrupt button
         */
        boolean await(long time, TimeUnit unit) throws InterruptedException;
    }

}
