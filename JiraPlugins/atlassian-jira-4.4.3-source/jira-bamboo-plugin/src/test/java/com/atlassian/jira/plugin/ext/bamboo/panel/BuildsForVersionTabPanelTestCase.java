package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ComponentManagerMocker;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@PrepareForTest( {ComponentManager.class} )
@PowerMockIgnore("org.apache.log4j.*")
@RunWith(PowerMockRunner.class)
public class BuildsForVersionTabPanelTestCase
{
    private JiraAuthenticationContext authenticationContext;

    private SearchProvider searchProvider;

    private BambooPanelHelper bambooPanelHelper;

    private BambooApplicationLinkManager bambooApplicationLinkManager;

    private PermissionManager permissionManager;

    private Project project;

    private Version version;

    private BrowseVersionContext browseVersionContext;

    private final ComponentManagerMocker componentManagerMocker =  new ComponentManagerMocker();
    
    private BuildsForVersionTabPanel buildsForVersionTabPanel;

    private BambooReleaseService bambooReleaseService;

    @BeforeClass
    public static void setUpMultiTenantContext()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Before
    public void setUp() throws Exception
    {
        componentManagerMocker.doMock();

        browseVersionContext = mock(BrowseVersionContext.class);
        version = mock(Version.class);
        project = mock(Project.class);
        permissionManager = mock(PermissionManager.class);
        bambooPanelHelper = mock(BambooPanelHelper.class);
        searchProvider = mock(SearchProvider.class);
        bambooApplicationLinkManager = mock(BambooApplicationLinkManager.class);
        authenticationContext = mock(JiraAuthenticationContext.class);
        bambooReleaseService = mock(BambooReleaseService.class);

        when(browseVersionContext.getProject()).thenReturn(project);
        when(browseVersionContext.getVersion()).thenReturn(version);

        buildsForVersionTabPanel = new TestBuildsForVersionTabPanel();
    }

    @Test
    public void testPanelNotShownIfUserDoesNotHaveViewVersionControlPermission()
    {
        assertFalse(new TestBuildsForVersionTabPanel()
        {
            @Override
            protected boolean shouldShowPanelAccordingToSuperClass(BrowseVersionContext context)
            {
                return true;
            }
        }.showPanel(browseVersionContext));
    }

    @Test
    public void testPanelNotShownIfBambooNotConfigured()
    {
        when(
                permissionManager.hasPermission(
                        eq(Permissions.VIEW_VERSION_CONTROL),
                        eq(project),
                        (User) anyObject()
                )
        ).thenReturn(true);


        assertFalse(new TestBuildsForVersionTabPanel()
        {
            @Override
            protected boolean shouldShowPanelAccordingToSuperClass(BrowseVersionContext context)
            {
                return true;
            }
        }.showPanel(browseVersionContext));
    }

    @Test
    public void testPanelNotShownIfParentClassShowPanelReturnsFalse()
    {
        assertFalse(new TestBuildsForVersionTabPanel()
        {
            @Override
            protected boolean shouldShowPanelAccordingToSuperClass(BrowseVersionContext context)
            {
                return false;
            }
        }.showPanel(browseVersionContext));
    }

    @Test
    public void testPanelShownIfBambooConfiguredAndUserHasViewVcPermission()
    {
        when(
                permissionManager.hasPermission(
                        eq(Permissions.VIEW_VERSION_CONTROL),
                        eq(project),
                        (User) anyObject()
                )
        ).thenReturn(true);
        when(bambooApplicationLinkManager.hasApplicationLinks()).thenReturn(true);


        assertTrue(new TestBuildsForVersionTabPanel()
        {
            @Override
            protected boolean shouldShowPanelAccordingToSuperClass(BrowseVersionContext context)
            {
                return true;
            }
        }.showPanel(browseVersionContext));
    }

    @Test
    public void testCreateVelocityParamsBasedOnVersion()
    {
        String projectKey = "TST";
        long versionId = 10000L;

        when(project.getKey()).thenReturn(projectKey);
        when(version.getId()).thenReturn(versionId);

        Map<String, Object> actualVelocityParams = new TestBuildsForVersionTabPanel()
        {
            @Override
            protected Map<String, Object> getVelocityParamsFromParent(BrowseVersionContext context)
            {
                return new HashMap<String, Object>();
            }
        }.createVelocityParams(browseVersionContext);

        verify(bambooPanelHelper).prepareVelocityContext(
                actualVelocityParams,
                "bamboo-version-tabpanel",
                "/browse/" + projectKey + "/fixforversion/" + versionId + "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + "bamboo-version-tabpanel",
                "versionId=" + versionId,
                BambooPanelHelper.SUB_TABS,
                project
        );
    }

    private class TestBuildsForVersionTabPanel extends BuildsForVersionTabPanel
    {
        private TestBuildsForVersionTabPanel()
        {
            super(BuildsForVersionTabPanelTestCase.this.authenticationContext, BuildsForVersionTabPanelTestCase.this.searchProvider, bambooPanelHelper, permissionManager, bambooApplicationLinkManager, bambooReleaseService);
        }
    }
    
}
