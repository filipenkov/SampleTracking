/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.admin.ListenerCreate;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Name;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.local.AbstractIndexingTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.searchers.MockSearcherFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.google.common.collect.Lists;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.Scheduler;
import webwork.action.Action;
import webwork.action.ActionSupport;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TestDefaultIssueIndexManager extends AbstractIndexingTestCase
{
    private static final class Id
    {
        static final Long ONE = 1L;
        static final Long TEN = 10L;
        static final Long HUNDRED = 100L;
        static final Long TEN_THOUSAND = 10000L;
    }

    private static final String ISSUE1_ID = "1";
    private static final String ISSUE2_ID = "2";

    private static final Long CHANGEGROUP1_ID=101l;

    private static final String UNINDEXED = "UNINDEXED";
    @SuppressWarnings("unchecked")
    private static final Map<String, String> DBToIssueDocumentConstants = Collections.unmodifiableMap(EasyMap.build("id", DocumentConstants.ISSUE_ID,
        "project", DocumentConstants.PROJECT_ID, "type", UNINDEXED));
    @SuppressWarnings("unchecked")
    private static final Map<String, String> DBToCommentDocumentConstants = Collections.unmodifiableMap(EasyMap.build("id",
        DocumentConstants.COMMENT_ID, "issue", DocumentConstants.ISSUE_ID, "project", DocumentConstants.PROJECT_ID, "type", UNINDEXED, "author",
        DocumentConstants.COMMENT_AUTHOR));

    //adding a time-stamp to the directory to make sure it's unique and multiple unit tests running at the same time wont interfere with each other.
    private final String indexDirectory = System.getProperty("java.io.tmpdir") + java.io.File.separator + TestDefaultIssueIndexManager.class.getName() + System.currentTimeMillis();
    private Mock mockApplicationProperties;
    private Mock mockScheduler;
    private DefaultIndexManager indexManager;
    private IndexDirectoryFactory indexDirectoryFactory;

    private Directory issueDirectory;
    private Directory commentDirectory;
    private Directory changesDirectory;

    private IndexPathManager indexPath;
    private FieldVisibilityManager origFieldVisibilityManager;

    private ReindexMessageManager reindexMessageManager;

    public TestDefaultIssueIndexManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        reindexMessageManager = EasyMock.createMock(ReindexMessageManager.class);

        origFieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);

        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();

        EasyMock.replay(visibilityBean, reindexMessageManager);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);

        indexPath = new MockIndexPathManager();
        mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockScheduler = new Mock(Scheduler.class);

        issueDirectory = MockSearcherFactory.getCleanRAMDirectory();
        commentDirectory = MockSearcherFactory.getCleanRAMDirectory();
        changesDirectory = MockSearcherFactory.getCleanRAMDirectory();

        ManagerFactory.addService(Scheduler.class, (Scheduler) mockScheduler.proxy());
        indexDirectoryFactory = new MockIndexDirectoryFactory(new Function<Name, Directory>()
        {
            public Directory get(final Name input)
            {
                if (input == Name.ISSUE)
                {
                    return issueDirectory;
                }
                else if (input == Name.COMMENT)
                {
                    return commentDirectory;
                }
                else if (input == Name.CHANGE_HISTORY)
                {
                    return changesDirectory;
                }
                throw new UnsupportedOperationException("unknown indexType: " + input);
            }
        });
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new DefaultIssueIndexer(indexDirectoryFactory, new MemoryIssueIndexer.CommentRetrieverImpl(
                ComponentAccessor.getIssueManager()), new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(ComponentAccessor.getIssueManager())), indexPath, reindexMessageManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(Scheduler.class);
        ManagerFactory.addService(FieldVisibilityManager.class, origFieldVisibilityManager);
        super.tearDown();
    }

    public void testIndexChanges() throws Exception
    {
        prepareIndexDir();
        UtilsForTests.getTestEntity("Project", EasyMap.build("id", Id.TEN, "name", "A Project"));

        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));
        UtilsForTests.getTestEntity("ChangeGroup",EasyMap.build("id",CHANGEGROUP1_ID,"issue",Id.ONE,"author","user","created", new Date(System.currentTimeMillis())));
        UtilsForTests.getTestEntity("ChangeItem",EasyMap.build("id", Id.TEN,"group", CHANGEGROUP1_ID,"oldstring","Open","newstring","Closed","oldvalue","1", "newvalue","5","field","status"));
        UtilsForTests.getTestEntity("ChangeItem",EasyMap.build("id", Id.HUNDRED,"group", CHANGEGROUP1_ID,"oldvalue","Fred","newvalue","Barney","field","assignee"));

        prepareMockIndexManager();
        indexManager.reIndexAll();

        final IndexSearcher issueSearcher = new IndexSearcher(getChangeIndexDirectory());
        try
        {
            TopDocs hits = issueSearcher.search(new TermQuery(new Term("ch_who", "ch-user")), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            hits = issueSearcher.search(new TermQuery(new Term("status.ch_from", "ch-open")), Integer.MAX_VALUE);
            assertEquals(1,hits.totalHits);
            //assignee is not a suuported field
            hits = issueSearcher.search(new TermQuery(new Term("assignee.ch_to", "ch-barney")), Integer.MAX_VALUE);
            assertEquals(0,hits.totalHits);
        }

        finally
        {
            issueSearcher.close();
        }
        verifyMocks(reindexMessageManager);

    }

    public void testIndexSearchIssue() throws Exception
    {
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));

        prepareMockIndexManager();

        indexManager.reIndexAll();

        final IndexSearcher issueSearcher = new IndexSearcher(getIssueIndexDirectory());
        try
        {
            TopDocs hits = issueSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_KEY, "ABC-7348")), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            final QueryParser parser = new QueryParser(DocumentConstants.ISSUE_DESC, DefaultIndexManager.ANALYZER_FOR_SEARCHING);
            hits = issueSearcher.search(parser.parse("body"), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            hits = issueSearcher.search(parser.parse("shouldn't"), Integer.MAX_VALUE);
            assertEquals(0, hits.totalHits);
        }
        finally
        {
            issueSearcher.close();
        }

        verifyMocks(reindexMessageManager);
    }

    private void prepareMockIndexManager()
    {
        EasyMock.reset(reindexMessageManager);
        reindexMessageManager.clear();
        EasyMock.expectLastCall();
        EasyMock.replay(reindexMessageManager);
    }

    public void testIndexLookupIssue() throws Exception
    {
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));

        prepareMockIndexManager();

        final Map<Name, Directory> map = new HashMap<Name, Directory>();
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new DefaultIssueIndexer(new MockIndexDirectoryFactory(
                new Function<Name, Directory>()
                {
                    public Directory get(final Name input)
                    {
                        Directory directory = map.get(input);
                        if (directory == null)
                        {
                            directory = MockSearcherFactory.getCleanRAMDirectory();
                            map.put(input, directory);
                        }
                        return directory;
                    }
                }), new MemoryIssueIndexer.CommentRetrieverImpl(ComponentAccessor.getIssueManager()), new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(ComponentAccessor.getIssueManager())), indexPath, reindexMessageManager);
        indexManager.reIndexAll();

        final IndexReader reader = IndexReader.open(map.get(Name.ISSUE));
        try
        {
            assertEquals(1, reader.numDocs());

            final Document doc = reader.document(0);
            assertEquals("1", doc.get(DocumentConstants.ISSUE_ID));
            assertEquals("ABC-7348", doc.get(DocumentConstants.ISSUE_KEY));
        }
        finally
        {
            reader.close();
        }

        verifyMocks(reindexMessageManager);
    }

    public void testShutdown() throws Exception
    {
        final Mock mockIssueIndexer = new Mock(IssueIndexer.class);
        mockIssueIndexer.setStrict(true);
        mockIssueIndexer.expectVoid("shutdown");

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), (IssueIndexer) mockIssueIndexer.proxy(), indexPath, reindexMessageManager);
        // Do the right thang
        indexManager.shutdown();

        mockIssueIndexer.verify();
        verifyMocks();
    }

    public void testDeIndex() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        // Create some comments for the issue
        UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author",
            "somedude", "body", "Here we have a comment"));
        UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author",
            "somedude2", "body", "Here we have another comment"));

        // Create another issue
        final GenericValue issueGV2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project",
            Id.TEN, "description", "This is the another body", "summary", "Another Issue"));
        // Create comments for the issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude", "body", "Here we have stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude2", "body", "Here we have another stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude3", "body", "Here we have abc stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude4", "body", "Here we have xyz stuff")));

        indexIssue(issueGV, 1, 2);
        indexIssue(issueGV2, 2, 6);

        // Do the right thing
        indexManager.deIndex(issueGV);

        // Assert the second issue is left in the index
        assertIndexContainsIssue(1, ISSUE2_ID);
        // Assert that second issue's comments are in the index
        assertIndexContainsComments(commentGVs, 4);

        // Ensure all the methods were called
        verifyMocks();

        //yay
    }

    private void prepareIndexDir() throws IOException
    {
        final java.io.File issueIndexDirectory = new java.io.File(indexDirectory + "/issues");
        final java.io.File commentsIndexDirectory = new java.io.File(indexDirectory + "/comments");
        final java.io.File changesIndexDirectory = new java.io.File(indexDirectory + "/changes");

        issueIndexDirectory.mkdirs();
        commentsIndexDirectory.mkdirs();
        changesIndexDirectory.mkdirs();

        // Initialise the issues, comments and changes directories
        new IndexWriter(getIssueIndexDirectory(), JiraAnalyzer.ANALYZER_FOR_INDEXING, true).close();
        new IndexWriter(getCommentsIndexDirectory(), JiraAnalyzer.ANALYZER_FOR_INDEXING, true).close();
        new IndexWriter(getChangeIndexDirectory(), JiraAnalyzer.ANALYZER_FOR_INDEXING, true).close();
    }

    public void testDeIndexNotAnIssue() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        // Create comments for issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude2", "body", "Here we have another comment")));

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", Id.ONE, "name", "A Project"));

        indexIssue(issueGV, 1, 2);

        // Do the right thing
        indexManager.deIndex(projectGV);

        // Assert that an issue was *not* deleted
        assertIndexContainsIssue(1, "1");
        // Assert that issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);

        // Ensure all the methods were called
        verifyMocks();

        // true anality would test here if something got logged...
        //yay
    }

    private Documents IndexChange(final GenericValue issueGV, final int issueCount, final int commentCount, final int changeCount)
            throws IOException, GenericEntityException
    {
        return indexIssue(issueGV, issueCount, commentCount);
    }

    private Documents indexIssue(final GenericValue issueGV, final int issueCount, final int commentCount)
            throws IOException, GenericEntityException
    {
        // Put an issue issueDocument in there
        final Document issueDocument = new Document();

        issueDocument.add(new Field(DocumentConstants.ISSUE_ID, issueGV.getLong("id").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_KEY, issueGV.getString("key"), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(DocumentConstants.PROJECT_ID, issueGV.getLong("project").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_DESC, issueGV.getString("description"), Field.Store.YES, Field.Index.ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_SUMMARY, issueGV.getString("summary"), Field.Store.YES, Field.Index.ANALYZED));

        IndexWriter indexWriter = null;
        try
        {
            // Create a new index
            indexWriter = new IndexWriter(getIssueIndexDirectory(), JiraAnalyzer.ANALYZER_FOR_INDEXING, false);
            indexWriter.addDocument(issueDocument);
        }
        finally
        {
            if (indexWriter != null)
            {
                indexWriter.close();
            }
        }

        final Document createdIssueDocument = assertIndexContainsIssue(issueCount, issueGV.getLong("id").toString());

        final List<GenericValue> commentGVs = issueGV.getRelatedByAnd("ChildAction", MapBuilder.build("type", ActionConstants.TYPE_COMMENT));
        final List<Document> createdCommentDocs = new ArrayList<Document>(commentGVs.size());

        if (!commentGVs.isEmpty())
        {

            try
            {
                // Create a new index
                indexWriter = new IndexWriter(getCommentsIndexDirectory(), JiraAnalyzer.ANALYZER_FOR_INDEXING, false);

                for (final Object element : commentGVs)
                {
                    final GenericValue commentGV = (GenericValue) element;
                    final Document doc = new Document();
                    final String body = commentGV.getString("body");
                    if (body != null)
                    {
                        doc.add(new Field(DocumentConstants.PROJECT_ID, String.valueOf(issueGV.getLong("project")), Field.Store.YES,
                            Field.Index.NOT_ANALYZED));
                        doc.add(new Field(DocumentConstants.ISSUE_ID, String.valueOf(issueGV.getLong("id")), Field.Store.YES,
                            Field.Index.NOT_ANALYZED));
                        doc.add(new Field(DocumentConstants.COMMENT_ID, commentGV.getString("id"), Field.Store.YES, Field.Index.NOT_ANALYZED));

                        final String author = commentGV.getString("author");
                        if (author != null) //can't add null keywords
                        {
                            doc.add(new Field(DocumentConstants.COMMENT_AUTHOR, author, Field.Store.YES, Field.Index.NOT_ANALYZED));
                        }

                        doc.add(new Field(DocumentConstants.COMMENT_BODY, body, Field.Store.YES, Field.Index.ANALYZED));
                        FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL, commentGV.getString("level"),
                            BaseFieldIndexer.NO_VALUE_INDEX_VALUE);
                        FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL_ROLE, commentGV.getString("rolelevel"),
                            BaseFieldIndexer.NO_VALUE_INDEX_VALUE);

                        indexWriter.addDocument(doc);
                    }
                }
            }
            finally
            {
                indexWriter.close();
            }

            // Assert comments are in the index
            createdCommentDocs.addAll(assertIndexContainsComments(commentGVs, commentCount));
        }

        return new Documents(createdIssueDocument, createdCommentDocs);
    }

    public void testDeIndexCouldNotGetLock() throws Exception
    {
        prepareIndexDir();

        final AtomicBoolean lockCalled = new AtomicBoolean(false);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath, reindexMessageManager)
        {
            @Override
            boolean getIndexLock()
            {
                lockCalled.set(true);
                return false;
            }
        };

        // Create an issue for testing
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude2", "body", "Here we have another comment")));

        indexIssue(issueGV, 1, 2);

        // Do the right thing
        indexManager.deIndex(issueGV);

        // Ensure that the issue was *not* deindexed
        assertIndexContainsIssue(1, "1");
        // Ensure issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);

        // Ensure the getIndexLock() method was invoked
        assertTrue(lockCalled.get());

        // Verify mocks
        verifyMocks();
    }

    public void testDeIndexCouldNotGetLockCallsToGetStringKey() throws Exception
    {
        prepareIndexDir();

        final AtomicBoolean lockCalled = new AtomicBoolean(false);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath, reindexMessageManager)
        {
            /* @Override */
            boolean getIndexLock()
            {
                lockCalled.set(true);
                return false;
            }
        };

        final AtomicBoolean getStringCalled = new AtomicBoolean(false);
        // Create an issue for testing
        final GenericValue issueGV = new MockGenericValue("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN, "description",
            "This is the body", "summary", "An Issue"))
        {
            public String getString(final String string)
            {
                if ("key".equals(string))
                {
                    getStringCalled.set(true);
                }
                else
                {
                    fail("wah, he's calling getString with: " + string);
                }
                return super.getString(string);
            }
        };

        // Do the right thing
        indexManager.deIndex(issueGV);

        // Ensure the getIndexLock() method was invoked
        assertTrue(lockCalled.get());
        assertTrue(getStringCalled.get());

        // Verify mocks
        verifyMocks();
    }

    public void testReIndexChucksNPE() throws Exception
    {
        final Context event = Contexts.nullContext();
        try
        {
            indexManager.reIndexIssues(null, event);
            fail("IllegalArg expected but not thrown.");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    public void testReIndexAllChucksRuntimeExceptionNotIndexExFromIndexerDeleteAndReinit() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public void shutdown()
            {
            // Called from reIndexAll()
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, reindexMessageManager);
        final Context event = Contexts.nullContext();

        try
        {
            indexManager.reIndexAll();
            fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {}

        try
        {
            indexManager.reIndexAll(event);
            fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    public void testReIndexAllChucksRuntimeExceptionNotIndexExFromIndexerShutdown() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer();

        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, reindexMessageManager);
        final Context event = Contexts.nullContext();

        try
        {
            indexManager.reIndexAll();
            fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {}

        try
        {
            indexManager.reIndexAll(event);
            fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    public void testReIndexChucksRuntimeException() throws Exception
    {
        // If we have a Runtime Exception which is *not* a LuceneException thrown by the indexing
        // code, then it should not be wrapped into a LuceneException

        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public Index.Result reindexIssues(final EnclosedIterable<Issue> issues, final Context event)
            {
                throw new IllegalArgumentException("ha ha");
            }

            @Override
            public void shutdown()
            {
            // Called from reIndexAll()
            }

            @Override
            public void deleteIndexes()
            {
            // Called from reIndexAll()
            }

            @Override
            public Result indexIssuesBatchMode(final EnclosedIterable<Issue> issuesIterable, final Context event)
            {
                throw new IllegalArgumentException("botched");
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, reindexMessageManager);
        final Context context = Contexts.nullContext();

        // make sure we call all the methods that delegate to reindex(IssueIterable, Context)
        try
        {
            indexManager.reIndex(new MockIssue());
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            indexManager.reIndexIssueObjects(Lists.newArrayList(new MockIssue()));
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            final GenericValue issueGV = new MockGenericValue("Issue", Collections.EMPTY_MAP);
            indexManager.reIndex(issueGV);
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            final GenericValue issueGV = new MockGenericValue("Issue", Collections.EMPTY_MAP);
            indexManager.reIndexIssues(Lists.newArrayList(issueGV));
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            indexManager.reIndexIssues(new IssueObjectIssuesIterable(Lists.newArrayList(new MockIssue())), context);
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            indexManager.reIndexAll();
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}

        try
        {
            indexManager.reIndexAll(context);
            fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {}
    }

    public void testReIndex() throws Exception
    {
        // Need to ensure that the project exists, as when reindexing the issue the project is pulled out from a manager
        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", 11L, "name", "Test Project"));

        prepareIndexDir();
        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);

        // Create an issue for testing
        @SuppressWarnings("unchecked")
        final Map<String, ?> issue1Vals = Collections.unmodifiableMap(EasyMap.build("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", issue1Vals);
        // Create some comments for the issue
        @SuppressWarnings("unchecked")
        final Map<String, ?> comment1Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body",
                "Here we have a comment")).getAllFields());
        @SuppressWarnings("unchecked")
        final Map<String, ?> comment2Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                "Here we have another comment")).getAllFields());

        final Documents issue1Documents = indexIssue(issueGV, 1, 2);
        // Assert that the right values are in the index
        assertNotNull(issue1Documents);
        assertIssueDocumentEquals(issue1Documents.issue, issue1Vals);

        // Assert issue1's comments
        assertNotNull(issue1Documents.comments);
        assertEquals(2, issue1Documents.comments.size());
        {
            final Iterator<Document> iterator = issue1Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment1Vals);
            assertCommentDocumentEquals(iterator.next(), comment2Vals);
        }
        // Create another issue
        @SuppressWarnings("unchecked")
        Map<String, ?> issue2Vals = Collections.unmodifiableMap(EasyMap.build("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project", Id.TEN,
            "description", "This is the another body", "summary", "Another Issue"));
        final GenericValue issueGV2 = UtilsForTests.getTestEntity("Issue", issue2Vals);
        // Create comments for the issue
        @SuppressWarnings("unchecked")
        final Map<String, ?> comment3Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity("Action",
            EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body", "Here we have stuff")).getAllFields());
        @SuppressWarnings("unchecked")
        final Map<String, ?> comment4Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                "Here we have another stuff")).getAllFields());
        final GenericValue commentGV5 = UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type",
            ActionConstants.TYPE_COMMENT, "author", "somedude3", "body", "Here we have abc stuff"));
        @SuppressWarnings("unchecked")
        final Map<String, ?> comment5Vals = Collections.unmodifiableMap(commentGV5.getAllFields());
        final GenericValue commentGV6 = UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type",
            ActionConstants.TYPE_COMMENT, "author", "somedude4", "body", "Here we have xyz stuff"));
        @SuppressWarnings("unchecked")
        Map<String, ?> comment6Vals = Collections.unmodifiableMap(commentGV6.getAllFields());

        final Documents issue2Documents = indexIssue(issueGV2, 2, 6);

        // Assert that the right values are in the index
        assertNotNull(issue2Documents);
        assertIssueDocumentEquals(issue2Documents.issue, issue2Vals);

        // Assert issue2's comments
        assertNotNull(issue2Documents.comments);
        assertEquals(4, issue2Documents.comments.size());
        {
            final Iterator<Document> iterator = issue2Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment3Vals);
            assertCommentDocumentEquals(iterator.next(), comment4Vals);
            assertCommentDocumentEquals(iterator.next(), comment5Vals);
            assertCommentDocumentEquals(iterator.next(), comment6Vals);
        }
        // assert document changed
        issueGV2.set("key", "ABC-1234");
        issueGV2.set("project", 11L);
        issueGV2.set("description", "no longer stuffed");
        issueGV2.set("summary", "no, really!");
        issueGV2.store();

        issue2Vals = Collections.unmodifiableMap(issueGV2.getAllFields());

        commentGV5.remove();

        commentGV6.set("author", "somebabe");
        commentGV6.set("body", "Here we don't have anything much at all");
        // Need to store the comment - as the reindex of an issue pulls issue's comment from the database
        commentGV6.store();

        comment6Vals = Collections.unmodifiableMap(commentGV6.getAllFields());

        // Do the right thing
        final MockIssue issueToReindex = new MockIssue();
        issueToReindex.setGenericValue(issueGV2);
        issueToReindex.setProject(projectGV);

        final IndexSearcher searcher = indexManager.getIssueSearcher();

        indexManager.reIndex(issueToReindex);

        final IndexSearcher newSearcher = indexManager.getIssueSearcher();
        assertNotSame(searcher, newSearcher);

        IndexSearcher indexSearcher = null;
        try
        {
            Document issueDocument = assertIndexContainsIssue(2, ISSUE2_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, issue2Vals);
            // Assert issue2's comments
            // Open a searcher and find all issue2's documents

            indexSearcher = indexManager.getCommentSearcher(); //new IndexSearcher(getCommentsIndexDirectory());
            TopDocs hits = indexSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_ID, ISSUE2_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(3, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment3Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment4Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[2].doc), comment6Vals);
            }
            issueDocument = assertIndexContainsIssue(2, ISSUE1_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, issue1Vals);

            // Assert issue1's comments
            hits = indexSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_ID, ISSUE1_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(2, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment1Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment2Vals);
            }
        }
        finally
        {
            // Clear the thread local so that other tests are not affected
            SearcherCache.getThreadLocalCache().closeSearchers();
        }
    }

    public void testReindexAllCallsIssueIndexerBatchMode() throws Exception
    {
        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);

        prepareMockIndexManager();

        final AtomicInteger indexIssuesBatchModeCount = new AtomicInteger(0);
        final AtomicInteger deleteTheWorldCount = new AtomicInteger(0);
        final AtomicInteger optimiseCalledCount = new AtomicInteger(0);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new UnimplementedIssueIndexer()
        {
            @Override
            public Index.Result indexIssuesBatchMode(final EnclosedIterable<Issue> issues, final Context event)
            {
                // Ensure the methods are invoked in correct order
                assertEquals(1, deleteTheWorldCount.get());
                assertEquals(0, indexIssuesBatchModeCount.get());
                assertEquals(0, optimiseCalledCount.get());

                indexIssuesBatchModeCount.incrementAndGet();
                return new MockResult();
            }

            @Override
            public void deleteIndexes()
            {
                // Ensure the methods are invoked in correct order
                assertEquals(0, deleteTheWorldCount.get());
                assertEquals(0, indexIssuesBatchModeCount.get());
                assertEquals(0, optimiseCalledCount.get());

                deleteTheWorldCount.incrementAndGet();
            }

            @Override
            public Index.Result optimize()
            {
                // Ensure the methods are invoked in correct order
                assertEquals(1, deleteTheWorldCount.get());
                assertEquals(1, indexIssuesBatchModeCount.get());
                assertEquals(0, optimiseCalledCount.get());

                optimiseCalledCount.incrementAndGet();
                return new MockResult();
            }
        }, indexPath, reindexMessageManager);

        indexManager.reIndexAll(Contexts.nullContext());

        assertEquals(1, deleteTheWorldCount.get());
        assertEquals(1, indexIssuesBatchModeCount.get());
        assertEquals(1, optimiseCalledCount.get());

        EasyMock.verify(reindexMessageManager);
    }

    public void testReindexIssueObjects() throws Exception
    {
        // We would like to transform the Issue object to GenericValues before re-indexing to ensure that there
        // are no discrepancies between them. Once we move the entire system to Issue objects this will be unnecessary.
        // Until then, please do *not* change this behaviour.

        final MockGenericValue issueGV1 = new MockGenericValue("Issue", EasyMap.build("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project",
            Id.TEN, "description", "This is the body", "summary", "An Issue"));
        final MockIssue issue1 = new MockIssue();
        issue1.setGenericValue(issueGV1);

        final MockGenericValue issueGV2 = new MockGenericValue("Issue", EasyMap.build("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project",
            Id.TEN, "description", "This is the another body", "summary", "Another Issue"));
        final MockIssue issue2 = new MockIssue();
        issue2.setGenericValue(issueGV2);

        final List<? extends Issue> issueObjects = Lists.newArrayList(issue1, issue2);

        final AtomicBoolean reindexCollectionMethodCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath, reindexMessageManager)
        {
            @Override
            public long reIndexIssues(final Collection<GenericValue> issues) throws IndexException
            {
                reindexCollectionMethodCalled.set(true);

                assertNotNull(issues);
                assertEquals(2, issues.size());

                final Iterator<GenericValue> iterator = issues.iterator();
                assertSame(issueGV1, iterator.next());
                assertSame(issueGV2, iterator.next());

                return -2;
            }
        };

        final long returned = indexManager.reIndexIssueObjects(issueObjects);
        assertEquals(-2, returned);

        // Ensure the method was called
        assertTrue(reindexCollectionMethodCalled.get());
    }

    public void testReIndexNotAnIssue() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        // Create comments for issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
            "author", "somedude2", "body", "Here we have another comment")));

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", Id.ONE, "name", "A Project"));

        indexIssue(issueGV, 1, 2);

        // Do the right thing
        indexManager.reIndex(projectGV);

        // Assert that an issue was *not* deleted
        assertIndexContainsIssue(1, "1");
        // Assert that issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);

        // Ensure all the methods were called
        verifyMocks();

        // true anality would test here if something got logged...
        //yay
    }

    public void testReIndexCouldNotGetLock() throws Exception
    {
        prepareIndexDir();
        // Create an issue for testing
        @SuppressWarnings("unchecked")
        final Map issue1Vals = Collections.unmodifiableMap(EasyMap.build("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project", Id.TEN,
            "description", "This is the body", "summary", "An Issue"));
        final GenericValue issueGV = UtilsForTests.getTestEntity("Issue", issue1Vals);
        // Create some comments for the issue
        final Map comment1Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body",
                "Here we have a comment")).getAllFields());
        final Map comment2Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                "Here we have another comment")).getAllFields());

        final Documents issue1Documents = indexIssue(issueGV, 1, 2);
        // Assert that the right values are in the index
        assertNotNull(issue1Documents);
        assertIssueDocumentEquals(issue1Documents.issue, issue1Vals);

        // Assert issue1's comments
        assertNotNull(issue1Documents.comments);
        assertEquals(2, issue1Documents.comments.size());
        {
            final Iterator<Document> iterator = issue1Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment1Vals);
            assertCommentDocumentEquals(iterator.next(), comment2Vals);
        }
        // Create another issue
        final Map issue2Vals = Collections.unmodifiableMap(EasyMap.build("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project", Id.TEN,
            "description", "This is the another body", "summary", "Another Issue"));
        final GenericValue issueGV2 = UtilsForTests.getTestEntity("Issue", issue2Vals);
        // Create comments for the issue
        final Map comment3Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity("Action",
            EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body", "Here we have stuff")).getAllFields());
        final Map comment4Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
            "Action",
            EasyMap.build("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                "Here we have another stuff")).getAllFields());
        final GenericValue commentGV5 = UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type",
            ActionConstants.TYPE_COMMENT, "author", "somedude3", "body", "Here we have abc stuff"));
        final Map comment5Vals = Collections.unmodifiableMap(commentGV5.getAllFields());
        final GenericValue commentGV6 = UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issueGV2.getLong("id"), "type",
            ActionConstants.TYPE_COMMENT, "author", "somedude4", "body", "Here we have xyz stuff"));
        final Map comment6Vals = Collections.unmodifiableMap(commentGV6.getAllFields());

        final Documents issue2Documents = indexIssue(issueGV2, 2, 6);

        // Assert that the right values are in the index
        assertNotNull(issue2Documents);
        assertIssueDocumentEquals(issue2Documents.issue, issue2Vals);

        // Assert issue2's comments
        assertNotNull(issue2Documents.comments);
        assertEquals(4, issue2Documents.comments.size());
        {
            final Iterator<Document> iterator = issue2Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment3Vals);
            assertCommentDocumentEquals(iterator.next(), comment4Vals);
            assertCommentDocumentEquals(iterator.next(), comment5Vals);
            assertCommentDocumentEquals(iterator.next(), comment6Vals);
        }
        // assert document changed
        issueGV2.set("key", "ABC-1234");
        issueGV2.set("project", 11L);
        issueGV2.set("description", "no longer stuffed");
        issueGV2.set("summary", "no, really!");
        issueGV2.store();

        commentGV5.remove();

        commentGV6.set("author", "somebabe");
        commentGV6.set("body", "Here we don't have anything much at all");
        // Need to store the comment - as the reindex of an issue pulls issue's comment from the database
        commentGV6.store();

        // Do the right thing
        final MockIssue issueToReindex = new MockIssue();
        issueToReindex.setGenericValue(issueGV2);

        final AtomicBoolean methodCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath, reindexMessageManager)
        {
            boolean getIndexLock()
            {
                methodCalled.set(true);
                return false;
            }
        };

        indexManager.reIndex(issueToReindex);

        // Ensure the method was called
        assertTrue(methodCalled.get());

        // Ensure that nothing has changed as the lock was not obtained
        IndexSearcher indexSearcher = null;
        try
        {
            Document issueDocument = assertIndexContainsIssue(2, ISSUE2_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, issue2Vals);
            // Assert issue2's comments
            // Open a seracher and find all issue2's documents
            indexSearcher = new IndexSearcher(getCommentsIndexDirectory());
            TopDocs hits = indexSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_ID, ISSUE2_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(4, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment3Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment4Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[2].doc), comment5Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[3].doc), comment6Vals);
            }
            issueDocument = assertIndexContainsIssue(2, ISSUE1_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, issue1Vals);

            // Assert issue1's comments
            hits = indexSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_ID, ISSUE1_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(2, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment1Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment2Vals);
            }
        }
        finally
        {
            if (indexSearcher != null)
            {
                indexSearcher.close();
            }
        }
    }

    public void testReindexThatDoesntGetLockCallsToStringOnIssuesIterable() throws Exception
    {
        final AtomicBoolean methodCalled = new AtomicBoolean(false);
        final AtomicBoolean toStringCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath, reindexMessageManager)
        {
            boolean getIndexLock()
            {
                methodCalled.set(true);
                return false;
            }
        };

        final IssuesIterable mockIssuesIterable = new IssuesIterable()
        {
            public void foreach(final Consumer<Issue> sink)
            {
                throw new UnsupportedOperationException();
            }

            public int size()
            {
                throw new UnsupportedOperationException();
            }

            public boolean isEmpty()
            {
                throw new UnsupportedOperationException();
            }

            public String toString()
            {
                toStringCalled.set(true);
                return "I'm a teapot";
            }
        };

        final long result = indexManager.reIndexIssues(mockIssuesIterable, Contexts.nullContext());

        assertTrue(methodCalled.get());
        assertTrue(toStringCalled.get());
        assertEquals(-1L, result);

        verifyMocks();
    }

    public void testDeactivateIndexesInactive() throws Exception
    {
        // SHould not throw any exceptions
        indexManager.deactivate();

        verifyMocks();
    }

    public void testDeactivateRemoveListenerError() throws Exception
    {
        final String errorMessage = "something went wrong.";

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResultAction(new ActionSupport()
        {
            public Collection<String> getErrorMessages()
            {
                return Lists.newArrayList(errorMessage);
            }

            public boolean getHasErrorMessages()
            {
                return true;
            }
        });
        mad.setResult(Action.ERROR);
        CoreFactory.setActionDispatcher(mad);

        try
        {
            indexManager.deactivate();
            fail("AtlassianCoreException should have been thrown.");
        }
        catch (final RuntimeException e)
        {
            assertTrue(e.getMessage().indexOf(errorMessage) > -1);
        }

        verifyMocks();
    }

    public void testDeactivate() throws Exception
    {
        final AtomicInteger shutDownCalled = new AtomicInteger(0);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new UnimplementedIssueIndexer()
        {
            @Override
            public void shutdown()
            {
                shutDownCalled.incrementAndGet();
            }
        }, indexPath, reindexMessageManager);

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        assertEquals(0, shutDownCalled.get());

        indexManager.deactivate();

        final List actionsCalled = mad.getActionsCalled();
        checkSingleElementCollection(actionsCalled, "com.atlassian.jira.action.admin.ListenerDelete");
        final List parametersCalled = mad.getParametersCalled();
        checkSingleElementCollection(parametersCalled, EasyMap.build("clazz", IssueIndexListener.class.getName()));

        assertEquals(1, shutDownCalled.get());
        verifyMocks();
    }

    public void testActivateIndexesActive() throws Exception
    {
        mockScheduler.expectAndReturn("isShutdown", Boolean.TRUE);
        try
        {
            indexManager.activate(Contexts.nullContext());
            fail("IllegalStateException must have been thrown.");
        }
        catch (final IllegalStateException e)
        {
            assertEquals("Cannot activate indexing as it is already active.", e.getMessage());
        }

        verifyMocks();
    }

    public void testActivateCreateListenerError() throws Exception
    {
        mockScheduler.expectAndReturn("isShutdown", Boolean.TRUE);

        final String errorMessage = "something went wrong.";

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResultAction(new ActionSupport()
        {
            public Collection<String> getErrorMessages()
            {
                return Lists.newArrayList(errorMessage);
            }

            public boolean getHasErrorMessages()
            {
                return true;
            }
        });
        mad.setResult(Action.ERROR);
        CoreFactory.setActionDispatcher(mad);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, new DefaultIssueIndexer(indexDirectoryFactory, new MemoryIssueIndexer.CommentRetrieverImpl(ComponentAccessor.getIssueManager()), new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(ComponentAccessor.getIssueManager())), indexPath, reindexMessageManager);
        try
        {
            indexManager.activate(Contexts.nullContext());
            fail("AtlassianCoreException should have been thrown.");
        }
        catch (final RuntimeException e)
        {
            assertTrue(e.getMessage().indexOf(errorMessage) > -1);
        }

        verifyMocks();
    }

    public void testActivate() throws Exception
    {
        final AtomicReference<Context> reindexAllContext = new AtomicReference<Context>(null);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, new UnimplementedIssueIndexer()
        {
            @Override
            public void setIndexRootPath(final String path)
            {
                assertNotNull(path);
                assertEquals(indexDirectory, path);
            }
        }, indexPath, reindexMessageManager)
        {
            @Override
            public long reIndexAll(final Context context)
            {
                reindexAllContext.compareAndSet(null, context);
                return 10L;
            }
        };
        mockScheduler.expectAndReturn("isShutdown", Boolean.TRUE);

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResultAction(new ListenerCreate());
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        final Context ctx = Contexts.nullContext();
        indexManager.activate(ctx);

        final List actionsCalled = mad.getActionsCalled();
        checkSingleElementCollection(actionsCalled, "com.atlassian.jira.action.admin.ListenerCreate");
        final List parametersCalled = mad.getParametersCalled();
        checkSingleElementCollection(parametersCalled, EasyMap.build("name", "Issue Index Listener", "clazz",
            "com.atlassian.jira.event.listeners.search.IssueIndexListener"));

        assertSame(ctx, reindexAllContext.get());

        verifyMocks();
    }

    public void testActivateDontShutdownScheduler() throws Exception
    {
        prepareMockIndexManager();

        mockScheduler.expectAndReturn("isShutdown", Boolean.TRUE);
        mockScheduler.expectNotCalled("pause");
        mockScheduler.expectNotCalled("start");

        ManagerFactory.addService(Scheduler.class, (Scheduler) mockScheduler.proxy());

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResultAction(new ListenerCreate());
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, new DefaultIssueIndexer(indexDirectoryFactory, new MemoryIssueIndexer.CommentRetrieverImpl(ComponentAccessor.getIssueManager()), new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(ComponentAccessor.getIssueManager())), indexPath, reindexMessageManager);

        indexManager.activate(Contexts.nullContext());

        final List actionsCalled = mad.getActionsCalled();
        checkSingleElementCollection(actionsCalled, "com.atlassian.jira.action.admin.ListenerCreate");
        final List parametersCalled = mad.getParametersCalled();
        checkSingleElementCollection(parametersCalled, EasyMap.build("name", "Issue Index Listener", "clazz",
            "com.atlassian.jira.event.listeners.search.IssueIndexListener"));

        mockScheduler.verify();
        verifyMocks(reindexMessageManager);
        ManagerFactory.removeService(Scheduler.class);
    }

    public void testActivateShutdownScheduler() throws Exception
    {
        prepareMockIndexManager();

        mockScheduler.expectAndReturn("isShutdown", Boolean.FALSE);
        mockScheduler.expectVoid("pause");
        mockScheduler.expectVoid("start");

        ManagerFactory.addService(Scheduler.class, (Scheduler) mockScheduler.proxy());

        final MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResultAction(new ListenerCreate());
        mad.setResult(Action.SUCCESS);
        CoreFactory.setActionDispatcher(mad);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, new DefaultIssueIndexer(indexDirectoryFactory, new MemoryIssueIndexer.CommentRetrieverImpl(ComponentAccessor.getIssueManager()), new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(ComponentAccessor.getIssueManager())), indexPath, reindexMessageManager);
        indexManager.activate(Contexts.nullContext());

        final List actionsCalled = mad.getActionsCalled();
        checkSingleElementCollection(actionsCalled, "com.atlassian.jira.action.admin.ListenerCreate");
        final List parametersCalled = mad.getParametersCalled();
        checkSingleElementCollection(parametersCalled, EasyMap.build("name", "Issue Index Listener", "clazz",
            "com.atlassian.jira.event.listeners.search.IssueIndexListener"));

        mockScheduler.verify();
        verifyMocks(reindexMessageManager);
        ManagerFactory.removeService(Scheduler.class);
    }

    public void testShutdownCallsIndexClose() throws Exception
    {
        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);

        final AtomicInteger closeCalledCount = new AtomicInteger(0);
        final AtomicInteger writerCallCount = new AtomicInteger(0);

        final IssueIndexer mockIssueIndexer = new MemoryIssueIndexer()
        {
            @Override
            public void shutdown()
            {
                closeCalledCount.incrementAndGet();
            }

            @Override
            public Result reindexIssues(final EnclosedIterable<Issue> issues, final Context event)
            {
                writerCallCount.incrementAndGet();
                return new MockResult();
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath, reindexMessageManager);

        assertEquals(0, writerCallCount.get());
        indexManager.shutdown();
        assertEquals(1, closeCalledCount.get());
        assertEquals(0, writerCallCount.get());

        // This should create the LuceneConnections
        indexManager.reIndex(new MockIssue());
        assertEquals(1, writerCallCount.get());
        assertEquals(1, closeCalledCount.get());
        indexManager.shutdown();
        // The close() method should be invoked twice as in this test we return the same LuceneConnection for
        // both issue index and comment index
        assertEquals("Should have closed our connections", 2, closeCalledCount.get());
        assertEquals(1, writerCallCount.get());
    }

    public void testOptimize() throws Exception
    {
        final Mock mockIssueIndexer = new Mock(IssueIndexer.class);
        mockIssueIndexer.setStrict(true);
        final IssueIndexer indexer = (IssueIndexer) mockIssueIndexer.proxy();

        final Index.Result result = new MockResult();
        mockIssueIndexer.expectAndReturn("reindexIssues", P.args(new IsAnything(), P.isA(Context.class)), result);
        mockIssueIndexer.expectAndReturn("reindexIssues", P.args(new IsAnything(), P.isA(Context.class)), result);
        mockIssueIndexer.expectAndReturn("optimize", result);
        mockApplicationProperties.expectAndReturn("getOption", P.args(new IsEqual(APKeys.JIRA_OPTION_INDEXING)), Boolean.TRUE);
        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);

        // does it pass calls to IssueIndexer
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), indexer, indexPath, reindexMessageManager);

        assertEquals(0, indexManager.getReindexesSinceOptimize());

        indexManager.reIndex(new MockIssue(Id.HUNDRED));
        assertEquals(1, indexManager.getReindexesSinceOptimize());

        indexManager.reIndex(new MockIssue(Id.HUNDRED));
        assertEquals(2, indexManager.getReindexesSinceOptimize());

        indexManager.optimize();

        assertEquals(0, indexManager.getReindexesSinceOptimize());

        mockIssueIndexer.verify();
    }

    public void testOptimizeReturnsZeroIfIndexingIsDisabled() throws Exception
    {
        final Mock mockIssueIndexer = new Mock(IssueIndexer.class);
        mockIssueIndexer.setStrict(true);
        final IssueIndexer indexer = (IssueIndexer) mockIssueIndexer.proxy();

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, indexer, indexPath, reindexMessageManager);
        assertEquals(0, indexManager.optimize());

        mockIssueIndexer.verify();
        verifyMocks();
    }

    public void testIsIndexingEnabledTrue()
    {
        final Mock mockIssueIndexer = new Mock(IssueIndexer.class);
        mockIssueIndexer.setStrict(true);
        final IssueIndexer indexer = (IssueIndexer) mockIssueIndexer.proxy();

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), indexer, indexPath, reindexMessageManager);
        assertTrue(indexManager.isIndexingEnabled());

        mockIssueIndexer.verify();
        verifyMocks();
    }

    public void testIsIndexingEnabledFalse()
    {
        final Mock mockIssueIndexer = new Mock(IssueIndexer.class);
        mockIssueIndexer.setStrict(true);
        final IssueIndexer indexer = (IssueIndexer) mockIssueIndexer.proxy();

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndexing();
            }
        }, indexer, indexPath, reindexMessageManager);
        assertFalse(indexManager.isIndexingEnabled());

        mockIssueIndexer.verify();
        verifyMocks();
    }

    public void testDeIndexClearsThreadLocalSearcher() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        new IndexWriter(directory, null, true, IndexWriter.MaxFieldLength.UNLIMITED).close();

        final AtomicInteger deIndexCalled = new AtomicInteger(0);
        final AtomicInteger getIssueSearcherCalled = new AtomicInteger(0);

        final IssueIndexer mockIssueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public IndexSearcher getIssueSearcher()
            {
                getIssueSearcherCalled.incrementAndGet();
                try
                {

                    return new IndexSearcher(directory);
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Index.Result deindexIssues(final EnclosedIterable<Issue> issues, final Context event)
            {
                deIndexCalled.incrementAndGet();
                return new MockResult();
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath, reindexMessageManager);
        final IssueIndexManager oldIndexManager = ManagerFactory.getIndexManager();

        try
        {
            ManagerFactory.addService(IssueIndexManager.class, indexManager);

            assertEquals(0, deIndexCalled.get());
            assertEquals(0, getIssueSearcherCalled.get());

            final IndexSearcher searcher = indexManager.getIssueSearcher();
            assertNotNull(searcher);
            assertEquals(1, getIssueSearcherCalled.get());

            final MockGenericValue mockIssueGV = new MockGenericValue("Issue", EasyMap.build("id", Id.TEN_THOUSAND));
            indexManager.deIndex(mockIssueGV);

            assertEquals(1, deIndexCalled.get());

            final IndexSearcher newSearcher = indexManager.getIssueSearcher();
            assertNotNull(newSearcher);
            assertNotSame(searcher, newSearcher);
            assertEquals(2, getIssueSearcherCalled.get());

            verifyMocks();
        }
        finally
        {
            // Clear the searcher so that other tests are not affected
            DefaultIndexManager.flushThreadLocalSearchers();

            // Put the old manager back - so not to affect other tests.
            ManagerFactory.addService(IssueIndexManager.class, oldIndexManager);
        }
    }

    public void testDeIndexChucksRuntimeExceptionNotIndexExFromIndexerDeleteAndReinit() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer();

        mockApplicationProperties.expectAndReturn("getString", P.args(new IsEqual(APKeys.JIRA_PATH_INDEX)), indexDirectory);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, reindexMessageManager);

        try
        {
            indexManager.deIndex(new MockGenericValue("Issue", Collections.EMPTY_MAP));
            fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    public void testDeIndexWithRuntimeExceptionClearsThreadLocalSearcher() throws Exception
    {
        _testDeIndexWithThrowableClearsThreadLocalSearcher(new RuntimeException());
    }

    public void testDeIndexWithErrorClearsThreadLocalSearcher() throws Exception
    {
        _testDeIndexWithThrowableClearsThreadLocalSearcher(new ClassFormatError());
    }

    private void _testDeIndexWithThrowableClearsThreadLocalSearcher(final Throwable throwable) throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        new IndexWriter(directory, null, true).close();

        final AtomicInteger deIndexCalled = new AtomicInteger(0);
        final AtomicInteger getIssueSearcherCalled = new AtomicInteger(0);

        final IssueIndexer mockIssueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public IndexSearcher getIssueSearcher()
            {
                getIssueSearcherCalled.incrementAndGet();
                try
                {
                    return new IndexSearcher(directory);
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Index.Result deindexIssues(final EnclosedIterable<Issue> issues, final Context event)
            {
                deIndexCalled.incrementAndGet();
                if (throwable instanceof Error)
                {
                    throw (Error) throwable;
                }
                else if (throwable instanceof RuntimeException)
                {
                    throw (RuntimeException) throwable;
                }
                else
                {
                    fail("Cannot throw a checked Exception");
                }
                return new MockResult();
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath, reindexMessageManager);

        final IssueIndexManager oldIndexManager = ManagerFactory.getIndexManager();

        try
        {
            ManagerFactory.addService(IssueIndexManager.class, indexManager);

            assertEquals(0, deIndexCalled.get());
            assertEquals(0, getIssueSearcherCalled.get());

            final IndexSearcher searcher = indexManager.getIssueSearcher();
            assertNotNull(searcher);
            assertEquals(1, getIssueSearcherCalled.get());

            final MockGenericValue mockIssueGV = new MockGenericValue("Issue", EasyMap.build("id", Id.TEN_THOUSAND));
            try
            {
                indexManager.deIndex(mockIssueGV);
            }
            catch (final Throwable yay)
            {
                assertSame(throwable, yay);
            }

            assertEquals(1, deIndexCalled.get());

            final IndexSearcher newSearcher = indexManager.getIssueSearcher();
            assertNotNull(newSearcher);
            assertNotSame(searcher, newSearcher);
            assertEquals(2, getIssueSearcherCalled.get());

            verifyMocks();
        }
        finally
        {
            // Clear the searcher so that other tests are not affected
            DefaultIndexManager.flushThreadLocalSearchers();

            // Put the old manager back - so not to affect other tests.
            ManagerFactory.addService(IssueIndexManager.class, oldIndexManager);
        }
    }

    private void assertIssueDocumentEquals(final Document document, final Map<String, ?> expectedFields)
    {
        assertDocumentEquals(document, expectedFields, DBToIssueDocumentConstants);
    }

    private void assertCommentDocumentEquals(final Document document, final Map<String, ?> expectedFields)
    {
        assertDocumentEquals(document, expectedFields, DBToCommentDocumentConstants);
    }

    private void assertDocumentEquals(final Document document, final Map<String, ?> expectedFields, final Map<String, String> DBToDocumentConstants)
    {
        assertNotNull(document);
        assertNotNull(expectedFields);
        for (final Map.Entry<String, ?> entry : expectedFields.entrySet())
        {
            String documentFieldKey = DBToDocumentConstants.get(entry.getKey());
            // Fall back to the actual key
            if (documentFieldKey == null)
            {
                documentFieldKey = entry.getKey();
            }
            if (!UNINDEXED.equals(documentFieldKey))
            {
                assertEquals("Did not get expected value for: " + documentFieldKey + " in doc:" + document, entry.getValue().toString(),
                    document.get(documentFieldKey));
            }
        }
    }

    private Document assertIndexContainsIssue(final int numDocs, final String issueId) throws IOException
    {
        IndexReader indexReader = null;
        try
        {
            SearcherCache.getThreadLocalCache().closeSearchers();
            indexManager.getIssueSearcher();
            indexReader = IndexReader.open(getIssueIndexDirectory());

            // Ensure the correct things are in the index before we proceed with the test
            if (indexReader.numDocs() != numDocs)
            {
                for (int i = 0; i < indexReader.numDocs(); i++)
                {
                    System.out.println(indexReader.document(i));
                }
            }
            assertEquals(numDocs, indexReader.numDocs());

            if (issueId != null)
            {
                for (int i = 0; i < indexReader.maxDoc(); ++i)
                {
                    if (!indexReader.isDeleted(i))
                    {
                        final Document doc = indexReader.document(i);
                        if (issueId.equals(doc.get(DocumentConstants.ISSUE_ID)))
                        {
                            return doc;
                        }
                    }
                }
                fail("could not find document with Issue ID: " + issueId);
            }

            throw new IllegalStateException("I so should not be here!!!! Issue id is null!");
        }
        finally
        {
            if (indexReader != null)
            {
                indexReader.close();
            }
        }
    }

    private Collection<Document> assertIndexContainsComments(final Collection<GenericValue> commentGVs, final int totalCommentCountInIndex) throws IOException
    {
        IndexSearcher indexSearcher = null;
        try
        {
            final List<Document> result = new ArrayList<Document>(commentGVs.size());
            indexManager.getCommentSearcher();
            indexSearcher = new IndexSearcher(getCommentsIndexDirectory());
            assertEquals(totalCommentCountInIndex, indexSearcher.getIndexReader().numDocs());

            for (final GenericValue commentGV : commentGVs)
            {
                final TopDocs hits = indexSearcher.search(new TermQuery(new Term(DocumentConstants.COMMENT_ID, commentGV.getLong("id").toString())), Integer.MAX_VALUE);
                assertNotNull(hits);
                assertEquals(1, hits.totalHits);
                result.add(indexSearcher.doc(hits.scoreDocs[0].doc));
            }

            return result;
        }
        finally
        {
            if (indexSearcher != null)
            {
                indexSearcher.close();
            }
        }
    }

    private Directory getIssueIndexDirectory() throws IOException
    {
        return issueDirectory;
    }

    private Directory getCommentsIndexDirectory() throws IOException
    {
        return commentDirectory;
    }

    private Directory getChangeIndexDirectory() throws IOException
    {
        return changesDirectory;
    }

    private void verifyMocks(Object... easyMocks)
    {
        mockApplicationProperties.verify();

        if (easyMocks != null && easyMocks.length > 0)
        {
            EasyMock.verify(easyMocks);
        }
    }

    class Documents
    {
        final Document issue;
        final Collection<Document> comments;
        final Collection<Document> changes;

        Documents(final Document issue, final Collection<Document> comments, final Collection<Document> changes)
        {
            this.issue = issue;
            this.comments = Collections.unmodifiableCollection(comments);
            this.changes = Collections.unmodifiableCollection(changes);
        }

        Documents(final Document issue, final Collection<Document> comments)
        {
            this.issue = issue;
            this.comments = Collections.unmodifiableCollection(comments);
            this.changes = null;
        }

    }
}
