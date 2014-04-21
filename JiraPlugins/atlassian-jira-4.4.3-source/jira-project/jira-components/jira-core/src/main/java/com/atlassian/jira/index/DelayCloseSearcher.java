package com.atlassian.jira.index;

import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.CompositeCloseable;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.RuntimeIOException;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Implements search over a single IndexReader, but remains open even if close() is called. This way
 * it can be shared by multiple objects that need to search the index without being aware of the
 * keep-the-index-open-until-it-changes logic.
 * <p>
 * Like the {@link DelegateSearcher} it extends, this class uses fragile extension, we need to check
 * the super-classes whenever we up Lucene to make sure we override everything correctly.
 */
class DelayCloseSearcher extends DelegateSearcher implements DelayCloseable
{
    private final DelayCloseable.Helper helper;

    DelayCloseSearcher(@NotNull final IndexSearcher searcher)
    {
        super(notNull("searcher", searcher));
        helper = new DelayCloseable.Helper(new SearcherCloser(searcher));
    }

    DelayCloseSearcher(@NotNull final IndexSearcher searcher, @NotNull final Closeable closeAction)
    {
        super(notNull("searcher", searcher));
        helper = new DelayCloseable.Helper(new CompositeCloseable(closeAction, new SearcherCloser(searcher)));
    }

    public void closeWhenDone()
    {
        helper.closeWhenDone();
    }

    public boolean isClosed()
    {
        return helper.isClosed();
    }

    public void open()
    {
        helper.open();
    }

    //
    // IndexSearcher overrides
    //

    @Override
    public void close()
    {
        helper.close();
    }

    /**
     * Simple {@link Closeable} adaptor for a Searcher.
     */
    private static class SearcherCloser implements Closeable
    {
        private final IndexSearcher searcher;

        SearcherCloser(final IndexSearcher searcher)
        {
            this.searcher = searcher;
        }

        public void close()
        {
            try
            {
                searcher.close();
            }
            catch (final IOException e)
            {
                throw new RuntimeIOException(e);
            }
        }
    }
}
