package com.atlassian.jira.index;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.index.DelayCloseable.AlreadyClosedException;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.ListeningTestCase;

public class TestDelayCloseSearcher extends ListeningTestCase
{
    @Test
    public void testNullSearcher() throws Exception
    {
        try
        {
            new DelayCloseSearcher(null, new Closeable()
            {
                public void close()
                {}
            });
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullCloseable() throws Exception
    {
        try
        {
            new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory()), null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testCloseWhenDone() throws Exception
    {
        final AtomicBoolean searcherClosed = new AtomicBoolean();
        final AtomicBoolean closeCalled = new AtomicBoolean();
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory())
        {
            @Override
            public void close() throws IOException
            {
                searcherClosed.set(true);
                super.close();
            }
        }, new Closeable()
        {
            public void close()
            {
                closeCalled.set(true);
            }
        });
        searcher.open();
        searcher.open();
        searcher.closeWhenDone();
        assertFalse(searcherClosed.get());
        assertFalse(closeCalled.get());
        assertFalse(searcher.isClosed());
        searcher.close();
        assertFalse(searcherClosed.get());
        assertFalse(closeCalled.get());
        assertFalse(searcher.isClosed());
        searcher.close();
        assertTrue(searcherClosed.get());
        assertTrue(closeCalled.get());
        assertTrue(searcher.isClosed());
    }

    @Test
    public void testCloseThrowsIOException() throws Exception
    {
        final IOException blah = new IOException("blah!");
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory())
        {
            @Override
            public void close() throws IOException
            {
                throw blah;
            }
        }, new Closeable()
        {
            public void close()
            {}
        });
        try
        {
            searcher.closeWhenDone();
            fail("RuntimeIOException expected");
        }
        catch (final RuntimeIOException expected)
        {
            assertSame(blah, expected.getCause());
        }
    }

    @Test
    public void testOpenThrowsAlreadyClosedException() throws Exception
    {
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory())
        {}, new Closeable()
        {
            public void close()
            {}
        });
        searcher.closeWhenDone();
        try
        {
            searcher.open();
            fail("AlreadyClosedException expected");
        }
        catch (final AlreadyClosedException expected)
        {}
    }

    @Test
    public void testCloseWhenDoneClosesImmediatelyIfNotOpen() throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean();
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory()), new Closeable()
        {
            public void close()
            {
                closed.set(true);
            }
        });
        searcher.closeWhenDone();
        assertTrue(closed.get());
    }

    @Test
    public void testCloseWhenDoneDoesNotCloseImmediatelyIfOpen() throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean();
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory()), new Closeable()
        {
            public void close()
            {
                closed.set(true);
            }
        });
        searcher.open();
        searcher.closeWhenDone();
        assertFalse(closed.get());
        searcher.close();
        assertTrue(closed.get());
    }
}
