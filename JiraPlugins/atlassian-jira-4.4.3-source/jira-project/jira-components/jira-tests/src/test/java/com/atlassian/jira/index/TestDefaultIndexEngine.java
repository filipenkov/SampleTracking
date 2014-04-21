package com.atlassian.jira.index;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.atlassian.jira.util.searchers.MockSearcherFactory.getCleanSearcher;

import com.atlassian.jira.index.DefaultIndexEngine.FlushPolicy;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.AssertionFailedError;
import com.atlassian.jira.local.ListeningTestCase;

public class TestDefaultIndexEngine extends ListeningTestCase
{
    @Test
    public void testSearcherClosed() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return getCleanSearcher();
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return new WriterWrapper(configuration, mode);
            }
        }, configuration, FlushPolicy.NONE);

        final IndexSearcher searcher = engine.getSearcher();
        assertSame("should be same until something is written", searcher, engine.getSearcher());

        engine.write(new Index.Operation()
        {
            @Override
            void perform(final Writer writer)
            {}

            @Override
            UpdateMode mode()
            {
                return UpdateMode.INTERACTIVE;
            }
        });

        final IndexSearcher newSearcher = engine.getSearcher();

        assertNotSame(searcher, newSearcher);
    }

    @Test
    public void testWriterNotFlushedForWritePolicyNone() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final WriterWrapper writerWrapper = new WriterWrapper(configuration, UpdateMode.INTERACTIVE)
        {
            @Override
            public void close()
            {
                throw new AssertionFailedError("should not close!");
            }

            @Override
            public void commit()
            {
                throw new AssertionFailedError("should not commit!");
            }
        };
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                throw new AssertionFailedError("no searcher required");
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return writerWrapper;
            }
        }, configuration, FlushPolicy.NONE);

        engine.write(new Index.Operation()
        {
            @Override
            void perform(final Writer writer)
            {}

            @Override
            UpdateMode mode()
            {
                return UpdateMode.INTERACTIVE;
            }
        });
    }

    @Test
    public void testWriterClosedForWritePolicyClose() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final WriterWrapper writerWrapper = new WriterWrapper(configuration, UpdateMode.INTERACTIVE)
        {
            @Override
            public void close()
            {
                count.incrementAndGet();
                super.close();
            }

            @Override
            public void commit()
            {
                throw new AssertionFailedError("should not commit!");
            }
        };
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                throw new AssertionFailedError("no searcher required");
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return writerWrapper;
            }
        }, configuration, FlushPolicy.CLOSE);

        engine.write(new Index.Operation()
        {
            @Override
            void perform(final Writer writer)
            {}

            @Override
            UpdateMode mode()
            {
                return UpdateMode.INTERACTIVE;
            }
        });

        assertEquals(1, count.get());
    }

    @Test
    public void testWriterAndSearcherClosedWhenClosed() throws Exception
    {
        final AtomicInteger searcherCloseCount = new AtomicInteger();
        final AtomicInteger writerCloseCount = new AtomicInteger();
        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer());
        final WriterWrapper writerWrapper = new WriterWrapper(configuration, UpdateMode.INTERACTIVE)
        {
            @Override
            public void close()
            {
                writerCloseCount.incrementAndGet();
                super.close();
            }

            @Override
            public void commit()
            {
                throw new AssertionFailedError("should not commit!");
            }
        };
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                try
                {
                    return new IndexSearcher(directory)
                    {
                        @Override
                        public void close() throws IOException
                        {
                            searcherCloseCount.incrementAndGet();
                            super.close();
                        }
                    };
                }
                catch (final IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return writerWrapper;
            }
        }, configuration, FlushPolicy.CLOSE);

        engine.write(new Index.Operation()
        {
            @Override
            void perform(final Writer writer)
            {}

            @Override
            UpdateMode mode()
            {
                return UpdateMode.INTERACTIVE;
            }
        });
        engine.getSearcher().close();
        engine.close();

        assertEquals(1, writerCloseCount.get());
        assertEquals(1, searcherCloseCount.get());
    }

    @Test
    public void testDirectoryCleaned() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        final StandardAnalyzer analyzer = new StandardAnalyzer();
        {
            final IndexWriter writer = new IndexWriter(directory, analyzer);
            writer.addDocument(new Document());
            writer.close();
        }
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, analyzer);
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                throw new AssertionFailedError("no searcher required");
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                throw new AssertionFailedError("no writer required");
            }
        }, configuration, FlushPolicy.NONE);

        assertEquals(1, new IndexSearcher(directory).getIndexReader().numDocs());
        engine.clean();
        assertEquals(0, new IndexSearcher(directory).getIndexReader().numDocs());
    }

    @Test
    public void testDirectoryCleanThrowsRuntimeIO() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory()
        {
            @Override
            public IndexOutput createOutput(final String name) throws IOException
            {
                throw new IOException("haha");
            }
        };
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer());
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                throw new AssertionFailedError("no searcher required");
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                throw new AssertionFailedError("no writer required");
            }
        }, configuration, FlushPolicy.NONE);

        try
        {
            engine.clean();
            fail("RuntimeIOException expected");
        }
        catch (final RuntimeIOException expected)
        {
            assertTrue(expected.getMessage().contains("haha"));
        }
    }

    @Test
    public void testWriterWrapperClearLockCalledIfOutOfMemoryError() throws Exception
    {
        final AtomicBoolean clearCalled = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer());
        final DefaultIndexEngine engine = new DefaultIndexEngine(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return getCleanSearcher();
            }
        }, new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return new WriterWrapper(configuration, mode)
                {
                    @Override
                    public void commit()
                    {
                        throw new OutOfMemoryError();
                    }

                    @Override
                    public void clearWriteLock()
                    {
                        clearCalled.set(true);
                    }
                };
            }
        }, configuration, FlushPolicy.NONE);

        assertFalse(clearCalled.get());
        try
        {
            engine.write(new Index.Operation()
            {
                @Override
                void perform(final Writer writer)
                {
                    writer.commit();
                }

                @Override
                UpdateMode mode()
                {
                    return UpdateMode.INTERACTIVE;
                }
            });
            fail("OutOfMemoryError expected");
        }
        catch (final OutOfMemoryError expected)
        {}
        assertTrue(clearCalled.get());
    }
}
