package com.atlassian.jira.index;

import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestDefaultManager extends ListeningTestCase
{
    @Test
    public void testDeleteCallsClean() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Override
            public void clean()
            {
                called.set(true);
            }
        }, new MockIndex());
        assertFalse(called.get());
        manager.deleteIndexDirectory();
        assertTrue(called.get());
    }

    @Test
    public void testGetSearcherCallsEngine() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Override
            public IndexSearcher getSearcher()
            {
                called.set(true);
                return null;
            }
        }, new MockIndex());
        assertFalse(called.get());
        assertNull(manager.getSearcher());
        assertTrue(called.get());
    }

    @Test
    public void testGetNumDocsCallsEngineSearcher() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Override
            public IndexSearcher getSearcher()
            {
                called.set(true);
                try
                {
                    return new IndexSearcher(configuration.getDirectory());
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, new MockIndex());
        assertFalse(called.get());
        assertEquals(0, manager.getNumDocs());
        assertTrue(called.get());
    }

    @Test
    public void testCloseCallsEngine() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex()
        {
            @Override
            public void close()
            {
                called.set(true);
            }
        });
        assertFalse(called.get());
        manager.close();
        assertTrue(called.get());
    }

    @Test
    public void testGetIndexReturnsSame() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final MockIndex index = new MockIndex();
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), index);
        assertSame(index, manager.getIndex());
    }

    @Test
    public void testIndexCreatedTrue() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex());
        assertTrue(manager.isIndexCreated());
    }

    @Test
    public void testIndexCreatedFalse() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(DefaultIndexManager.LUCENE_VERSION));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex());
        assertFalse(manager.isIndexCreated());
    }
}
