package com.atlassian.jira.sharing.type;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
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
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.VelocityException;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    private static final int ROLE1_ID = 1000;
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
    private VelocityManager velocityManager;
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
        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("test", mpa, new MockCrowdService());
        userCtx = createAuthenticationContext(user);
        anonymousCtx = createAuthenticationContext(null);
        velocityManager = (VelocityManager) DuckTypeProxy.getProxy(VelocityManager.class, new Object()
        {
            public String getEncodedBody(final String templateDirectory, final String template, final String encoding, final Map contextParameters)
                    throws VelocityException
            {
                return TestProjectShareTypeRenderer.VELOCITY_RETURN;
            }
        });
        permMgrControl = MockControl.createStrictControl(PermissionManager.class);
        permMgr = (PermissionManager) permMgrControl.getMock();

        projectMgrControl = MockControl.createStrictControl(ProjectManager.class);
        projectMgr = (ProjectManager) projectMgrControl.getMock();

        projectRoleMgrControl = MockControl.createStrictControl(ProjectRoleManager.class);
        projectRoleMgr = (ProjectRoleManager) projectRoleMgrControl.getMock();

        projectFactory = new MockProjectFactory();

        renderer = new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, projectRoleMgr, permMgr, projectFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        userCtx = null;
        user = null;
        velocityManager = null;
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

    @Test
    public void testConstructorWithNullProjectManager()
    {
        try
        {
            new ProjectShareTypeRenderer(encoding, velocityManager, null, projectRoleMgr, permMgr, projectFactory);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testConstructorWithNullProjectRoleManager()
    {
        try
        {
            new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, null, permMgr, projectFactory);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testConstructorWithNullPermissionManager()
    {
        try
        {
            new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, projectRoleMgr, null, projectFactory);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testConstructorWithNullProjectFactory()
    {
        try
        {
            new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, projectRoleMgr, permMgr, null);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
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

    @Test
    public void testGetShareTypeLabelWithNullCtx()
    {
        try
        {
            renderer.getShareTypeLabel(null);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testIsAddButtonNeededTrue()
    {
        permMgr.getProjects(Permissions.BROWSE, user);
        permMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.PROJECT1.getGenericValue()));

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
        permMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.PROJECT1.getGenericValue()));

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

    @Test
    public void testIsAddButtonNeededWithNullCtx()
    {
        try
        {
            renderer.isAddButtonNeeded(null);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionOfProjectWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.PROJECT_PERM, userCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.PROJECT1_NAME) >= 0);

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
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_PROJECT) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoRoleWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(new Long(TestProjectShareTypeRenderer.ROLE1_ID));
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, userCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.PROJECT1_NAME) >= 0);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_ROLE) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        projectRoleMgr.getProjectRole(new Long(TestProjectShareTypeRenderer.ROLE1_ID));
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, userCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_PROJECT) >= 0);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_ROLE) >= 0);

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
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.PROJECT1_NAME) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(new Long(TestProjectShareTypeRenderer.ROLE1_ID));
        projectRoleMgrControl.setReturnValue(TestProjectShareTypeRenderer.ROLE1);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.PROJECT1_NAME) >= 0);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.ROLE1_NAME) >= 0);

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
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_PROJECT) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(TestProjectShareTypeRenderer.PROJECT1);

        projectRoleMgr.getProjectRole(new Long(TestProjectShareTypeRenderer.ROLE1_ID));
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.PROJECT1_NAME) >= 0);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_ROLE) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionOfNoProjectNoRoleWithAnonymousUser()
    {
        projectMgr.getProjectObj(TestProjectShareTypeRenderer.PROJECT1.getId());
        projectMgrControl.setReturnValue(null);

        projectRoleMgr.getProjectRole(new Long(TestProjectShareTypeRenderer.ROLE1_ID));
        projectRoleMgrControl.setReturnValue(null);

        initialiseMocks();

        final String html = renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, anonymousCtx);
        assertNotNull(html);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_PROJECT) >= 0);
        assertTrue(html.indexOf(TestProjectShareTypeRenderer.UNKNOWN_ROLE) >= 0);

        validateMocks();
    }

    @Test
    public void testRenderPermissionWithNullPermission()
    {
        try
        {
            renderer.renderPermission(null, userCtx);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionWithNullCtx()
    {
        try
        {
            renderer.renderPermission(TestProjectShareTypeRenderer.ROLE_PERM_1, null);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetShareTypeEditor()
    {
        permMgr.getProjects(Permissions.BROWSE, user);
        permMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.PROJECT1.getGenericValue(), TestProjectShareTypeRenderer.PROJECT2.getGenericValue(), TestProjectShareTypeRenderer.PROJECT3.getGenericValue()));

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT1);
        projectRoleMgrControl.setReturnValue(Collections.EMPTY_LIST);

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT2);
        projectRoleMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE3));

        projectRoleMgr.getProjectRoles(user, TestProjectShareTypeRenderer.PROJECT3);
        projectRoleMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.ROLE3, TestProjectShareTypeRenderer.ROLE4));

        initialiseMocks();
        renderer = new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestProjectShareTypeRenderer.PROJECTS_KEY));
                final Collection projects = (Collection) params.get(TestProjectShareTypeRenderer.PROJECTS_KEY);
                assertEquals(EasyList.build(TestProjectShareTypeRenderer.PROJECT3, TestProjectShareTypeRenderer.PROJECT1, TestProjectShareTypeRenderer.PROJECT2), projects);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_KEY));
                final Collection roles = (Collection) params.get(TestProjectShareTypeRenderer.ROLES_KEY);
                assertEquals(EasyList.build(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE4, TestProjectShareTypeRenderer.ROLE3), roles);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_MAP_KEY));
                final Map rolesMap = (Map) params.get(TestProjectShareTypeRenderer.ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT1.getId()), Collections.EMPTY_LIST);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT2.getId()), EasyList.build(new Long(1001), new Long(1002)));
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT3.getId()), EasyList.build(new Long(1003), new Long(1002)));

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
        permMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.PROJECT1.getGenericValue(), TestProjectShareTypeRenderer.PROJECT2.getGenericValue(), TestProjectShareTypeRenderer.PROJECT3.getGenericValue()));

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT1);
        projectRoleMgrControl.setReturnValue(Collections.EMPTY_LIST);

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT2);
        projectRoleMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE3));

        projectRoleMgr.getProjectRoles(null, TestProjectShareTypeRenderer.PROJECT3);
        projectRoleMgrControl.setReturnValue(EasyList.build(TestProjectShareTypeRenderer.ROLE1, TestProjectShareTypeRenderer.ROLE3, TestProjectShareTypeRenderer.ROLE4));

        initialiseMocks();

        renderer = new ProjectShareTypeRenderer(encoding, velocityManager, projectMgr, projectRoleMgr, permMgr, projectFactory)
        {
            Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestProjectShareTypeRenderer.PROJECTS_KEY));
                final Collection projects = (Collection) params.get(TestProjectShareTypeRenderer.PROJECTS_KEY);
                assertEquals(EasyList.build(TestProjectShareTypeRenderer.PROJECT3, TestProjectShareTypeRenderer.PROJECT1, TestProjectShareTypeRenderer.PROJECT2), projects);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_KEY));
                final Collection roles = (Collection) params.get(TestProjectShareTypeRenderer.ROLES_KEY);
                assertEquals(EasyList.build(TestProjectShareTypeRenderer.ROLE1, TestProjectShareTypeRenderer.ROLE2, TestProjectShareTypeRenderer.ROLE4, TestProjectShareTypeRenderer.ROLE3), roles);

                assertTrue(params.containsKey(TestProjectShareTypeRenderer.ROLES_MAP_KEY));
                final Map rolesMap = (Map) params.get(TestProjectShareTypeRenderer.ROLES_MAP_KEY);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT1.getId()), Collections.EMPTY_LIST);
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT2.getId()), EasyList.build(new Long(1001), new Long(1002)));
                assertJsonEquals((String) rolesMap.get(TestProjectShareTypeRenderer.PROJECT3.getId()), EasyList.build(new Long(1000), new Long(1003), new Long(1002)));

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertEquals(TestProjectShareTypeRenderer.VELOCITY_RETURN, html);

        validateMocks();
    }

    @Test
    public void testGetShareTypeEditorWithNullCtx()
    {
        try
        {
            renderer.getShareTypeEditor(null);
            fail("Null argument should not be accepted.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetTranslatedTemplatesNullCtx()
    {
        try
        {
            renderer.getTranslatedTemplates(null, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplatesNullEntityType()
    {
        try
        {
            renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplatesNullRenderMode()
    {
        try
        {
            renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        initialiseMocks();

        Map actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_invalid_project", "share_invalid_role",
                "share_project_display_all", "share_project_display", "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_invalid_project", "share_invalid_role",
                "share_project_display_all", "share_project_display", "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(userCtx, SearchRequest.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_invalid_project", "share_invalid_role",
                "share_project_description", "share_role_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_invalid_project", "share_invalid_role",
                "share_project_description", "share_role_description"), actualTemplates);

        validateMocks();
    }

    private void assertTemplates(final List expectedKeys, final Map actualTemplates)
    {
        assertEquals(expectedKeys.size(), actualTemplates.size());
        assertTrue(actualTemplates.keySet().containsAll(expectedKeys));
        for (Iterator iterator = actualTemplates.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry currentEntry = (Map.Entry) iterator.next();
            assertTrue("Template for key '" + currentEntry.getKey() + "' is blank.", StringUtils.isNotBlank((String) currentEntry.getValue()));
        }
    }

    private void assertJsonEquals(final String array, final Collection expectedItems)
    {
        try
        {
            final JSONArray jsonArray = new JSONArray(array);
            final List jsonItems = new ArrayList(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                jsonItems.add(new Long(jsonArray.getLong(i)));
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
            final List projects = new ArrayList(projectGVs.size());
            for (final Iterator iterator = projectGVs.iterator(); iterator.hasNext();)
            {
                final GenericValue gv = (GenericValue) iterator.next();
                projects.add(getProject(gv));
            }

            return projects;
        }
    }
}
