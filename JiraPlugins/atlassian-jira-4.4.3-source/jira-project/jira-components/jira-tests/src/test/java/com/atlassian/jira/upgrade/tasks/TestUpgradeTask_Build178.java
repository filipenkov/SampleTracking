/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.upgrade.tasks;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.upgrade.LogEvent;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.web.action.admin.index.IndexAdminImpl;
import com.atlassian.johnson.event.EventType;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.ListeningTestCase;

public class TestUpgradeTask_Build178 extends ListeningTestCase
{
    private UpgradeTask_Build178 upgradeTask;
    private Mock mockIssueIndexManager;
    private RAMDirectory directory;

    @Test
    public void testBuildNoAndDescription()
    {
        assertEquals("178", upgradeTask.getBuildNumber());
    }

    @Before
    public void setUp() throws Exception
    {
        directory = new RAMDirectory();

        mockIssueIndexManager = new Mock(IssueIndexManager.class);
        mockIssueIndexManager.setStrict(true);

        upgradeTask = new UpgradeTask_Build178((IssueIndexManager) mockIssueIndexManager.proxy());
    }

    @After
    public void tearDown() throws Exception
    {
        directory = null;
        mockIssueIndexManager = null;
        upgradeTask = null;
    }

    @Test
    public void testDescription()
    {
        assertNotNull(upgradeTask.getShortDescription());
        assertTrue(upgradeTask.getShortDescription().indexOf("Upgrade to build number") == -1);
        assertTrue(upgradeTask.getShortDescription().length() > 40);
    }

    @Test
    public void testNullDependencies()
    {
        try
        {
            new UpgradeTask_Build178(null);
            fail("Expected NPE");
        }
        catch (final NullPointerException yay)
        {}
    }

    @Test
    public void testUpgradeNotDoneIfIndexingDisabled() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.FALSE);

        upgradeTask.doUpgrade(false);

        // verifies that reindexall not called
        mockIssueIndexManager.verify();
    }

    @Test
    public void testUpgradeNotDoneIfIndexUpToDateWith200Docs() throws Exception
    {
        _testUpgradeNotDone(200);
    }

    @Test
    public void testUpgradeNotDoneIfIndexUpToDateWith30Docs() throws Exception
    {
        _testUpgradeNotDone(30);
    }

    @Test
    public void testUpgradeNotDoneIfIndexUpToDateWith1Doc() throws Exception
    {
        _testUpgradeNotDone(1);
    }

    @Test
    public void testUpgradeDoneWithEmptyIndex() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("size", new Integer(0));
        createDocuments(0, new Date());
        _testUpgradeDone();
    }

    @Test
    public void testSearcherClosedWhenProblemsOccurConstructingAndUsingReader() throws Exception
    {
        createDocuments(200, new Date());

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final IndexSearcher searcher = new IndexSearcher(directory)
        {
            @Override
            public void close() throws IOException
            {
                closeCalled.set(true);
                super.close();
            }

            @Override
            public IndexReader getIndexReader()
            {
                throw new UnsupportedOperationException("uh oh...");
            }
        };
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);

        // should not fail
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
        assertTrue(closeCalled.get());
    }

    @Test
    public void testUpgradeNotDoneWhenProblemsOccurConstructingSearcher() throws Exception
    {
        createDocuments(200, new Date());

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        mockIssueIndexManager.expectAndThrow("getCommentSearcher", new IndexException("bad, bad indexes"));

        // should not fail
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
    }

    @Test
    public void testUpgradeDoneIfIndexNotUpToDateWith1Doc() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("size", new Integer(1));
        _testUpgradeDoneIfNotUpToDate(1);
    }

    @Test
    public void testUpgradeDoneIfIndexNotUpToDateWith30Docs() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("size", new Integer(30));
        _testUpgradeDoneIfNotUpToDate(30);
    }

    @Test
    public void testUpgradeDoneIfIndexNotUpToDateWith200Docs() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("size", new Integer(200));
        _testUpgradeDoneIfNotUpToDate(200);
    }

    private void _testUpgradeDoneIfNotUpToDate(final int numDocs) throws Exception
    {
        createDocuments(numDocs, null);

        _testUpgradeDone();
    }

    @Test
    public void testUpgradeDoneEvenThoughDocumentsDeleted() throws Exception
    {
        createDocuments(100, null);

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        mockIssueIndexManager.expectAndReturn("size", new Integer(100));
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final IndexReader reader = IndexReader.open(directory);
        reader.deleteDocument(0);
        final IndexSearcher searcher = new IndexSearcher(reader)
        {
            @Override
            public void close() throws IOException
            {
                closeCalled.set(true);
                super.close();
            }
        };
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);
        mockIssueIndexManager.expectAndReturn("reIndexAll", P.args(P.isA(Context.class)), new Long(10));
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
        assertTrue(closeCalled.get());
        reader.close();
    }

    @Test
    public void testUpgradeNotDoneEvenThoughDocumentsDeleted() throws Exception
    {
        createDocuments(100, new Date());

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final IndexReader reader = IndexReader.open(directory);
        reader.deleteDocument(0);
        reader.deleteDocument(3);
        reader.deleteDocument(16);
        reader.deleteDocument(43);
        reader.deleteDocument(57);
        reader.deleteDocument(88);
        reader.deleteDocument(99);
        final IndexSearcher searcher = new IndexSearcher(reader)
        {
            @Override
            public void close() throws IOException
            {
                closeCalled.set(true);
                super.close();
            }
        };
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
        assertTrue(closeCalled.get());
        reader.close();
    }

    @Test
    public void testUpgradeNotDoneEvenThoughAlmostAllDocumentsDeleted() throws Exception
    {
        createDocuments(10, new Date());

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final IndexReader reader = IndexReader.open(directory);
        for (int i = 0; i < 9; i++)
        {
            reader.deleteDocument(i);
        }
        final IndexSearcher searcher = new IndexSearcher(reader)
        {
            @Override
            public void close() throws IOException
            {
                closeCalled.set(true);
                super.close();
            }
        };
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
        assertTrue(closeCalled.get());
        reader.close();
    }

    private void _testUpgradeDone() throws IOException
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        final IndexSearcher searcher = new IndexSearcher(directory);
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);
        mockIssueIndexManager.expectAndReturn("reIndexAll", P.args(P.isA(Context.class)), new Long(10));

        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
    }

    private void _testUpgradeNotDone(final int numDocs) throws Exception
    {
        createDocuments(numDocs, new Date());

        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        final AtomicBoolean closeCalled = new AtomicBoolean(false);
        final IndexSearcher searcher = new IndexSearcher(directory)
        {
            @Override
            public void close() throws IOException
            {
                closeCalled.set(true);
                super.close();
            }
        };
        mockIssueIndexManager.expectAndReturn("getCommentSearcher", searcher);
        upgradeTask.doUpgrade(false);

        // verifies reindexAll is not called as the index is up to date
        mockIssueIndexManager.verify();
        assertTrue(closeCalled.get());
    }

    private void createDocuments(final int numDocs, final Date createdDate) throws Exception
    {
        final IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(), true);
        for (int i = 1; i <= numDocs; i++)
        {
            // created date, re-index must have been done already
            writer.addDocument(createCommentDocument(i, createdDate));
        }
        writer.close();
    }

    private Document createCommentDocument(final int i, final Date createdDate)
    {
        final Document doc = new Document();

        doc.add(new Field(DocumentConstants.PROJECT_ID, String.valueOf(i), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.ISSUE_ID, String.valueOf(i), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.COMMENT_ID, String.valueOf(i), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.COMMENT_AUTHOR, "fred", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.COMMENT_BODY, "this is a comment. #" + i, Field.Store.YES, Field.Index.TOKENIZED));
        if (createdDate != null)
        {
            doc.add(new Field(DocumentConstants.COMMENT_CREATED, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }
        return doc;
    }

    @Test
    public void testEventConstructor()
    {
        final LogEvent event = new LogEvent(Logger.getLogger(TestUpgradeTask_Build178.class), "reindex", IndexAdminImpl.JIRA_IS_BEING_REINDEXED,
            "message");
        assertNotNull(event.getKey());
        assertEquals(EventType.get("reindex"), event.getKey());
        assertEquals(IndexAdminImpl.JIRA_IS_BEING_REINDEXED, event.getDesc());

        try
        {
            new LogEvent(null, "reindex", "description", "message");
            fail("NPE expected");
        }
        catch (final NullPointerException yay)
        {}
    }

    @Test
    public void testEventLogging()
    {
        final List<Object> msgs = new ArrayList<Object>();
        final Logger log = new Logger(this.getClass().getName())
        {
            @Override
            public void info(final Object object)
            {
                msgs.add(object);
            }
        };
        final LogEvent event = new LogEvent(log, "type", "description", "message");

        event.setProgress(0);
        event.setProgress(20);
        event.setProgress(40);
        event.setProgress(60);
        event.setProgress(80);
        event.setProgress(100);

        assertEquals(6, msgs.size());
    }

    @Test
    public void testEventLogsOnlyIfProgressChanges()
    {
        final List<Object> msgs = new ArrayList<Object>();
        final Logger log = new Logger(this.getClass().getName())
        {
            @Override
            public void info(final Object object)
            {
                msgs.add(object);
            }
        };
        final LogEvent event = new LogEvent(log, "type", "description", "message {0}%");

        for (int i = 0; i <= 1000; i++)
        {
            event.setProgress(i / 10);
        }

        assertEquals(101, msgs.size());

        int i = 0;
        for (final Object element : msgs)
        {
            final String msg = (String) element;
            assertTrue(msg, msg.indexOf(String.valueOf(i++)) > -1);
        }
    }
}
