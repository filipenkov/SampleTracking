package com.atlassian.jira.dashboard.permission;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestJiraGadgetPermissionManager extends MockControllerTestCase
{
    private User admin = new MockUser("admin");
    private User user = new MockUser("user");

    @Test
    public void testVoteOnNull()
    {
        JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(null, null, null);
        try
        {
            permissionManager.voteOn((PluginGadgetSpec) null, null);
            fail("Should have thrown exception!");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    //public PluginGadgetSpec(Plugin plugin, String moduleKey, String location, Map <String, String> params)

    @Test
    public void testVoteOnNonPluginGadget()
    {
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        expect(mockPluginAccessor.getEnabledPluginModule("somerandomthing:stuff")).andReturn(null);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        Vote vote = permissionManager.voteOn("somerandomthing:stuff", user);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnLoginGadget()
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets").times(2);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "login-gadget", "rest/login.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);

        //login gadget should be shown for logged out user.
        vote = permissionManager.voteOn(mockSpec, null);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnGadgetNoRolesSpecified()
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets").times(2);
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getParams()).andReturn(Collections.<String, String>emptyMap());
        final ModuleDescriptor mockModuleDescriptor2 = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor2.getParams()).andReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "").toMap());

        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).andReturn(mockModuleDescriptor);
        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget2")).andReturn(mockModuleDescriptor2);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        final PluginGadgetSpec mockSpec2 = new PluginGadgetSpec(mockPlugin, "intro-gadget2", "rest/intro.xml", MapBuilder.<String, String>newBuilder().add("roles-required", "").toMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec2, null);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnGadgetInvalidRolesSpecified()
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets").times(1);
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getParams()).andReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "someinvalidmumbojumbo").toMap());

        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).andReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.PASS, vote);
    }

    @Test
    public void testVoteOnGadgetGlobalRolesSpecified()
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets").times(2);
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getParams()).andReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "use").toMap()).times(2);
        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).andReturn(mockModuleDescriptor).times(2);

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.USE, admin)).andReturn(true);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.USE, user)).andReturn(false);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);
    }

    @Test
    public void testVoteOnGadgetProjectRolesSpecified() throws Exception
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets").times(2);
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getParams()).andReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "browse").toMap()).times(2);
        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).andReturn(mockModuleDescriptor).times(2);

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).andReturn(false);
        expect(mockPermissionManager.hasProjects(Permissions.BROWSE, admin)).andReturn(true);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        expect(mockPermissionManager.hasProjects(Permissions.BROWSE, user)).andReturn(false);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);
    }
    
    @Test
    public void testVoteOnGadgetGlobalAdmin() throws Exception
    {
        final Plugin mockPlugin = mockController.getMock(Plugin.class);
        expect(mockPlugin.getKey()).andReturn("com.atlassian.jira.gadgets");
        final PluginAccessor mockPluginAccessor = mockController.getMock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getParams()).andReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "browse").toMap());
        expect(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).andReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        expect(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).andReturn(true);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testFilterNullDashboardState()
    {
        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        try
        {
            permissionManager.filterGadgets(null, null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    @Test
    public void testFilterReadOnlyDashboardState()
    {
        List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        List<GadgetState> columnOne = new ArrayList<GadgetState>();
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget2.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream.xml")).build());
        List<GadgetState> columnTwo = new ArrayList<GadgetState>();
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:login-gadget/login.xml")).build());
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream3.xml")).build());
        columns.add(columnOne);
        columns.add(columnTwo);

        final DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("System Dashboard").columns(columns).build();

        final DashboardPermissionService mockPermissionService = mockController.getMock(DashboardPermissionService.class);
        expect(mockPermissionService.isWritableBy(DashboardId.valueOf("1"), user.getName())).andReturn(false);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final DashboardState filteredState = permissionManager.filterGadgets(state, user);

        assertNotSame(state, filteredState);
        assertEquals(Layout.AA, filteredState.getLayout());
        Iterator<GadgetState> stateIterator = filteredState.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO).iterator();
        assertEquals(URI.create("rest/gadgets/1.0/g/someothergadget.xml"), stateIterator.next().getGadgetSpecUri());
        assertEquals(URI.create("rest/gadgets/1.0/g/someothergadget2.xml"), stateIterator.next().getGadgetSpecUri());
        assertEquals(URI.create("http://www.atlassian.com/stream.xml"), stateIterator.next().getGadgetSpecUri());
        assertFalse(stateIterator.hasNext());
        stateIterator = filteredState.getGadgetsInColumn(DashboardState.ColumnIndex.ONE).iterator();
        assertEquals(URI.create("http://www.atlassian.com/stream3.xml"), stateIterator.next().getGadgetSpecUri());
        assertFalse(stateIterator.hasNext());
    }

    @Test
    public void testFilterWriteableDashboardState()
    {
        List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        List<GadgetState> columnOne = new ArrayList<GadgetState>();
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget2.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream.xml")).build());
        List<GadgetState> columnTwo = new ArrayList<GadgetState>();
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:login-gadget/login.xml")).build());
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream3.xml")).build());
        columns.add(columnOne);
        columns.add(columnTwo);

        final DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("System Dashboard").columns(columns).build();

        final DashboardPermissionService mockPermissionService = mockController.getMock(DashboardPermissionService.class);
        expect(mockPermissionService.isWritableBy(DashboardId.valueOf("1"), user.getName())).andReturn(true);

        final JiraGadgetPermissionManager permissionManager = mockController.instantiate(JiraGadgetPermissionManager.class);
        final DashboardState filteredState = permissionManager.filterGadgets(state, user);

        assertEquals(state, filteredState);
    }

    @Test
    public void testExtractModuleKey()
    {
        JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(null, null, null);
        String key = permissionManager.extractModuleKey("http://localhost:8090/jira/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/some/gadgets/admin-gadget.xml");
        assertNull(key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/admin-gadget.xml");
        assertNull(key);
    }
}
