/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search.providers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProviderFactoryImpl;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGeneratorImpl;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.issue.statistics.util.PrefixFieldableHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.MockFieldClausePermissionFactory;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class TestLuceneSearchProvider extends AbstractUsersIndexingTestCase
{
    private static enum QueryType
    {
        ISSUE, COMMENT
    }

    private static final String SUMMARY_BODY = "SummaryBody";
    private static final String ANOTHER_SUMMARY_BODY = "AnotherSummaryBody";
    private static final String COMMENT_BODY = "CommentBody";
    private static final String ANOTHER_COMMENT_BODY = "AnotherCommentBody";

    private FieldClausePermissionChecker.Factory originalFactory;
    private final String resolutionId = "1";
    private User bob;
    private User carl;

    public TestLuceneSearchProvider(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        final FieldVisibilityManager visibilityBean = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);

        originalFactory = ComponentManager.getComponentInstanceOfType(FieldClausePermissionChecker.Factory.class);
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, new MockFieldClausePermissionFactory());
        bob = createMockUser("bob");

        final Group group = addUserToNewGroup(bob, "group");
        carl = createMockUser("carl");

        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "TST"));

        final PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final GenericValue defaultScheme = permissionSchemeManager.createDefaultScheme();
        final SchemeEntity schemeEntity = new SchemeEntity(GroupDropdown.DESC, null, Permissions.BROWSE);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);

        permissionSchemeManager.addSchemeToProject(project, defaultScheme);

        UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", resolutionId));
        // Create two Issues with same comment but different level
        final GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project.getLong("id"), "key", "TST-1",
            "resolution", resolutionId, "summary", SUMMARY_BODY));
        UtilsForTests.getTestEntity("Action",
            EasyMap.build("issue", issue1.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "body", COMMENT_BODY));

        final GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project.getLong("id"), "key", "TST-2",
            "resolution", resolutionId, "summary", SUMMARY_BODY));
        UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issue2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "body",
            COMMENT_BODY, "level", group.getName()));

        final GenericValue issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project.getLong("id"), "key", "TST-3",
            "resolution", resolutionId, "summary", ANOTHER_SUMMARY_BODY));
        UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issue3.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "body",
            ANOTHER_COMMENT_BODY, "level", group.getName()));

        ManagerFactory.getIndexManager().reIndexAll();
    }

    private Group addUserToNewGroup(User user, String groupName)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {
        Group group = createMockGroup(groupName);
        addUserToGroup(user, group);
        return group;
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, originalFactory);

        // remove our mock from the Manager Factory and put a normal one back in place
        ManagerFactory.addService(FieldVisibilityManager.class, new FieldVisibilityBean());
    }

    private LuceneSearchProvider getSearchProvider()
    {
        return new LuceneSearchProvider(ComponentAccessor.getIssueFactory(), new SearchProviderFactoryImpl(),
                ComponentManager.getComponentInstanceOfType(PermissionsFilterGeneratorImpl.class), ComponentManager.getComponentInstanceOfType(SearchHandlerManager.class),
                ComponentManager.getComponentInstanceOfType(SearchSortUtil.class), ComponentManager.getComponentInstanceOfType(LuceneQueryBuilder.class));
    }

    public void testNullQuery() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final SearchResults issues = luceneSearchProvider.search(null, bob, new PagerFilter(-1));
        assertNotNull(issues);
        assertEquals(0, issues.getTotal());
    }

    public void testStandardQuery() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("resolution", Operator.EQUALS, new SingleValueOperand(resolutionId)));

        final SearchResults issues = luceneSearchProvider.search(query, bob, new PagerFilter(-1));
        assertNotNull(issues);
        assertEquals(3, issues.getTotal());
    }
    
    public void testStandardHitCollectorSearch() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("resolution", Operator.EQUALS, new SingleValueOperand(resolutionId)));

        final Set<String> results = new LinkedHashSet<String>();
        //create a PrefixFieldableHitCollector that searches for summaries starting with "Summ"
        luceneSearchProvider.search(query, bob, new PrefixFieldableHitCollector(
                ComponentManager.getComponentInstanceOfType(IssueIndexManager.class).getIssueSearcher(), "summary", "Summ", results));
        assertEquals(1, results.size());
        assertEquals(SUMMARY_BODY, results.iterator().next());
    }

    public void testStandardHitCollectorSearchWithAndQuery() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("resolution", Operator.EQUALS, new SingleValueOperand(resolutionId)));

        org.apache.lucene.search.Query andQuery = new PrefixQuery(new Term("summary", "NOTHING"));
        final Set<String> results = new LinkedHashSet<String>();
        //create a PrefixFieldableHitCollector that searches for summaries starting with "Summ"
        luceneSearchProvider.search(query, bob, new PrefixFieldableHitCollector(
                ComponentManager.getComponentInstanceOfType(IssueIndexManager.class).getIssueSearcher(), "summary", "Summ", results), andQuery);
        assertEquals(0, results.size());
    }

    public void testStandardQueryWithAndQuery() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("resolution", Operator.EQUALS, new SingleValueOperand(resolutionId)));

        final TermQuery andQuery = new TermQuery(new Term("non", "sense"));
        final SearchResults issues = luceneSearchProvider.search(query, bob, new PagerFilter(-1), andQuery);
        assertNotNull(issues);
        assertEquals(0, issues.getTotal());
    }

    public void testEverythingQuery() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl();

        final SearchResults issues = luceneSearchProvider.search(query, bob, new PagerFilter(-1));
        assertNotNull(issues);
        assertEquals(3, issues.getTotal());
    }

    public void testStandardQueryWithSummarySearch() throws SearchException
    {
        _runSearch(SUMMARY_BODY, QueryType.ISSUE, bob, 2, resolutionId);
    }

    public void testStandardQueryWithInvalidSummarySearch() throws SearchException
    {
        _runSearch("Othersummary", QueryType.ISSUE, bob, 0, resolutionId);
    }

    public void testComplexQueryWithComments() throws SearchException
    {
        _runSearch(COMMENT_BODY, QueryType.COMMENT, bob, 2, resolutionId);
    }

    public void testComplexQueryWithIncorrectComments() throws SearchException
    {
        _runSearch("Othercomment", QueryType.COMMENT, bob, 0, resolutionId);
    }

    public void testInvalidComplexQueryWithComments() throws SearchException
    {
        _runSearch(COMMENT_BODY, QueryType.COMMENT, bob, 0, "2");
    }

    public void testComplexQueryWithSecurityComment() throws SearchException
    {
        _runSearch(COMMENT_BODY, QueryType.COMMENT, carl, 1, resolutionId);
    }

    public void testComplexQueryWithSecurityIncorrectComment() throws SearchException
    {
        _runSearch("Othercomment", QueryType.COMMENT, carl, 0, resolutionId);
    }

    public void testSearchAndSortForPagerConstraints() throws SearchException, IOException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        Query jqlQuery = JqlQueryBuilder.newBuilder().where().resolution(resolutionId).and().summary(SUMMARY_BODY).buildQuery();

        final MyDocumentHitCollector hitCollector = new MyDocumentHitCollector(null);

        // Test that we limit the results with a max on the filter
        luceneSearchProvider.searchAndSort(jqlQuery, bob, hitCollector, new PagerFilter(1));
        assertEquals(1, hitCollector.count);
        hitCollector.clearCount();

        // Test that we bring them all back with a max of 2
        luceneSearchProvider.searchAndSort(jqlQuery, bob, hitCollector, new PagerFilter(2));
        assertEquals(2, hitCollector.count);
        hitCollector.clearCount();

        // Test the start and end
        final PagerFilter filter = new PagerFilter(2);
        filter.setStart(1);
        luceneSearchProvider.searchAndSort(jqlQuery, bob, hitCollector, filter);
        assertEquals(1, hitCollector.count);
        hitCollector.clearCount();

        // Bring them all back
        luceneSearchProvider.searchAndSort(jqlQuery, bob, hitCollector, PagerFilter.getUnlimitedFilter());
        assertEquals(2, hitCollector.count);
    }

    private class MyDocumentHitCollector extends DocumentHitCollector
    {
        public int count = 0;

        protected MyDocumentHitCollector(final IndexSearcher searcher)
        {
            super(searcher);
        }

        @Override
        public void collect(final Document d)
        {
            count++;
        }

        @Override
        public void collect(int i)
        {
            collect(null);
        }

        public void clearCount()
        {
            count = 0;
        }
    }

    private void _runSearch(final String query, final QueryType type, final User searcher, final int numberOfIssues, final String resolutionId) throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final JqlClauseBuilder jqlQuery = JqlQueryBuilder.newBuilder().where().defaultAnd().resolution(resolutionId);

        switch (type)
        {
            case COMMENT:
                jqlQuery.comment(query);
                break;
            case ISSUE:
                jqlQuery.summary(query);
                break;
        }
        final SearchResults issues = luceneSearchProvider.search(jqlQuery.buildQuery(), searcher, new PagerFilter(-1));
        assertNotNull(issues);
        assertEquals(query, numberOfIssues, issues.getTotal());
    }

    public void testComplexQueryWithSummaryAndComments() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        JqlClauseBuilder jqlQuery = JqlQueryBuilder.newBuilder().where().resolution(resolutionId).and().sub().comment(SUMMARY_BODY).or().summary(SUMMARY_BODY).endsub();

        final SearchResults issues = luceneSearchProvider.search(jqlQuery.buildQuery(), bob, new PagerFilter(-1));
        assertNotNull(issues);
        assertEquals(2, issues.getTotal());
    }

    public void testHitCounter() throws SearchException
    {
        final LuceneSearchProvider luceneSearchProvider = getSearchProvider();

        // Create a search request
        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("resolution", Operator.EQUALS, new SingleValueOperand(resolutionId)));

        final long issueCount = luceneSearchProvider.searchCount(query, bob);
        assertEquals(3, issueCount);

    }
}
