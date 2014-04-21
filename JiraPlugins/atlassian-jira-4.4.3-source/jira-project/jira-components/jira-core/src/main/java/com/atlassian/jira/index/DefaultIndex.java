package com.atlassian.jira.index;

import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import net.jcip.annotations.ThreadSafe;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Uses a {@link DefaultIndex.Engine} to perform actual writes to an index.
 */
@ThreadSafe
class DefaultIndex implements CloseableIndex
{
    private final Engine engine;

    DefaultIndex(@NotNull final Engine engine)
    {
        this.engine = notNull("engine", engine);
    }

    public Result perform(@NotNull final Operation operation)
    {
        notNull("operation", operation);
        try
        {
            engine.write(operation);
            return new Success();
        }
        catch (final IOException e)
        {
            return new Failure(e);
        }
        catch (final Error e)
        {
            return new Failure(e);
        }
        catch (final RuntimeException e)
        {
            return new Failure(e);
        }
    }

    public void close()
    {
        engine.close();
    }

    /**
     * An {@link Engine} maintains the lifecycle of a {@link Writer} while allowing access to it via a
     * {@link Function} that is used to add and delete documents to it.
     */
    interface Engine extends Closeable
    {
        @NotNull
        void write(@NotNull Operation operation) throws IOException;

        /**
         * Wait till any open Searchers are closed and then close the connection.
         */
        void close();

        /**
         * Get the current searcher. Must be closed after use.
         *
         * @return An {@link IndexSearcher} that reflects the latest writes to the index.
         */
        @NotNull
        IndexSearcher getSearcher();

        /**
         * Clean (wipe) the index.
         */
        void clean();
    }

    /**
     * Indicate that an operation completed successfully.
     */
    private static class Success implements Result
    {
        public void await()
        {}

        public boolean await(final long timeout, final TimeUnit unit)
        {
            return true;
        }

        public boolean isDone()
        {
            return true;
        }
    }

    /**
     * Indicate that an operation failed.
     */
    private static class Failure implements Result
    {
        private final Throwable failure;

        Failure(final Error failure)
        {
            this.failure = failure;
        }

        Failure(final RuntimeException failure)
        {
            this.failure = failure;
        }

        Failure(final Exception failure)
        {
            this.failure = new RuntimeException(failure);
        }

        public void await()
        {
            doThrow();
        }

        public boolean await(final long timeout, final TimeUnit unit)
        {
            return doThrow();
        }

        public boolean isDone()
        {
            return true;
        }

        private boolean doThrow()
        {
            if (failure instanceof RuntimeException)
            {
                throw (RuntimeException) failure;
            }
            throw (Error) failure;
        }
    }
}
