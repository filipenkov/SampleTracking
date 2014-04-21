package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestPermissionQueryFactory extends MockControllerTestCase
{
    private IssueSecurityLevelManager issueSecurityLevelManager;
    private PermissionManager permissionManager;
    private PermissionSchemeManager permissionSchemeManager;
    private PermissionTypeManager permissionTypeManager;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    private SecurityTypeManager issueSecurityTypeManager;
    private ProjectFactory projectFactory;
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        issueSecurityLevelManager = mockController.getMock(IssueSecurityLevelManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        permissionSchemeManager = mockController.getMock(PermissionSchemeManager.class);
        permissionTypeManager = mockController.getMock(PermissionTypeManager.class);
        issueSecuritySchemeManager = mockController.getMock(IssueSecuritySchemeManager.class);
        issueSecurityTypeManager = mockController.getMock(SecurityTypeManager.class);
        projectFactory = mockController.getMock(ProjectFactory.class);
        theUser = new MockUser("fred");
    }

    @Test
    public void testUserHasPermissionForProjectAndSecurityTypeNullUser() throws Exception
    {
        final GenericValue project = new MockGenericValue("Project");
        final GenericValue entity = createEntityGV(null, "test");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(securityType.hasPermission(project, "test")).andReturn(true);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory);

        final boolean result = generator.userHasPermissionForProjectAndSecurityType(null, project, entity, securityType);

        assertTrue(result);
    }

    @Test
    public void testUserHasPermissionForProjectAndSecurityTypeUser() throws Exception
    {
        final GenericValue project = new MockGenericValue("Project");
        final GenericValue entity = createEntityGV(null, "test");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(securityType.hasPermission(project, "test", theUser, false)).andReturn(true);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory);

        final boolean result = generator.userHasPermissionForProjectAndSecurityType(theUser, project, entity, securityType);

        assertTrue(result);
    }

    @Test
    public void testCollectProjectTermsUserHasNoPermission() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(permissionSchemeManager.getSchemes(projectGV)).andReturn(Collections.singletonList(schemeGV));

        org.easymock.EasyMock.expect(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).andReturn(
            Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(permissionTypeManager.getSecurityType("type")).andReturn(securityType);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return false;
            }
        };

        generator.collectProjectTerms(projectGV, theUser, collect, Permissions.BROWSE);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectProjectTermsNoQueryGenerated() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(permissionSchemeManager.getSchemes(projectGV)).andReturn(Collections.singletonList(schemeGV));

        org.easymock.EasyMock.expect(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).andReturn(
            Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(permissionTypeManager.getSecurityType("type")).andReturn(securityType);

        org.easymock.EasyMock.expect(securityType.getQuery(theUser, projectGV, "parameter")).andReturn(null);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectProjectTerms(projectGV, theUser, collect, Permissions.BROWSE);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectProjectTermsHappyPath() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue schemeGV = new MockGenericValue("PermissionScheme");
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);
        final Query projectQuery = new TermQuery(new Term("project", "123"));

        org.easymock.EasyMock.expect(permissionSchemeManager.getSchemes(projectGV)).andReturn(Collections.singletonList(schemeGV));

        org.easymock.EasyMock.expect(permissionSchemeManager.getEntities(schemeGV, (long) Permissions.BROWSE)).andReturn(
            Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(permissionTypeManager.getSecurityType("type")).andReturn(securityType);

        org.easymock.EasyMock.expect(securityType.getQuery(theUser, projectGV, "parameter")).andReturn(projectQuery);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectProjectTerms(projectGV, theUser, collect, Permissions.BROWSE);

        assertEquals(1, collect.size());
        assertTrue(collect.contains(projectQuery));
    }

    @Test
    public void testCollectSecurityLevelTermsUserHasNoPermission() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue securityLevelGV = createSecurityLevelGV(123L);
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(issueSecurityLevelManager.getUsersSecurityLevels(projectGV, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(
            Collections.singletonList(securityLevelGV));

        org.easymock.EasyMock.expect(issueSecuritySchemeManager.getEntitiesBySecurityLevel(123L)).andReturn(Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(issueSecurityTypeManager.getSecurityType("type")).andReturn(securityType);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return false;
            }
        };

        generator.collectSecurityLevelTerms(projectGV, theUser, collect);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectSecurityLevelTermsNoQueryGenerated() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Project projectObject = new MockProject(555L);
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue securityLevelGV = createSecurityLevelGV(123L);
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);

        org.easymock.EasyMock.expect(issueSecurityLevelManager.getUsersSecurityLevels(projectGV, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(
            Collections.singletonList(securityLevelGV));

        org.easymock.EasyMock.expect(issueSecuritySchemeManager.getEntitiesBySecurityLevel(123L)).andReturn(Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(issueSecurityTypeManager.getSecurityType("type")).andReturn(securityType);

        org.easymock.EasyMock.expect(projectFactory.getProject(projectGV)).andReturn(projectObject);

        org.easymock.EasyMock.expect(securityType.getQuery(theUser, projectObject, securityLevelGV, "parameter")).andReturn(null);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectSecurityLevelTerms(projectGV, theUser, collect);

        assertEquals(0, collect.size());
    }

    @Test
    public void testCollectSecurityLevelTermsHappyPath() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Project projectObject = new MockProject(555L);
        final Set<Query> collect = new LinkedHashSet<Query>();
        final GenericValue securityLevelGV = createSecurityLevelGV(123L);
        final GenericValue entityGV = createEntityGV("type", "parameter");
        final SecurityType securityType = mockController.getMock(SecurityType.class);
        final Query securityLevelQuery = new TermQuery(new Term("issue_security_level", "123"));

        org.easymock.EasyMock.expect(issueSecurityLevelManager.getUsersSecurityLevels(projectGV, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(
            Collections.singletonList(securityLevelGV));

        org.easymock.EasyMock.expect(issueSecuritySchemeManager.getEntitiesBySecurityLevel(123L)).andReturn(Collections.singletonList(entityGV));

        org.easymock.EasyMock.expect(issueSecurityTypeManager.getSecurityType("type")).andReturn(securityType);

        org.easymock.EasyMock.expect(projectFactory.getProject(projectGV)).andReturn(projectObject);

        org.easymock.EasyMock.expect(securityType.getQuery(theUser, projectObject, securityLevelGV, "parameter")).andReturn(securityLevelQuery);

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            boolean userHasPermissionForProjectAndSecurityType(final com.atlassian.crowd.embedded.api.User searcher, final GenericValue project, final GenericValue entity, final SecurityType s)
            {
                assertSame(theUser, searcher);
                assertSame(projectGV, project);
                assertSame(securityType, s);
                return true;
            }
        };

        generator.collectSecurityLevelTerms(projectGV, theUser, collect);

        assertEquals(1, collect.size());
        assertTrue(collect.contains(securityLevelQuery));
    }

    @Test
    public void testGenerateQueryNoProjects() throws Exception
    {
        org.easymock.EasyMock.expect(permissionManager.getProjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.<GenericValue> emptyList());

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                fail("Should not have been called");
            }

            @Override
            void collectSecurityLevelTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries) throws GenericEntityException
            {
                fail("Should not have been called");
            }
        };

        final Query result = generator.getQuery(theUser, Permissions.BROWSE);

        final BooleanQuery expected = new BooleanQuery();

        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryNoProjectQueries() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");

        org.easymock.EasyMock.expect(permissionManager.getProjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.singletonList(projectGV));

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(projectGV, p);
                assertSame(theUser, searcher);

                // dont add anything
            }

            @Override
            void collectSecurityLevelTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries) throws GenericEntityException
            {
                fail("Should not be called if no project queries");
            }
        };

        final Query result = generator.getQuery(theUser, Permissions.BROWSE);

        final BooleanQuery expected = new BooleanQuery();

        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryHappyPath() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Query projectQuery = new TermQuery(new Term("project", "123"));
        final Query securityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "123"));
        final Query noSecurityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1"));

        org.easymock.EasyMock.expect(permissionManager.getProjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.singletonList(projectGV));

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(projectGV, p);
                assertSame(theUser, searcher);
                queries.add(projectQuery);
            }

            @Override
            void collectSecurityLevelTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries) throws GenericEntityException
            {
                assertSame(projectGV, p);
                assertSame(theUser, searcher);
                queries.add(securityLevelQuery);
            }
        };

        final Query result = generator.getQuery(theUser, Permissions.BROWSE);

        final BooleanQuery expected = new BooleanQuery();
        final BooleanQuery expectedProjectQuery = new BooleanQuery();
        expectedProjectQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedSecurityLevelQuery = new BooleanQuery();
        expectedSecurityLevelQuery.add(noSecurityLevelQuery, BooleanClause.Occur.SHOULD);
        expectedSecurityLevelQuery.add(securityLevelQuery, BooleanClause.Occur.SHOULD);
        expected.add(expectedProjectQuery, BooleanClause.Occur.MUST);
        expected.add(expectedSecurityLevelQuery, BooleanClause.Occur.MUST);

        assertEquals(expected, result);
    }

    @Test
    public void testGenerateQueryNoSecurityLevelQueries() throws Exception
    {
        final GenericValue projectGV = new MockGenericValue("Project");
        final Query projectQuery = new TermQuery(new Term("project", "123"));
        final Query noSecurityLevelQuery = new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1"));

        org.easymock.EasyMock.expect(permissionManager.getProjects(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) theUser)).andReturn(Collections.singletonList(projectGV));

        replay();

        final DefaultPermissionQueryFactory generator = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
            permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager, projectFactory)
        {
            @Override
            void collectProjectTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
            {
                assertSame(projectGV, p);
                assertSame(theUser, searcher);
                queries.add(projectQuery);
            }

            @Override
            void collectSecurityLevelTerms(final GenericValue p, final com.atlassian.crowd.embedded.api.User searcher, final Set<Query> queries) throws GenericEntityException
            {
                assertSame(projectGV, p);
                assertSame(theUser, searcher);
                // don't add anything
            }
        };

        final Query result = generator.getQuery(theUser, Permissions.BROWSE);

        final BooleanQuery expected = new BooleanQuery();
        final BooleanQuery expectedProjectQuery = new BooleanQuery();
        expectedProjectQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedSecurityLevelQuery = new BooleanQuery();
        expectedSecurityLevelQuery.add(noSecurityLevelQuery, BooleanClause.Occur.SHOULD);
        expected.add(expectedProjectQuery, BooleanClause.Occur.MUST);
        expected.add(expectedSecurityLevelQuery, BooleanClause.Occur.MUST);

        assertEquals(expected, result);
    }

    private GenericValue createEntityGV(final String type, final String parameter)
    {
        return new MockGenericValue("Entity", MapBuilder.newBuilder().add("type", type).add("parameter", parameter).toMap());
    }

    private GenericValue createSecurityLevelGV(final Long id)
    {
        return new MockGenericValue("IssueSecurityLevel", MapBuilder.newBuilder().add("id", id).toMap());
    }
}
