/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.index.CompositeResultBuilder;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.index.IndexingStrategy;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.index.MultiThreadedIndexingStrategy;
import com.atlassian.jira.index.SimpleIndexingStrategy;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.util.concurrent.RuntimeInterruptedException;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Default IndexManager for {@link SharedEntity shared entities}
 *
 * @since v3.13
 */
public class DefaultSharedEntityIndexManager implements SharedEntityIndexManager
{
    private static final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new MultiThreadedIndexingConfiguration()
    {
        public int minimumBatchSize()
        {
            return 50;
        }

        public int maximumQueueSize()
        {
            return 1000;
        }

        public int noOfThreads()
        {
            return 20;
        }
    };

    private final SharedEntityIndexer indexer;
    private final FileFactory fileFactory;

    private final IndexingStrategy simpleIndexingStrategy = new SimpleIndexingStrategy();

    private final SharedEntity.TypeDescriptor<?>[] types = new SharedEntity.TypeDescriptor[] { SearchRequest.ENTITY_TYPE, PortalPage.ENTITY_TYPE };

    private final Map<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> retrievers;

    public DefaultSharedEntityIndexManager(final SharedEntityIndexer indexer, final SearchRequestManager searchRequestManager, final PortalPageManager portalPageManager, FileFactory fileFactory)
    {
        this.indexer = indexer;
        this.fileFactory = fileFactory;

        final Map<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> retrievers = new LinkedHashMap<TypeDescriptor<?>, Retriever<? extends SharedEntity>>(
            types.length);
        retrievers.put(SearchRequest.ENTITY_TYPE, new Retriever<SharedEntity>()
        {
            public EnclosedIterable<SharedEntity> getAll()
            {
                return searchRequestManager.getAllIndexableSharedEntities();
            }
        });
        retrievers.put(PortalPage.ENTITY_TYPE, new Retriever<SharedEntity>()
        {
            public EnclosedIterable<SharedEntity> getAll()
            {
                return portalPageManager.getAllIndexableSharedEntities();
            }
        });
        this.retrievers = Collections.unmodifiableMap(retrievers);
    }

    public long reIndexAll(final Context context)
    {
        Assertions.notNull("event", context);
        long result = 0;

        for (final Map.Entry<SharedEntity.TypeDescriptor<?>, Retriever<? extends SharedEntity>> entry : retrievers.entrySet())
        {
            indexer.recreate(entry.getKey());
            result += reIndex(context, entry.getKey(), entry.getValue());
        }
        return result;
    }

    private <S extends SharedEntity> long reIndex(final Context context, final TypeDescriptor<?> type, final Retriever<S> retriever)
    {
        context.setName(type.getName());
        final long start = System.currentTimeMillis();
        final EnclosedIterable<S> all = retriever.getAll();
        final IndexingStrategy strategy = getStrategy(all.size());
        try
        {
            final CompositeResultBuilder builder = new CompositeResultBuilder();
            all.foreach(new Consumer<S>()
            {
                public void consume(final S entity)
                {
                    final Context.Task task = context.start(entity);
                    builder.add(strategy.get(new Supplier<Index.Result>()
                    {
                        public Index.Result get()
                        {
                            try
                            {
                                return indexer.index(entity);
                            }
                            finally
                            {
                                task.complete();
                            }
                        }
                    }));
                }
            });
            builder.toResult().await();
            return System.currentTimeMillis() - start;
        }
        finally
        {
            strategy.close();
        }
    }

    private <S> IndexingStrategy getStrategy(final int count)
    {
        if (count < multiThreadedIndexingConfiguration.minimumBatchSize())
        {
            return simpleIndexingStrategy;
        }
        return new MultiThreadedIndexingStrategy(simpleIndexingStrategy, multiThreadedIndexingConfiguration, "SharedEntityIndexer");
    }

    public long reIndexAll() throws IndexException
    {
        return reIndexAll(Contexts.nullContext());
    }

    public long optimize()
    {
        long result = 0;
        for (final TypeDescriptor<?> type : types)
        {
            result += indexer.optimize(type);
        }
        return result;
    }

    public void shutdown()
    {
        for (final TypeDescriptor<?> type : types)
        {
            indexer.shutdown(type);
        }
    }

    public long activate(final Context context)
    {
        Assertions.notNull("event", context);
        return reIndexAll(context);
    }

    public void deactivate()
    {
        for (final TypeDescriptor<?> type : types)
        {
            clear(type);
        }
    }

    public boolean isIndexingEnabled()
    {
        // TODO consider retiring from the Lifecycle interface, only really implemented in DefaultIndexManager
        return true;
    }

    public Collection<String> getAllIndexPaths()
    {
        return indexer.getAllIndexPaths();
    }

    public int size()
    {
        return sum(sizes());
    }

    public boolean isEmpty()
    {
        for (final Retriever<? extends SharedEntity> element : retrievers.values())
        {
            if (!element.getAll().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private int sum(final int[] ints)
    {
        int result = 0;
        for (final int j : ints)
        {
            result += j;
        }
        return result;
    }

    private int[] sizes()
    {
        final int[] sizes = new int[retrievers.size()];
        int i = 0;
        for (final Retriever<? extends SharedEntity> element : retrievers.values())
        {
            sizes[i++] = element.getAll().size();
        }
        return sizes;
    }

    @Override
    public String toString()
    {
        return "SharedEntityIndexManager: paths: " + getAllIndexPaths();
    }

    /**
     * Nuke the index directory.
     *
     * @param type
     */
    private void clear(final TypeDescriptor<?> type)
    {
        fileFactory.removeDirectoryIfExists(indexer.clear(type));
    }

    interface Retriever<S extends SharedEntity>
    {
        /**
         * Get all entities so we can re-index them.
         *
         * @return a CloseableIterable over all entities for a type.
         */
        EnclosedIterable<S> getAll();
    }

    static final class CompositeResult implements Index.Result
    {
        private final BlockingQueue<Index.Result> results;
        private final CountDownLatch latch;

        public CompositeResult(final int size)
        {
            results = new LinkedBlockingQueue<Result>(size);
            latch = new CountDownLatch(size);
        }

        void add(final Index.Result result)
        {
            results.add(result);
        }

        public boolean isDone()
        {
            return latch.getCount() == 0;
        }

        public void await()
        {
            try
            {
                latch.await();
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeInterruptedException(e);
            }
        }

        public boolean await(final long timeout, final TimeUnit unit)
        {
            try
            {
                return latch.await(timeout, unit);
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeInterruptedException(e);
            }
        }

        void clearDone()
        {
            for (final Iterator<Index.Result> it = results.iterator(); it.hasNext();)
            {
                if (it.next().isDone())
                {
                    latch.countDown();
                    it.remove();
                }
            }
        }
    }
}
