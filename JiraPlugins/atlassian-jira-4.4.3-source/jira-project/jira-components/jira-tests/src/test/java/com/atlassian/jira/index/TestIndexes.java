package com.atlassian.jira.index;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;

public class TestIndexes extends ListeningTestCase
{
    @Test
    public void testQueuedManagerNewSearcherAfterCreate() throws Exception
    {
        final Index.Manager manager = Indexes.createQueuedIndexManager("TestQueuedManager", new DefaultConfiguration(
            MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer()));
        try
        {
            final IndexSearcher searcher = manager.getSearcher();
            manager.getIndex().perform(Operations.newCreate(new Document(), UpdateMode.INTERACTIVE)).await();
            final IndexSearcher searcher2 = manager.getSearcher();
            assertNotSame(searcher, searcher2);

            searcher.close();
            searcher2.close();
        }
        finally
        {
            manager.close();
        }
    }

    @Test
    public void testQueuedManagerNewSearcherAfterUpdate() throws Exception
    {
        final Index.Manager manager = Indexes.createQueuedIndexManager("TestQueuedManager", new DefaultConfiguration(
            MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer()));
        try
        {
            final IndexSearcher searcher = manager.getSearcher();
            manager.getIndex().perform(Operations.newUpdate(new Term("test", "1"), new Document(), Index.UpdateMode.INTERACTIVE)).await();
            final IndexSearcher searcher2 = manager.getSearcher();
            assertNotSame(searcher, searcher2);
            searcher.close();
            searcher2.close();
        }
        finally
        {
            manager.close();
        }
    }

    @Test
    public void testQueuedManagerNewSearcherAfterDelete() throws Exception
    {
        final Index.Manager manager = Indexes.createQueuedIndexManager("TestQueuedManager", new DefaultConfiguration(
            MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer()));
        try
        {
            final IndexSearcher searcher = manager.getSearcher();
            manager.getIndex().perform(Operations.newDelete(new Term("test", "1"), UpdateMode.INTERACTIVE)).await();
            final IndexSearcher searcher2 = manager.getSearcher();
            assertNotSame(searcher, searcher2);

            searcher.close();
            searcher2.close();
        }
        finally
        {
            manager.close();
        }
    }

    @Test
    public void testSimpleManagerNewSearcherAfterDelete() throws Exception
    {
        final Index.Manager manager = Indexes.createSimpleIndexManager(new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(),
            new StandardAnalyzer()));
        try
        {
            final IndexSearcher searcher = manager.getSearcher();
            manager.getIndex().perform(Operations.newCreate(new Document(), UpdateMode.INTERACTIVE)).await();
            final IndexSearcher searcher2 = manager.getSearcher();
            assertNotSame(searcher, searcher2);

            searcher.close();
            searcher2.close();
        }
        finally
        {
            manager.close();
        }
    }
}
