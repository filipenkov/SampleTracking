package com.atlassian.jira.index;

import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.DefaultIndexEngine.FlushPolicy;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import junit.framework.AssertionFailedError;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.atlassian.jira.util.searchers.MockSearcherFactory.getCleanSearcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestDefaultIndexEngine
{
    @Mock
    private InstrumentRegistry instrumentRegistry;
    @Mock
    private Counter counter;

    @Before
    public void setUp() throws Exception
    {
        when(instrumentRegistry.pullCounter(any(String.class))).thenReturn(counter);
        final MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(InstrumentRegistry.class, instrumentRegistry);
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void testSearcherClosed() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
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
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
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
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
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
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
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
        final StandardAnalyzer analyzer = new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION);
        {
            IndexWriterConfig conf = new IndexWriterConfig(DefaultIndexManager.LUCENE_VERSION, analyzer);
            final IndexWriter writer = new IndexWriter(directory, conf);
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
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
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

    /**
     * Test the simple flow of get searcher / write / close searcher / getSearcher
     * correctly gets a new reader and closes the old reader.
     * @throws Exception
     */
    @Test
    public void testSimpleFlowReaderIsClosed() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher searcher = engine.getSearcher();
        final IndexReader reader = searcher.getIndexReader();
        writeTestDocument(engine);
        searcher.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertNotSame(searcher, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertNotSame(reader, newReader);
        assertReaderClosed(reader);
        assertReaderOpen(newReader);
    }

    /**
     * Test se just get the same searcher and reader when there are no writes.
     * @throws Exception
     */
    @Test
    public void testMultipleSearchersWithoutWrites() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher searcher = engine.getSearcher();
        final IndexReader reader = searcher.getIndexReader();
        searcher.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertSame(searcher, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertSame(reader, newReader);
        assertReaderOpen(reader);
    }

    /**
     * Test the old reader is still open until all searchers using it are closed
     * @throws Exception
     */
    @Test
    public void testOldReaderStillOpenTillAllSearchersClosed() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher oldSearcher1 = engine.getSearcher();
        final IndexSearcher oldSearcher2 = engine.getSearcher();
        final IndexSearcher oldSearcher3 = engine.getSearcher();
        final IndexReader reader = oldSearcher1.getIndexReader();
        assertSame("should be same until something is written", oldSearcher1, oldSearcher2);
        assertSame("should be same until something is written", oldSearcher1, oldSearcher3);

        writeTestDocument(engine);

        oldSearcher1.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertNotSame(oldSearcher1, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertNotSame(reader, newReader);
        assertReaderOpen(reader);
        assertReaderOpen(newReader);
        oldSearcher2.close();
        assertReaderOpen(reader);
        oldSearcher3.close();
        assertReaderClosed(reader);
        assertReaderOpen(newReader);

    }

    /**
     * Test the old reader is still open until all searchers using it are closed
     * @throws Exception
     */
    @Test
    public void testMultiWritesBetweenSearcherCloses() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher oldSearcher1 = engine.getSearcher();
        final IndexSearcher oldSearcher2 = engine.getSearcher();
        assertSame("should be same until something is written", oldSearcher1, oldSearcher2);

        writeTestDocument(engine);
        writeTestDocument(engine);
        writeTestDocument(engine);
        writeTestDocument(engine);
        final IndexSearcher oldSearcher3 = engine.getSearcher();
        assertNotSame(oldSearcher1, oldSearcher3);
        writeTestDocument(engine);
        writeTestDocument(engine);
        final IndexSearcher oldSearcher4 = engine.getSearcher();
        final IndexSearcher oldSearcher5 = engine.getSearcher();
        assertNotSame(oldSearcher1, oldSearcher4);
        assertNotSame(oldSearcher3, oldSearcher4);
        assertSame(oldSearcher4, oldSearcher5);

        assertReaderOpen(oldSearcher1.getIndexReader());
        assertReaderOpen(oldSearcher2.getIndexReader());
        assertReaderOpen(oldSearcher3.getIndexReader());
        assertReaderOpen(oldSearcher4.getIndexReader());
        assertReaderOpen(oldSearcher5.getIndexReader());

        oldSearcher1.close();
        oldSearcher2.close();
        oldSearcher3.close();
        oldSearcher4.close();
        oldSearcher5.close();

        assertReaderClosed(oldSearcher1.getIndexReader());
        assertReaderClosed(oldSearcher2.getIndexReader());
        assertReaderClosed(oldSearcher3.getIndexReader());
        assertReaderOpen(oldSearcher4.getIndexReader());
        assertReaderOpen(oldSearcher5.getIndexReader());

        writeTestDocument(engine);
        assertReaderOpen(oldSearcher4.getIndexReader());
        // Getting a new searcher -> gets a new reader -> closes the old reader
        final IndexSearcher oldSearcher6 = engine.getSearcher();
        assertReaderClosed(oldSearcher4.getIndexReader());
        assertReaderClosed(oldSearcher5.getIndexReader());

    }

    private void assertReaderClosed(IndexReader reader) throws IOException
    {
        // If the reader is closed. flush will throw an AlreadyClosedException
        try
        {
            reader.flush();
            fail("The reader should have been closed after a write when we get a new searcher");
        }
        catch (AlreadyClosedException e)
        {
            assertTrue(true);
        }
    }

    private void assertReaderOpen(IndexReader reader) throws IOException
    {
        // If the reader is closed. flush will throw an AlreadyClosedException
        try
        {
            reader.flush();
        }
        catch (AlreadyClosedException e)
        {
            fail("The reader should not have been closed.");
        }
    }

    private void writeTestDocument(DefaultIndexEngine engine) throws IOException
    {Document d = new Document();
        d.add(new Field("test", "bytes".getBytes()));
        engine.write(Operations.newCreate(d, UpdateMode.INTERACTIVE));
    }

    private DefaultIndexEngine getRamDirectory() throws IOException
    {
        final RAMDirectory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(DefaultIndexManager.LUCENE_VERSION, new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(directory, conf).close();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        return new DefaultIndexEngine(configuration, FlushPolicy.FLUSH);
    }

    @After
    public void teardown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
    }
}
