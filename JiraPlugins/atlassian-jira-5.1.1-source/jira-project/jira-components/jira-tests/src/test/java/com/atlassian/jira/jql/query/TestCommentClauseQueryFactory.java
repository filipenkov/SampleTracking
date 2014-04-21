package com.atlassian.jira.jql.query;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.parameters.lucene.CachedWrappedFilterCache;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCommentClauseQueryFactory extends MockControllerTestCase
{

    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";
    private static final String GROUP_3 = "group3";
    private static final ProjectRoleImpl PROJECT_ROLE_1 = new ProjectRoleImpl(11L, "one", "o");
    private static final ProjectRoleImpl PROJECT_ROLE_2 = new ProjectRoleImpl(22L, "two", "tt");
    private static final ProjectRoleImpl PROJECT_ROLE_3 = new ProjectRoleImpl(33L, "three", "ttt");

    private CommentClauseQueryFactory commentClauseQueryFactory;
    private PermissionsFilterGenerator permissionsFilterGenerator;
    private CachedWrappedFilterCache cachedWrappedFilterCache;

    private ApplicationProperties applicationProperties = null;
    private QueryCreationContext queryCreationContext;
    private User theUser = null;

    // don't really care about testing the modification - just use a dummy implementation
    private LuceneQueryModifier luceneQueryModifier = new LuceneQueryModifier()
    {
        public Query getModifiedQuery(final Query originalQuery)
        {
            return originalQuery;
        }
    };

    @Before
    public void setUp() throws Exception
    {
       UtilsForTestSetup.loadDatabaseDriver();

        cachedWrappedFilterCache = mockController.getMock(CachedWrappedFilterCache.class);
        permissionsFilterGenerator = mockController.getMock(PermissionsFilterGenerator.class);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, permissionsFilterGenerator)
        {
            @Override
            CachedWrappedFilterCache getCachedWrappedFilterCache()
            {
                return cachedWrappedFilterCache;
            }
        };

        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @After
    public void tearDown() throws Exception
    {
        // swap with original applicationProperties
        if (applicationProperties != null)
        {
            ManagerFactory.addService(ApplicationProperties.class, applicationProperties);
        }
        applicationProperties = null;
    }

    @Test
    public void testValidateClauseOperators() throws Exception
    {
        replay();

        _testOperatorValidate(Operator.GREATER_THAN);
        _testOperatorValidate(Operator.GREATER_THAN_EQUALS);
        _testOperatorValidate(Operator.LESS_THAN);
        _testOperatorValidate(Operator.LESS_THAN_EQUALS);
        _testOperatorValidate(Operator.EQUALS);
        _testOperatorValidate(Operator.NOT_EQUALS);
        _testOperatorValidate(Operator.IN);
    }

    private void _testOperatorValidate(Operator operator)
    {
        final boolean result = commentClauseQueryFactory.isClauseValid(new TerminalClauseImpl(IssueFieldConstants.COMMENT, operator, "test"));
        assertFalse(result);
    }

    @Test
    public void testValidateClauseHappyPath() throws Exception
    {
        replay();

        assertTrue(commentClauseQueryFactory.isClauseValid(new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test")));
    }

    @Test
    public void testCreateProjectVisibilityQueryEmptyProjectIds() throws Exception
    {
        replay();

        assertNull(commentClauseQueryFactory.createProjectVisibilityQuery(Collections.<Long>emptyList()));
    }

    @Test
    public void testCreateProjectVisibilityQueryHappyPath() throws Exception
    {
        replay();

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "23")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "33")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.PROJECT_ID, "34")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, commentClauseQueryFactory.createProjectVisibilityQuery(asList(23L, 33L, 34L)));
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsGroupRestrictions() throws Exception
    {
        List<Long> projectIds = asList(1L, 2L, 3L);

        final User mockUser = new MockUser("testDude");

        final ProjectRoleManager mockProjectRoleManager = mockController.getMock(ProjectRoleManager.class);
        EasyMock.expect(mockProjectRoleManager.createProjectIdToProjectRolesMap(mockUser, projectIds))
                .andReturn(new ProjectRoleManager.ProjectIdToProjectRoleIdsMap());

        commentClauseQueryFactory = new CommentClauseQueryFactory(null, mockProjectRoleManager, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null)
        {
            @Override
            Set<String> getGroups(final User searcher)
            {
                return asSet("group1", "group2", "group3");
            }
        };

        replay();

        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, mockUser);

        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, BooleanClause.Occur.SHOULD);
        BooleanQuery groupQuery = new BooleanQuery();
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group3")), BooleanClause.Occur.SHOULD);
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group1")), BooleanClause.Occur.SHOULD);
        groupQuery.add(new TermQuery(new Term(DocumentConstants.COMMENT_LEVEL, "group2")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(groupQuery, BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, levelQuery);
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsRoleRestrictions() throws Exception
    {
        List<Long> projectIds = asList(1L, 2L, 3L);

        final User mockUser = new MockUser("testDude");

        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap idsMap = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        idsMap.add(1L, 123L);
        idsMap.add(2L, 345L);
        final ProjectRoleManager mockProjectRoleManager = mockController.getMock(ProjectRoleManager.class);
        EasyMock.expect(mockProjectRoleManager.createProjectIdToProjectRolesMap(mockUser, projectIds))
                .andReturn(idsMap);

        final AtomicBoolean createRoleQueryCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, mockProjectRoleManager, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null)
        {
            @Override
            Set<String> getGroups(final User searcher)
            {
                return Collections.emptySet();
            }

            @Override
            Query createProjectRoleLevelQuery(final ProjectRoleManager.ProjectIdToProjectRoleIdsMap projectIdToProjectRolesMap)
            {
                assertEquals(idsMap, projectIdToProjectRolesMap);
                createRoleQueryCalled.set(true);
                return new BooleanQuery();
            }
        };

        replay();

        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, mockUser);

        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, BooleanClause.Occur.SHOULD);
        BooleanQuery roleQuery = new BooleanQuery();
        expectedQuery.add(roleQuery, BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, levelQuery);

        assertTrue(createRoleQueryCalled.get());
    }

    @Test
    public void testCreateLevelRestrictionQueryForCommentsNullUser() throws Exception
    {
        List<Long> projectIds = asList(1L, 2L, 3L);

        final User mockUser = null;

        final ProjectRoleManager mockProjectRoleManager = mockController.getMock(ProjectRoleManager.class);
        replay();

        commentClauseQueryFactory = new CommentClauseQueryFactory(null, mockProjectRoleManager, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null);        final BooleanQuery levelQuery = commentClauseQueryFactory.createLevelRestrictionQueryForComments(projectIds, mockUser);

        BooleanQuery expectedQuery = new BooleanQuery();
        Query noGroupOrProjectQuery = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        expectedQuery.add(noGroupOrProjectQuery, BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, levelQuery);
    }

    @Test
    public void testGetVisibleProjectIdsHappyPath() throws Exception
    {
        List<Project> projects = TestCommentClauseQueryFactory.<Project>asList(
                new MockProject(12),
                new MockProject(14));

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(mockPermissionManager.getProjectObjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) null))
                .andReturn(projects);

        replay();

        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null);

        assertEquals(EasyList.build(12L, 14L), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetVisibleProjectIdsNullProject() throws Exception
    {
        List<Project> projects = TestCommentClauseQueryFactory.<Project>asList(new MockProject(12), null);

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(mockPermissionManager.getProjectObjects(Permissions.BROWSE, null))
                .andReturn(projects);

        replay();

        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null);

        assertEquals(EasyList.build(12L), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetVisibleProjectIdsNullProjects() throws Exception
    {
        List<Project> projects = null;

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(mockPermissionManager.getProjectObjects(Permissions.BROWSE, null))
                .andReturn(projects);

        replay();

        commentClauseQueryFactory = new CommentClauseQueryFactory(mockPermissionManager, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null);

        assertEquals(EasyList.build(), commentClauseQueryFactory.getVisibleProjectIds(null));
    }

    @Test
    public void testGetQueryHappyPath() throws Exception
    {
        final AtomicBoolean visProjCalled = new AtomicBoolean(false);
        final AtomicBoolean createLevelCalled = new AtomicBoolean(false);
        final AtomicBoolean delegateCalled = new AtomicBoolean(false);
        final AtomicBoolean genCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null)
        {
            @Override
            List<Long> getVisibleProjectIds(final User searcher)
            {
                visProjCalled.set(true);
                return asList(1L, 2L);
            }

            @Override
            BooleanQuery createLevelRestrictionQueryForComments(final List<Long> projectIds, final User searcher)
            {
                createLevelCalled.set(true);
                return new BooleanQuery();
            }

            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandRegistry)
            {
                return new ClauseQueryFactory()
                {

                    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
                    {
                        delegateCalled.set(true);
                        return QueryFactoryResult.createFalseResult();
                    }

                };
            }

            @Override
            QueryFactoryResult generateIssueIdQueryFromCommentQuery(final Query commentIndexQuery, final QueryCreationContext creationContext)
            {
                genCalled.set(true);
                return QueryFactoryResult.createFalseResult();
            }
        };

        replay();

        commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));

        assertTrue(visProjCalled.get());
        assertTrue(createLevelCalled.get());
        assertTrue(delegateCalled.get());
        assertTrue(genCalled.get());
    }

    @Test
    public void testGetQueryOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final AtomicBoolean delegateCalled = new AtomicBoolean(false);
        final AtomicBoolean genCalled = new AtomicBoolean(false);
        commentClauseQueryFactory = new CommentClauseQueryFactory(null, null, MockJqlOperandResolver.createSimpleSupport(), null, luceneQueryModifier, null)
        {
            @Override
            ClauseQueryFactory getDelegate(final JqlOperandResolver operandRegistry)
            {
                return new ClauseQueryFactory()
                {
                    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
                    {
                        delegateCalled.set(true);
                        final TermQuery query = new TermQuery(new Term("comment", "test"));
                        return new QueryFactoryResult(query);
                    }
                };
            }

            @Override
            QueryFactoryResult generateIssueIdQueryFromCommentQuery(final Query commentIndexQuery, final QueryCreationContext creationContext)
            {
                assertTrue(commentIndexQuery.toString().contains("comment:test"));
                genCalled.set(true);
                return QueryFactoryResult.createFalseResult();
            }
        };

        replay();

        commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));

        assertTrue(delegateCalled.get());
        assertTrue(genCalled.get());
    }

    @Test
    public void testGetQueryNoVisibleProjects() throws Exception
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) theUser))
                .andReturn(Collections.<Project>emptyList());

        commentClauseQueryFactory = mockController.instantiate(CommentClauseQueryFactory.class);

        final QueryFactoryResult result = commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, "test"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testGetQueryInvalidClause() throws Exception
    {
        commentClauseQueryFactory = mockController.instantiate(CommentClauseQueryFactory.class);

        final QueryFactoryResult result = commentClauseQueryFactory.getQuery(queryCreationContext, new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.GREATER_THAN, "test"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    /**
     * Test that the query returned has level and role_level set to -1
     */
    @Test
    public void testCreateNoGroupOrProjectRoleLevelQuery()
    {
        replay();

        final Query query = commentClauseQueryFactory.createNoGroupOrProjectRoleLevelQuery();
        assertEquals("+level:-1 +role_level:-1", query.toString());
    }

    @Test
    public void testCreateGroupLevelQuery() throws Exception
    {
        replay();

        Query result = commentClauseQueryFactory.createGroupLevelQuery(null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createGroupLevelQuery(new HashSet<String>());
        assertEquals(new BooleanQuery(), result);

        Set<String> groups = asSet(GROUP_1);
        Query query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertEquals("level:" + GROUP_1, query.toString());

        groups = asSet(GROUP_1, GROUP_2);
        query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertEquals("level:" + GROUP_1 + " level:" + GROUP_2, query.toString());

        groups = asSet(GROUP_1, GROUP_2, GROUP_3);
        query = commentClauseQueryFactory.createGroupLevelQuery(groups);
        assertEquals("level:" + GROUP_3 + " level:" + GROUP_1 + " level:" + GROUP_2, query.toString());
    }

    @Test
    public void testCreateProjectRoleLevelQuery() throws Exception
    {
        replay();

        Query result = commentClauseQueryFactory.createProjectRoleLevelQuery(null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createProjectRoleLevelQuery(new ProjectRoleManager.ProjectIdToProjectRoleIdsMap());
        assertEquals(new BooleanQuery(), result);

        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap map = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        map.add(1L, null);
        map.add(2L, PROJECT_ROLE_1.getId());
        map.add(2L, PROJECT_ROLE_2.getId());
        map.add(3L, PROJECT_ROLE_1.getId());
        map.add(3L, PROJECT_ROLE_2.getId());
        map.add(3L, PROJECT_ROLE_3.getId());

        final Query query = commentClauseQueryFactory.createProjectRoleLevelQuery(map);
        final String queryString = query.toString();
        assertTrue(queryString.indexOf("(+projid:1 +role_level:" + PROJECT_ROLE_1.getId() + ")") == -1);
        assertTrue(queryString.indexOf("(+projid:1 +role_level:" + PROJECT_ROLE_2.getId() + ")") == -1);
        assertTrue(queryString.indexOf("(+projid:1 +role_level:" + PROJECT_ROLE_3.getId() + ")") == -1);
        assertTrue(queryString.indexOf("(+projid:2 +role_level:" + PROJECT_ROLE_1.getId() + ")") != -1);
        assertTrue(queryString.indexOf("(+projid:2 +role_level:" + PROJECT_ROLE_2.getId() + ")") != -1);
        assertTrue(queryString.indexOf("(+projid:2 +role_level:" + PROJECT_ROLE_3.getId() + ")") == -1);
        assertTrue(queryString.indexOf("(+projid:3 +role_level:" + PROJECT_ROLE_1.getId() + ")") != -1);
        assertTrue(queryString.indexOf("(+projid:3 +role_level:" + PROJECT_ROLE_2.getId() + ")") != -1);
        assertTrue(queryString.indexOf("(+projid:3 +role_level:" + PROJECT_ROLE_3.getId() + ")") != -1);
    }

    @Test
    public void testCreateCommentInProjectAndUserInRoleQuery()
    {
        replay();

        Query result = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(0L, null);
        assertEquals(new BooleanQuery(), result);

        result = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(null, 0L);
        assertEquals(new BooleanQuery(), result);

        final Long projectId = 123L;
        final Long projectRoleId = 567L;
        final Query query = commentClauseQueryFactory.createCommentInProjectAndUserInRoleQuery(projectId, projectRoleId);
        assertEquals("+projid:" + projectId + " +role_level:" + projectRoleId, query.toString());
    }

    @Test
    public void testGetPermissionsFilterOverrideSecurity() throws Exception
    {
        replay();
        assertNull(commentClauseQueryFactory.getPermissionsFilter(true, null));
    }

    @Test
    public void testGetPermissionsFilterNotFromCache() throws Exception
    {
        cachedWrappedFilterCache.getFilter(null);
        mockController.setReturnValue(null);
        cachedWrappedFilterCache.storeFilter(EasyMock.<Filter>anyObject(), EasyMock.<User>anyObject());
        permissionsFilterGenerator.getQuery(null);
        mockController.setReturnValue(new BooleanQuery());
        replay();
        assertNotNull(commentClauseQueryFactory.getPermissionsFilter(false, null));
        verify();
    }

    @Test
    public void testGetPermissionsFilterFromCache() throws Exception
    {
        cachedWrappedFilterCache.getFilter(null);
        mockController.setReturnValue(new QueryWrapperFilter(new BooleanQuery()));
        replay();
        assertNotNull(commentClauseQueryFactory.getPermissionsFilter(false, null));
        verify();
    }

    private static <T> List<T> asList(T... elements)
    {
        return CollectionBuilder.newBuilder(elements).asMutableList();
    }

    private static <T> Set<T> asSet(T... elements)
    {
        return CollectionBuilder.newBuilder(elements).asMutableSet();
    }
}
