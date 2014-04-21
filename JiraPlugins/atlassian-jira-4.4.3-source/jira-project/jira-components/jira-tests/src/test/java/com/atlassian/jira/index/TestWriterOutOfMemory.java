package com.atlassian.jira.index;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.index.Index.UpdateMode;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestWriterOutOfMemory extends ListeningTestCase
{
    static final Configuration configuration = TestWriterWrapper.configuration();

    @Test
    public void testOptimize() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void optimize() throws IOException
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.optimize();
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.optimize();
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testAddDocuments() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void addDocuments(final Collection<Document> document) throws IOException
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.addDocuments(new ArrayList<Document>());
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.addDocuments(new ArrayList<Document>());
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testDeleteDocuments() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void deleteDocuments(final Term identifyingTerm) throws IOException
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.deleteDocuments(new Term("dogs", "cats"));
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.deleteDocuments(new Term("dogs", "cats"));
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testUpdateDocuments() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void updateDocuments(final Term identifyingTerm, final Collection<Document> document) throws IOException
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.updateDocuments(new Term("dogs", "cats"), new ArrayList<Document>());
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.updateDocuments(new Term("dogs", "cats"), new ArrayList<Document>());
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testClose() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void close()
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.close();
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.close();
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testFlush() throws Exception
    {
        final OutOfMemoryError thrown = new OutOfMemoryError();
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void commit()
            {
                throw thrown;
            }

            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        try
        {
            wrapper.commit();
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {
            assertSame(thrown, expected);
        }
        assertTrue(handled.get());
        try
        {
            wrapper.commit();
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testClearDelegates() throws Exception
    {
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void clearWriteLock()
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        assertFalse(handled.get());
        wrapper.clearWriteLock();
        assertTrue(handled.get());
    }

    @Test
    public void testSetModeDelegates() throws Exception
    {
        final AtomicBoolean handled = new AtomicBoolean();
        final WriterOutOfMemory wrapper = new WriterOutOfMemory(new MockWriter()
        {
            @Override
            public void setMode(final UpdateMode mode)
            {
                handled.set(true);
            }
        }, WriterOutOfMemory.NULL_HANDLER);

        assertFalse(handled.get());
        wrapper.setMode(UpdateMode.BATCH);
        assertTrue(handled.get());

        handled.set(false);
        wrapper.setMode(UpdateMode.INTERACTIVE);
        assertTrue(handled.get());
    }
}
