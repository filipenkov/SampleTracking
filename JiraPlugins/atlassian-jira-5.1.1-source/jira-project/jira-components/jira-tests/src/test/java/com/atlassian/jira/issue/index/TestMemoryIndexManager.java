/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.searchers.MockSearcherFactory;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.io.IOException;

public class TestMemoryIndexManager extends AbstractUsersTestCase
{
    private User bob;
    private GenericValue project;
    private IssueIndexManager oldManager;
    private FieldVisibilityBean origFieldVisibilityBean;

    private Directory issueDir;

    public TestMemoryIndexManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);

        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        oldManager = ManagerFactory.getIndexManager();

        final Function<IndexDirectoryFactory.Name, Directory> directoryFactory = new Function<IndexDirectoryFactory.Name, Directory>()
        {
            public Directory get(final IndexDirectoryFactory.Name type)
            {
                final Directory directory = MockSearcherFactory.getCleanRAMDirectory();
                if (type == IndexDirectoryFactory.Name.ISSUE)
                {
                    issueDir = directory;
                }
                return directory;
            }
        };

        // set the indexManager to the memory one
        ManagerFactory.addService(IssueIndexManager.class, new MemoryIndexManager(directoryFactory, ComponentAccessor.getIssueManager()));

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", 10L));
        bob = createMockUser("bob");

        // add global perms to browse
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project, Permissions.BROWSE);

        // set index path to whatever, so that indexing will work.
        ComponentAccessor.getComponent(PropertiesManager.class).getPropertySet().setString(APKeys.JIRA_PATH_INDEX, "memory");
    }

    @Override
    protected void tearDown() throws Exception
    {
        bob = null;
        project = null;
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
        ManagerFactory.addService(IssueIndexManager.class, oldManager);
        super.tearDown();
    }

    /**
     * Make sure that the index manager set up is using the memory one
     * And the directories are using RAMDirectories
     */
    public void testMemoryManagerInUse()
    {
        assertTrue(ManagerFactory.getIndexManager() instanceof MemoryIndexManager);
    }

    public void testLuceneMemoryIndexing() throws IOException, SearchException, ParseException
    {
        // setup the issue.
        final MockIssue issue = new MockIssue();
        issue.setId(1L);
        issue.setProject(new MockGenericValue("Project", EasyMap.build("id", 10L)));

        // setup RAMDirectory
        final Directory ramDir = new RAMDirectory();

        // setup writer and add doc to index
        final IndexWriter writer = new IndexWriter(ramDir, JiraAnalyzer.ANALYZER_FOR_INDEXING, true, IndexWriter.MaxFieldLength.UNLIMITED);
        final Document doc = IssueDocument.getDocument(issue);
        writer.addDocument(doc);

        // check that documents has increased by 1
        assertEquals(1, writer.maxDoc());
        writer.close();

        // setup reader and make sure there is 1 issue in index
        final IndexReader reader = IndexReader.open(ramDir);
        assertEquals(1, reader.numDocs());
        reader.close();

        // setup a search to make sure the issue is indexed
        TopDocs hits = search(ramDir);

        assertEquals(1, hits.totalHits);
    }

    /**
     * Test memory indexing using the memoryIndexManager
     */
    public void testMemoryIndexManageReindexDeindex()
            throws ParseException, IOException, IndexException, SearchException
    {
        // setup this issue, and index it
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", new Long(10)));
        ManagerFactory.getIndexManager().reIndex(issue);

        // run a search on it and make sure it returns one hit
        ManagerFactory.getIndexManager().getIssueSearcher();

        TopDocs hits = search(issueDir);
        assertEquals(1, hits.totalHits);

        // try to count the number of issues via IndexReader
        final IndexReader reader = IndexReader.open(issueDir);
        assertEquals(1, reader.numDocs());
        reader.close();

        // deindex the issue and make sure we get 0 hits
        ManagerFactory.getIndexManager().deIndex(issue);
        ManagerFactory.getIndexManager().getIssueSearcher();
        hits = search(issueDir);
        assertEquals(0, hits.totalHits);

        // reindex and try again
        ManagerFactory.getIndexManager().reIndex(issue);
        ManagerFactory.getIndexManager().getIssueSearcher();
        hits = search(issueDir);
        assertEquals(1, hits.totalHits);
    }

    /**
     * Test indexing and reindexing all issues and using IssueManager to search
     */
    public void testReindexAllAndSearch() throws IndexException, ParseException, IOException, SearchException
    {
        // setup multiple issues and index
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", new Long(10)));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(2), "project", new Long(10)));
        ManagerFactory.getIndexManager().reIndexAll();

        // make sure 2 issues are indexed by searching
        final TopDocs hits = search(issueDir);
        assertEquals(2, hits.totalHits);

        // make sure a IndexReader can also find 2 issues
        final IndexReader reader = IndexReader.open(issueDir);
        assertEquals(2, reader.numDocs());
        reader.close();
    }

    /**
     * Sets up a search parameter (project parameter) given a directory
     * and returns the hits on that query
     *
     *
     * @param dir
     * @throws ParseException
     * @throws IOException
     */
    private TopDocs search(final Directory dir) throws ParseException, IOException, SearchException
    {
        final IndexSearcher searcher = new IndexSearcher(dir);

        LuceneQueryBuilder luceneQueryBuilder = ComponentManager.getComponentInstanceOfType(LuceneQueryBuilder.class);
        Query query = luceneQueryBuilder.createLuceneQuery(new QueryCreationContextImpl(bob), new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, 10L));

        return searcher.search(query, Integer.MAX_VALUE);
    }
}
