package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import com.atlassian.jira.web.bean.PercentageGraphRow;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestRoadMapPortlet extends MockControllerTestCase
{
    private static final Long PROJECT_1 = new Long(1);
    private static final Long PROJECT_2 = new Long(2);
    private static final Long PROJECT_3 = new Long(3);
    private static final Long PROJECT_999 = new Long(999);

    private JiraAuthenticationContext authContext;
    private Project project;
    private Version version;
    private SearchService searchService;


    @After
    public void tearDown() throws Exception
    {
        project = null;
        authContext = null;
        version = null;

    }

    @Before
    public void setUp() throws Exception
    {
        searchService = mockController.getMock(SearchService.class);
        mockController.replay();
        project = getProject();
        authContext = getContext();
        version = getVersion(project);
    }

    @Test
    public void testFilterProjectsByPermissionNoProjects()
    {
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService);
        assertTrue(portlet.filterProjectsByPermission(Collections.EMPTY_SET).isEmpty());
    }

    @Test
    public void testFilterProjectsByPermissionNone()
    {
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            boolean canBrowseProject(Long projectId)
            {
                return false;
            }
        };
        Set projectIds = new HashSet(EasyList.build(PROJECT_999, PROJECT_2, PROJECT_3, PROJECT_1));
        assertTrue(portlet.filterProjectsByPermission(projectIds).isEmpty());
    }

    @Test
    public void testFilterProjectsByPermission()
    {
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            boolean canBrowseProject(Long projectId)
            {
                return PROJECT_2.equals(projectId) || PROJECT_3.equals(projectId);
            }
        };
        Set projectIds = new HashSet(EasyList.build(PROJECT_999, PROJECT_2, PROJECT_3, PROJECT_1));
        final Set filtered = portlet.filterProjectsByPermission(projectIds);
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(PROJECT_2));
        assertTrue(filtered.contains(PROJECT_3));
    }

    @Test
    public void testGetProjectIdsNull()
    {
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService);
        assertTrue(portlet.getProjectIds(null).isEmpty());
    }

    @Test
    public void testGetProjectIdsEmpty()
    {
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService);
        assertTrue(portlet.getProjectIds(new ArrayList()).isEmpty());
    }

    @Test
    public void testGetProjectIds()
    {
        final Long cat1 = new Long(1);
        final Long cat2 = new Long(2);
        final Long cat3 = new Long(3);
        final Map categoryToProjIdsMap = EasyMap.build(
                cat1, new HashSet(EasyList.build(PROJECT_2, PROJECT_3)),
                cat2, new HashSet(EasyList.build(PROJECT_999)),
                cat3, new HashSet(EasyList.build(PROJECT_1))
        );
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            Set /* <Long> */ getProjectIdsForCategory(Long categoryId)
            {
                return (Set) categoryToProjIdsMap.get(categoryId);
            }

            boolean canBrowseProject(Long projectId)
            {
                return true;
            }
        };
        List projAndCatIds = EasyList.build("cat2", "cat1", "cat3", "55", "66");
        Set projIds = portlet.getProjectIds(projAndCatIds);

        assertNotNull(projIds);
        assertEquals(6, projIds.size());
        assertTrue(projIds.containsAll(EasyList.build(PROJECT_1, PROJECT_2, PROJECT_3, PROJECT_999, new Long(55), new Long(66))));
    }

    @Test
    public void testGetVersionsEmptySet()
    {
        final Set projectIds = new HashSet();
        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService);
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, 8, 28);

        List versions = portlet.getVersions(projectIds, cal);

        assertNotNull(versions);
        assertEquals(0, versions.size());
    }

    @Test
    public void testGetVersions()
    {
        final MockVersion version1 = createVersion(new Long(1), 2007, 1, 1);
        final MockVersion version2 = createVersion(new Long(2), 2007, 3, 13);
        final MockVersion version3 = createVersion(new Long(3), 2007, 5, 15);
        final MockVersion version4 = createVersion(new Long(4), 2007, 8, 18);
        final MockVersion version5 = createVersion(new Long(5), 2007, 10, 10);
        final MockVersion versionSame = createVersion(new Long(6), 2007, 8, 28);
        final MockVersion versionDuplicate = createVersion(new Long(7), 2007, 1, 1);

        final Map projectIdToVersionMap = EasyMap.build(
                PROJECT_1, EasyList.build(version5, version4),
                PROJECT_2, EasyList.build(version1, version2, version3),
                PROJECT_3, EasyList.build(versionSame, versionDuplicate),
                PROJECT_999, new ArrayList()
        );
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, 8, 28);

        final RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            Collection /* <Version> */ getVersionsForProject(Long projectId, Date releasedBefore)
            {
                assertEquals(cal.getTime(), releasedBefore);
                return (Collection) projectIdToVersionMap.get(projectId);
            }
        };

        Set projectIds = new HashSet(EasyList.build(PROJECT_1, PROJECT_2, PROJECT_999, PROJECT_3));
        List versions = portlet.getVersions(projectIds, cal);
        assertNotNull(versions);
        assertEquals(7, versions.size());
        assertTrue(version1.equals(versions.get(0)) || version1.equals(versions.get(1)));
        assertTrue(versionDuplicate.equals(versions.get(0)) || versionDuplicate.equals(versions.get(1)));
        assertEquals(version2, versions.get(2));
        assertEquals(version3, versions.get(3));
        assertEquals(version4, versions.get(4));
        assertEquals(versionSame, versions.get(5));
        assertEquals(version5, versions.get(6));

        projectIds = new HashSet(EasyList.build(PROJECT_3, PROJECT_2, PROJECT_999, PROJECT_1));
        versions = portlet.getVersions(projectIds, cal);
        assertNotNull(versions);
        assertEquals(7, versions.size());
        assertTrue(version1.equals(versions.get(0)) || version1.equals(versions.get(1)));
        assertTrue(versionDuplicate.equals(versions.get(0)) || versionDuplicate.equals(versions.get(1)));
        assertEquals(version2, versions.get(2));
        assertEquals(version3, versions.get(3));
        assertEquals(version4, versions.get(4));
        assertEquals(versionSame, versions.get(5));
        assertEquals(version5, versions.get(6));

        projectIds = new HashSet(EasyList.build(PROJECT_1));
        versions = portlet.getVersions(projectIds, cal);
        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertEquals(version4, versions.get(0));
        assertEquals(version5, versions.get(1));

    }

    @Test
    public void testGetVersionsForProject()
    {
        final Long projectId = new Long(1);
        final MockVersion version1 = createVersion(new Long(1), 2007, 1, 1);
        final MockVersion version2 = createVersion(new Long(2), 2007, 3, 13);
        final MockVersion version3 = createVersion(new Long(3), 2007, 5, 15);
        final MockVersion version4 = createVersion(new Long(4), 2007, 8, 18);
        final MockVersion version5 = createVersion(new Long(5), 2007, 10, 10);
        final MockVersion versionSame = createVersion(new Long(6), 2007, 8, 28);
        final MockVersion versionDuplicate = createVersion(new Long(7), 2007, 1, 1);

        Collection versions = new ArrayList();
        versions.add(version1);
        versions.add(version2);
        versions.add(version3);
        versions.add(version4);
        versions.add(version5);
        versions.add(versionSame);
        versions.add(versionDuplicate);

        Mock mockVersionManager = new Mock(VersionManager.class);
        mockVersionManager.setStrict(true);
        mockVersionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(projectId), P.eq(Boolean.FALSE)), versions);

        RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, (VersionManager) mockVersionManager.proxy(), searchService);

        final Calendar cal = Calendar.getInstance();
        cal.set(2007, 8, 28);

        final Collection retVersions = portlet.getVersionsForProject(projectId, cal.getTime());
        assertEquals(6, retVersions.size());

        assertTrue(retVersions.contains(version1));
        assertTrue(retVersions.contains(version2));
        assertTrue(retVersions.contains(version3));
        assertTrue(retVersions.contains(version4));
        assertTrue(retVersions.contains(versionSame));
        assertTrue(retVersions.contains(versionDuplicate));

        mockVersionManager.verify();
    }

    private MockVersion createVersion(Long id, int year, int month, int day)
    {
        final MockVersion version1 = new MockVersion();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        version1.setReleaseDate(cal.getTime());
        version1.setId(id);
        return version1;
    }

    @Test
    public void testGetGraphModelForVersion() throws SearchException
    {
        RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            long getAllIssueCount(Version version) throws SearchException
            {
                return 10;
            }

            long getUnresolvedIssueCount(Version version) throws SearchException
            {
                return 3;
            }

            TerminalClause createResolutionClause()
            {
                return null;
            }

            String getResolutionQueryString(TerminalClause terminalClause)
            {
                return "";
            }

            public String getText(String key)
            {
                return key;
            }

            @Override
            public String getNavigatorUrl(final Project project, final Version version, final boolean unresolved)
            {
                return "";
            }
        };

        PercentageGraphModel model = portlet.getGraphModelForVersion(version);
        assertEquals(10, model.getTotal());
        List rows = model.getRows();
        assertEquals(2, rows.size());

        PercentageGraphRow row1 = (PercentageGraphRow) rows.get(0);
        assertEquals(7, row1.getNumber());
        assertEquals("#009900", row1.getColor());
        assertEquals("common.concepts.resolved.issues", row1.getDescription());

        PercentageGraphRow row2 = (PercentageGraphRow) rows.get(1);
        assertEquals(3, row2.getNumber());
        assertEquals("#cc0000", row2.getColor());
        assertEquals("common.concepts.unresolved.issues", row2.getDescription());
    }

    @Test
    public void testGetGraphModelForVersionNoIssues() throws SearchException
    {
        RoadMapPortlet portlet = new RoadMapPortlet(authContext, null, null, null, null, null, null, searchService)
        {
            long getAllIssueCount(Version version) throws SearchException
            {
                return 0;
            }
        };

        PercentageGraphModel model = portlet.getGraphModelForVersion(version);
        assertEquals(0, model.getTotal());
        assertTrue(model.getRows().isEmpty());
    }

    @Test
    public void testGetAllIssueCount() throws SearchException
    {
        Constraint constraint = new Constraint()
        {

            public boolean eval(Object object)
            {
                Query query = (Query) object;

                assertEquals("{project = 123} AND {fixVersion = 555}", query.getWhereClause().toString());
                return true;
            }
        };
        RoadMapPortlet portlet = getPortlet(constraint);

        assertEquals(99, portlet.getAllIssueCount(version));
    }

    @Test
    public void testGetUnresolvedIssueCount() throws SearchException
    {
        Constraint constraint = new Constraint()
        {

            public boolean eval(Object object)
            {
                Query query = (Query) object;

                String expectedClause = "{project = 123} AND {resolution = \"Unresolved\"} AND {fixVersion = 555}";
                Clause resolutionClause = query.getWhereClause();
                assertNotNull(resolutionClause);
                assertEquals(expectedClause, resolutionClause.toString());
                return true;
            }
        };
        RoadMapPortlet portlet = getPortlet(constraint);

        assertEquals(99, portlet.getUnresolvedIssueCount(version));
    }

    private RoadMapPortlet getPortlet(Constraint constraint)
    {
        SearchProvider provider = getSearchProvider(constraint);
        ProjectManager projMgr = getProjectManager(project);
        return new RoadMapPortlet(authContext, null, null, provider, null, projMgr, null, searchService);
    }

    private Version getVersion(Project project)
    {
        Mock verMock = new Mock(Version.class);
        verMock.setStrict(true);
        verMock.expectAndReturn("getProjectObject", project);
        verMock.expectAndReturn("getId", new Long(555));
        return (Version) verMock.proxy();
    }

    private ProjectManager getProjectManager(Project project)
    {
        Mock projMgrMock = new Mock(ProjectManager.class);
        projMgrMock.setStrict(true);
        projMgrMock.expectAndReturn("getProjectObj", P.args(P.eq(new Long(123))), project);
        projMgrMock.expectAndReturn("getProject", P.args(P.eq(new Long(123))), null);
        return (ProjectManager) projMgrMock.proxy();
    }

    private Project getProject()
    {
        Mock projMock = new Mock(Project.class);
        projMock.setStrict(true);
        projMock.expectAndReturn("getId", new Long(123));
        return (Project) projMock.proxy();
    }

    private JiraAuthenticationContext getContext()
    {
        Mock ctxMock = new Mock(JiraAuthenticationContext.class);
        ctxMock.setStrict(true);
        ctxMock.expectAndReturn("getUser", null);
        return (JiraAuthenticationContext) ctxMock.proxy();
    }

    private SearchProvider getSearchProvider(Constraint constraint)
    {
        Mock spMock = new Mock(SearchProvider.class);
        spMock.setStrict(true);
        spMock.expectAndReturn("searchCount", P.args(constraint, P.IS_NULL), new Long(99));
        return (SearchProvider) spMock.proxy();
    }


}
