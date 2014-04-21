package com.atlassian.jira.index;

import com.atlassian.jira.index.DelayCloseable.AlreadyClosedException;
import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.util.concurrent.LazyReference;
import net.jcip.annotations.ThreadSafe;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Thread-safe container that manages our current {@link IndexSearcher} and {@link Writer}.
 * <p>
 * Gets passed searcher and writer factories that create new instances of these when required.
 */
@ThreadSafe
class DefaultIndexEngine implements DefaultIndex.Engine
{
    /**
     * How to perform an actual write to the writer.
     */
    static enum FlushPolicy
    {
        /**
         * Do not flush or close.
         */
        NONE()
        {
            @Override
            void commit(final WriterReference writer)
            {}
        },
        /**
         * Commit the writer's pending updates, do not close.
         */
        FLUSH()
        {
            @Override
            void commit(final WriterReference writer)
            {
                writer.commit();
            }
        },

        /**
         * Close the writer after performing the write.
         */
        CLOSE()
        {
            @Override
            synchronized void commit(final WriterReference writer)
            {
                writer.close();
            }
        };

        void perform(final Operation operation, final WriterReference writer) throws IOException
        {
            try
            {
                operation.perform(writer.get(operation.mode()));
            }
            finally
            {
                commit(writer);
            }
        }

        abstract void commit(final WriterReference writer);
    }

    private final WriterReference writerReference;
    private final SearcherReference searcherReference;
    private final FlushPolicy writePolicy;
    private final Configuration configuration;

    /**
     * Production ctor.
     *
     * @param configuration the {@link Directory} and {@link Analyzer}
     * @param writePolicy when to flush writes
     */
    DefaultIndexEngine(final @NotNull Configuration configuration, final @NotNull FlushPolicy writePolicy)
    {
        this(new SearcherFactory(configuration), new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return new WriterWrapper(configuration, mode);
            }
        }, configuration, writePolicy);
    }

    /**
     * Main ctor.
     *
     * @param searcherFactory for creating {@link Searcher searchers}
     * @param writerFactory for creating Writer instances of the correct mode
     * @param configuration the {@link Directory} and {@link Analyzer}
     * @param writePolicy when to flush writes
     */
    DefaultIndexEngine(final @NotNull Supplier<IndexSearcher> searcherFactory, final Function<Index.UpdateMode, Writer> writerFactory, final @NotNull Configuration configuration, final @NotNull FlushPolicy writePolicy)
    {
        this.writePolicy = notNull("writePolicy", writePolicy);
        this.configuration = notNull("configuration", configuration);
        searcherReference = new SearcherReference(searcherFactory);
        writerReference = new WriterReference(new WriterFactory(writerFactory));
    }

    /**
     * leak a {@link Searcher}. Must get closed after usage.
     */
    @NotNull
    public IndexSearcher getSearcher()
    {
        // mode is irrelevant to a Searcher
        return searcherReference.get(Index.UpdateMode.INTERACTIVE);
    }

    public void clean()
    {
        searcherReference.close();
        writerReference.close();
        try
        {
            new IndexWriter(configuration.getDirectory(), configuration.getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED).close();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public void write(@NotNull final Operation operation) throws IOException
    {
        try
        {
            writePolicy.perform(operation, writerReference);
        }
        finally
        {
            searcherReference.close();
        }
    }

    public void close()
    {
        writerReference.close();
        searcherReference.close();
    }

    /**
     * Thread-safe holder of the current Searcher
     */
    @ThreadSafe
    private class SearcherReference extends ReferenceHolder<DelayCloseSearcher>
    {
        private final Supplier<IndexSearcher> searcherSupplier;

        SearcherReference(@NotNull final Supplier<IndexSearcher> searcherSupplier)
        {
            this.searcherSupplier = notNull("searcherSupplier", searcherSupplier);
        }

        @Override
        DelayCloseSearcher doCreate(final Index.UpdateMode mode)
        {
            writePolicy.commit(writerReference);
            return new DelayCloseSearcher(searcherSupplier.get());
        }

        @Override
        DelayCloseSearcher open(final DelayCloseSearcher searcher)
        {
            searcher.open();
            return searcher;
        }

        @Override
        void doClose(final DelayCloseSearcher searcher)
        {
            searcher.closeWhenDone();
        }
    }

    /**
     * Thread-safe holder of the current Writer
     */
    @ThreadSafe
    private class WriterReference extends ReferenceHolder<Writer>
    {
        private final Function<Index.UpdateMode, Writer> writerFactory;
        private Index.UpdateMode mode;

        WriterReference(final Function<Index.UpdateMode, Writer> writerFactory)
        {
            this.writerFactory = notNull("writerFactory", writerFactory);
        }

        public void commit()
        {
            if (!isNull())
            {
                get().commit();
            }
        }

        @Override
        Writer doCreate(final Index.UpdateMode mode)
        {
            this.mode = mode;
            return writerFactory.get(mode);
        }

        @Override
        void doClose(final Writer writer)
        {
            writer.close();
        }

        @Override
        Writer open(final Writer writer)
        {
            writer.setMode(mode);
            return writer;
        }
    }

    static abstract class ReferenceHolder<T> implements Function<Index.UpdateMode, T>, Closeable
    {
        private final AtomicReference<LazyReference<T>> reference = new AtomicReference<LazyReference<T>>();

        public final void close()
        {
            final Reference<T> supplier = reference.getAndSet(null);
            if (supplier != null)
            {
                try
                {
                    doClose(supplier.get());
                }
                catch (final RuntimeException ignore)
                {}
            }
        }

        abstract void doClose(T element);

        public final T get(final Index.UpdateMode mode)
        {
            while (true)
            {
                Reference<T> ref = reference.get();
                while (ref == null)
                {
                    reference.compareAndSet(null, new LazyReference<T>()
                    {
                        @Override
                        protected T create()
                        {
                            return doCreate(mode);
                        }
                    });
                    ref = reference.get();
                }
                try
                {
                    // in the rare case of a race condition, try again
                    return open(ref.get());
                }
                catch (final AlreadyClosedException ignore)
                {}
            }
        }

        abstract T doCreate(Index.UpdateMode mode);

        abstract T open(T element);

        final boolean isNull()
        {
            return reference.get() == null;
        }

        final T get()
        {
            final LazyReference<T> lazyReference = reference.get();
            return (lazyReference == null) ? null : lazyReference.get();
        }

        final void setNull()
        {
            reference.set(null);
        }
    }

    /**
     * Special writer factory that clears the writerReference when an OutOfMemoryError is encountered.
     * <p>
     * For details see https://issues.apache.org/jira/browse/LUCENE-1429
     */
    private class WriterFactory implements Function<Index.UpdateMode, Writer>
    {
        private final Function<Index.UpdateMode, Writer> delegate;

        WriterFactory(final Function<Index.UpdateMode, Writer> delegate)
        {
            this.delegate = notNull("delegate", delegate);
        }

        public Writer get(final Index.UpdateMode mode)
        {
            return new WriterOutOfMemory(delegate.get(mode), new WriterOutOfMemory.Handler()
            {
                public void handle(final OutOfMemoryError error)
                {
                    writerReference.setNull();
                }
            });
        }
    }

    private static class SearcherFactory implements Supplier<IndexSearcher>
    {
        private final Configuration configuration;

        SearcherFactory(final Configuration configuration)
        {
            this.configuration = notNull("configuration", configuration);
        }

        public IndexSearcher get()
        {
            try
            {
                return new IndexSearcher(configuration.getDirectory(), true);
            }
            catch (final IOException e)
            {
                ///CLOVER:OFF
                throw new RuntimeIOException(e);
                ///CLOVER:ON
            }
        }
    }
}
