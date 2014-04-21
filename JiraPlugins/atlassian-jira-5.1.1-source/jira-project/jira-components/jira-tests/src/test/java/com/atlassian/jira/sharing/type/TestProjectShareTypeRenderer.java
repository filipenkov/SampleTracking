package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.web.util.MockOutlookManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.sharing.type.ProjectShareTypeRenderer}.
 *
 * @since v3.13
 */

public class TestProjectShareTypeRenderer extends ListeningTestCase
{
    private static final String PROJECT1_KEY = "PROJONE";
    private static final String PROJECT1_NAME = "Project One";
    private static final int PROJECT1_ID = 10;

    private static final long ROLE1_ID = 1000;
    private static final String ROLE1_NAME = "Cool";
    private static final String ROLE1_DESCRIPTION = "Cool Users Role";

    private static final Project PROJECT1 = new MockProject(TestProjectShareTypeRenderer.PROJECT1_ID, TestProjectShareTypeRenderer.PROJECT1_KEY, TestProjectShareTypeRenderer.PROJECT1_NAME);
    private static final Project PROJECT2 = new MockProject(11, "PROJTWO", "Project Two");
    private static final Project PROJECT3 = new MockProject(12, "ABC", "ABC Project");
    private static final Project PROJECTXSS = new MockProject(13, "XSS", "<script>alert(\"I'm an XSS attack\");</script>");
    public static final String PROJECTXSS_ENCODED = "&lt;script&gt;alert(&quot;I&#39;m an XSS attack&quot;);&lt;/script&gt;";

    private static final ProjectRole ROLE1 = new MockProjectRoleManager.MockProjectRole(TestProjectShareTypeRenderer.ROLE1_ID, TestProjectShareTypeRenderer.ROLE1_NAME, TestProjectShareTypeRenderer.ROLE1_DESCRIPTION);
    private static final ProjectRole ROLE2 = new MockProjectRoleManager.MockProjectRole(1001, "Dude", "The dudemisters.");
    private static final ProjectRole ROLE3 = new MockProjectRoleManager.MockProjectRole(1002, "Nerd", "The really really nerds.");
    private static final ProjectRole ROLE4 = new MockProjectRoleManager.MockProjectRole(1003, "Geek", "The really really geeks.");
    private static final ProjectRole ROLEXSS = new MockProjectRoleManager.MockProjectRole(1004, "<b>name</b>", "This is an XSS attack.");
    private static final String ROLEXSS_NAME_ENCODED = "&lt;b&gt;name&lt;/b&gt;";

    private static final SharePermission PROJECT_PERM = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(TestProjectShareTypeRenderer.PROJECT1_ID), null);
    private static final SharePermission PROJECT_PERM_XSS = new SharePermissionImpl(ProjectShareType.TYPE, TestProjectShareTypeRenderer.PROJECTXSS.getId().toString(), null);
    private static final SharePermission ROLE_PERM_1 = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(TestProjectShareTypeRenderer.PROJECT1_ID), String.valueOf(TestProjectShareTypeRenderer.ROLE1_ID));
    private static final SharePermission ROLE_PERM_XSS = new SharePermissionImpl(ProjectShareType.TYPE, String.valueOf(TestProjectShareTypeRenderer.PROJECT1_ID), TestProjectShareTypeRenderer.ROLEXSS.getId().toString());

    private static final String VELOCITY_RETURN = "<selector><option>b</option></selector>";
    private static final String UNKNOWN_PROJECT = "[Unknown Project]";
    private static final String UNKNOWN_ROLE = "[Unknown Role]";
    private static final String PROJECTS_KEY = "projects";
    private static final String ROLES_KEY = "roles";
    private static final String ROLES_MAP_KEY = "rolesMap";

    private JiraAuthenticationContext userCtx;
    private JiraAuthenticationContext anonymousCtx;
    private User user;
    private VelocityTemplatingEngine templatingEngine;
    private MockControl permMgrControl;
    private PermissionManager permMgr;
    private MockControl projectMgrControl;
    private ProjectManager projectMgr;
    private MockControl projectRoleMgrControl;
    private ProjectRoleManager projectRoleMgr;
    private ProjectShareTypeRenderer renderer;
    private ProjectFactory projectFactory;
    private EncodingConfiguration encoding = new EncodingConfiguration()
    {
        public String getEncoding()
        {
            return "UTF-8";
        }
    };

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("test");
        userCtx = createAuthenticationContext(user);
        anonymousCtx = createAuthenticationContext(null);
        templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput(VELOCITY_RETURN).get();
        permMgrControl = MockControl.createStrictControl(PermissionManager.class);
        permMgr = (PermissionManager) permMgrControl.getMock();

        projectMgrControl = MockControl.createStrictControl(ProjectManager.class);
        projectMgr = (ProjectManager) projectMgrControl.getMock();

        projectRoleMgrControl = MockControl.createStrictControl(ProjectRoleManager.class);
        projectRoleMgr = (ProjectRoleManager) projectRoleMgrControl.getMock();

        projectFactory = new MockProjectFactory();

        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        userCtx = null;
        user = null;
        templatingEngine = null;
        encoding = null;
        anonymousCtx = null;

        permMgrControl = null;
        projectMgrControl = null;
        projectRoleMgrControl = null;

        projectRoleMgr = null;
        projectMgr = null;
        permMgr = null;

        projectFactory = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, null, projectRoleMgr, permMgr, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectRoleManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, null, permMgr, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullPermissionManager()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, null, projectFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToInstantiateAProjectShareTypeRendererGivenANullProjectFactory()
    {
        new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, null);
    }

    @Test
    public void testGetShareTypeLabel()
    {
        initialiseMocks();

        final String html = renderer.getShareTypeLabel(userCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isEmpty(html));

        validateMocks();
    }

    @Test
    public void testGetShareTypeLabelAnonymousUser()
    {
        initialiseMocks();

        final String html = renderer.getShareTypeLabel(anonymousCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isEmpty(html));

        validateMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToGetAShareTypeLabelGivenANullAuthenticationContext()
    {
        renderer.getShareTypeLabel(null);
    }

    @Test
    public void testIsAddButtonNeededTrue()
    {
        permMgr.getProjects(Permissions.BROWSE, user);
        permMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT1.getGenericValue()));

        initialiseMocks();

        assertTrue(renderer.isAddButtonNeeded(userCtx));

        validateMocks();
    }

    @Test
    public void testIsAddButtonNeededFalse()
    {
        permMgr.getProjects(Permissions.BROWSE, user);
        permMgrControl.setReturnValue(Collections.EMPTY_LIST);

        initialiseMocks();

        assertFalse(renderer.isAddButtonNeeded(userCtx));

        validateMocks();
    }

    @Test
    public void testIsAddButtonNeededTrueWithAnonymousUser()
    {
        permMgr.getProjects(Permissions.BROWSE, null);
        permMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT1.getGenericValue()));

        initialiseMocks();

        assertTrue(renderer.isAddButtonNeeded(anonymousCtx));

        validateMocks();
    }

    @Test
    public void testIsAddButtonNeededFalseWithAnonymousUser()
    {
        permMgr.getProjects(Permissions.BROWSE, null);
        permMgrControl.setReturnValue(Collections.EMPTY_LIST);

        initialiseMocks();

        assertFalse(renderer.isAddButtonNeeded(anonymousCtx));

        validateMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkingWhetherAnAddButtonIsNeededOrNotShouldFailGivenANullAuthenticationContext()
    {
        renderer.isAddButtonNeeded(null);
    }

    @Test
    public void testRenderPermissionOfProjectWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM, userCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfProjectXSS()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECTXSS.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECTXSS);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM_XSS, userCtx);
        assertNotNull(html);
        assertFalse(html.contains(TestProjectShareTypeRenderer.PROJECTXSS.getName()));
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECTXSS_ENCODED));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfRoleXSS()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLEXSS.getId());
        projectRoleMgrControl.setReturnValue(TestProjectShareTypeRenderer.ROLEXSS);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_XSS, userCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));
        assertFalse(html.contains(TestProjectShareTypeRenderer.ROLEXSS.getName()));
        assertTrue(html.contains(TestProjectShareTypeRenderer.ROLEXSS_NAME_ENCODED));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM, userCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_PROJECT));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoRoleWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLE1_ID);
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, userCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_ROLE));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLE1_ID);
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, userCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_PROJECT));
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_ROLE));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfProjectWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLE1_ID);
        projectRoleMgrControl.setReturnValue(TestProjectShareTypeRenderer.ROLE1);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));
        assertTrue(html.contains(TestProjectShareTypeRenderer.ROLE1_NAME));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_PROJECT));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLE1_ID);
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.PROJECT1_NAME));
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_ROLE));

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        projectRoleMgr.getProjectRole(TestProjectShareTypeRenderer.ROLE1_ID);
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_PROJECT));
        assertTrue(html.contains(TestProjectShareTypeRenderer.UNKNOWN_ROLE));

        validateMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRenderANullPermissionType()
    {
        renderer.renderPermission(null, userCtx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRenderAPermissionGivenANullAuthenticationContext()
    {
        renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, null);
    }

    @Test
    public void testGetShareTypeEditor()
    {
        permMgr.getProjects(Permissions.BROWSE, user);
        permMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT1.getGenericValue(), TestProjectShareTypeRenderer.PROJECT2.getGenericValue(), TestProjectShareTypeRenderer.PROJECT3.getGenericValue()));

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT1);
        projectRoleMgrControl.setReturnValue(Collections.emptyList());

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT2);
        projectRoleMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE3));

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT3);
        projectRoleMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.ROLE3, TestProjectShareTypeRenderer.ROLE4));

        initialiseMocks();
        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestProjectShareTypeRenderer.PROJECTS_KEY));
                final Collection projects = (Collection) params.get(TestProjectShareTypeRenderer.PROJECTS_KEY);
                assertEquals(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT3, TestProjectShareTypeRenderer.PROJECT1, TestProjectShareTypeRenderer.PROJECT2), projects);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_KEY));
                final Collection roles = (Collection) params.get(TestProjectShareTypeRenderer.ROLES_KEY);
                assertEquals(ImmutableList.of(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE4, TestProjectShareTypeRenderer.ROLE3), roles);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_MAP_KEY));
                final Map rolesMap = (Map) params.get(TestProjectShareTypeRenderer.ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT1.getId()), Collections.EMPTY_LIST);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT2.getId()), ImmutableList.of(1001L, 1002L));
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT3.getId()), ImmutableList.of(1003L, 1002L));

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(TestProjectShareTypeRenderer.VELOCITY_RETURN, html);

        validateMocks();
    }

    @Test
    public void testGetShareTypeEditorAnonymous()
    {
        permMgr.getProjects(Permissions.BROWSE, null);
        permMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT1.getGenericValue(), TestProjectShareTypeRenderer.PROJECT2.getGenericValue(), TestProjectShareTypeRenderer.PROJECT3.getGenericValue()));

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT1);
        projectRoleMgrControl.setReturnValue(Collections.emptyList());

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT2);
        projectRoleMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE3));

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT3);
        projectRoleMgrControl.setReturnValue(ImmutableList.of(TestProjectShareTypeRenderer.ROLE1, TestProjectShareTypeRenderer.ROLE3, TestProjectShareTypeRenderer.ROLE4));

        initialiseMocks();

        renderer = new ProjectShareTypeRenderer(encoding, templatingEngine, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestProjectShareTypeRenderer.PROJECTS_KEY));
                final Collection projects = (Collection) params.get(TestProjectShareTypeRenderer.PROJECTS_KEY);
                assertEquals(ImmutableList.of(TestProjectShareTypeRenderer.PROJECT3, TestProjectShareTypeRenderer.PROJECT1, TestProjectShareTypeRenderer.PROJECT2), projects);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_KEY));
                final Collection roles = (Collection) params.get(TestProjectShareTypeRenderer.ROLES_KEY);
                assertEquals(ImmutableList.of(TestProjectShareTypeRenderer.ROLE1, TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE4, TestProjectShareTypeRenderer.ROLE3), roles);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_MAP_KEY));
                final Map rolesMap = (Map) params.get(TestProjectShareTypeRenderer.ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT1.getId()), Collections.emptyList());
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT2.getId()), ImmutableList.of(1001L, 1002L));
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT3.getId()), ImmutableList.of(1000L, 1003L, 1002L));

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertEquals(TestProjectShareTypeRenderer.VELOCITY_RETURN, html);

        validateMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingAShareTypeEditorShouldFailGivenANullAuthenticationContext()
    {
        renderer.getShareTypeEditor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplatesForAnEntityAndARenderModeShouldFailGivenANullAuthenticationContext()
    {
        renderer.getTranslatedTemplates(null, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRetrieveTheTranslatedTemplatesForANullEntityType()
    {
        renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplatesForAnEntityTypeShouldFailGivenANullRenderMode()
    {
        renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, null);
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        initialiseMocks();

        Map<String,String> actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_invalid_project", "share_invalid_role",
                "share_project_display_all", "share_project_display", "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_invalid_project", "share_invalid_role",
                "share_project_display_all", "share_project_display", "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_invalid_project", "share_invalid_role",
                "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_invalid_project", "share_invalid_role",
                "share_project_description", "share_role_description"), actualTemplates);

        validateMocks();
    }

    private void assertTemplates(final List<String> expectedKeys, final Map<String,String> actualTemplates)
    {
        assertEquals(expectedKeys.size(), actualTemplates.size());
        assertTrue(actualTemplates.keySet().containsAll(expectedKeys));
        for (Map.Entry entry : actualTemplates.entrySet())
        {
            assertTrue("Template for key '" + entry.getKey() + "' is blank.", isNotBlank((String) entry.getValue()));
        }
    }

    private void assertJsonEquals(final String array, final Collection expectedItems)
    {
        try
        {
            final JSONArray jsonArray = new JSONArray(array);
            final List<Long> jsonItems = newArrayListWithCapacity(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                jsonItems.add(jsonArray.getLong(i));
            }
            assertEquals(expectedItems, jsonItems);
        }
        catch (final JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void initialiseMocks()
    {
        permMgrControl.replay();
        projectRoleMgrControl.replay();
        projectMgrControl.replay();
    }

    private void validateMocks()
    {
        permMgrControl.verify();
        projectRoleMgrControl.verify();
        projectMgrControl.verify();
    }

    private JiraAuthenticationContext createAuthenticationContext(final User user)
    {
        return new MockAuthenticationContext(user, new MockOutlookManager(), new MockI18nBean());
    }

    private static class MockProjectFactory implements ProjectFactory
    {
        public Project getProject(final GenericValue projectGV)
        {
            return new MockProject(projectGV);
        }

        public List<Project> getProjects(final Collection projectGVs)
        {
            final List<Project> projects = newArrayListWithCapacity(projectGVs.size());
            for (Object projectGV : projectGVs)
            {
                final GenericValue gv = (GenericValue) projectGV;
                projects.add(getProject(gv));
            }

            return projects;
        }
    }
}
