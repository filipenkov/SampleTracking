package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import webwork.action.ActionContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BambooPanelHelperTestCase
{
    @Mock
    private WebResourceManager webResourceManager;

    @Mock
    private BambooApplicationLinkManager bambooApplicationLinkManager;

    @Mock
    private ApplicationLink applicationLink;

    @Mock
    private Project project;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private PermissionManager permissionManager;

    private BambooPanelHelper bambooPanelHelper;

    private String bambooPluginModuleKey;

    private String queryString;

    private String baseLinkUrl;

    private List subTabs;

    private Map<String, Object> velocityContext;

    private Map<String, String[]> params;

    private Map<String, Object> session;

    @Before
    public void setUp() throws Exception
    {
        when(project.getKey()).thenReturn("TST");
        when(applicationLink.getDisplayUrl()).thenReturn(URI.create("http://localhost:8080/bamboo"));
        when(applicationLink.getRpcUrl()).thenReturn(URI.create("http://localhost:8080/bamboo"));
        when(bambooApplicationLinkManager.getApplicationLink(any(String.class))).thenReturn(applicationLink);

        bambooPanelHelper = new TestBambooPanelHelper();

        bambooPluginModuleKey = "xyz";
        queryString = "issueKey=TST-1";
        baseLinkUrl = "/browse/TST-1?selected=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + bambooPluginModuleKey;
        subTabs = new ArrayList();
        velocityContext = new HashMap<String, Object>();
        params = new HashMap<String, String[]>();
        session = new HashMap<String, Object>();

        ActionContext.setParameters(params);
        ActionContext.setSession(session);
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            verify(webResourceManager).requireResource(BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + "css");
        }
        finally
        {
            ActionContext.setParameters(null);
            ActionContext.setSession(null);
        }

    }

    private void setUpCommonEntriesInExpectedVelocityContext(Map<String, Object> expectedVelocityContext)
    {
        expectedVelocityContext.put("moduleKey", bambooPluginModuleKey);
        expectedVelocityContext.put("querySection", queryString);
        expectedVelocityContext.put("baseLinkUrl", baseLinkUrl);
        expectedVelocityContext.put("baseResourceUrl", "/download/resources/" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + bambooPluginModuleKey);
        expectedVelocityContext.put("baseBambooUrl", "http://localhost:8080/bamboo");
        expectedVelocityContext.put("baseRestUrl", "/rest/bamboo/1.0/");
        expectedVelocityContext.put("baseBambooRestProxyUrl", "/rest/bamboo/1.0/proxy/");
        expectedVelocityContext.put("baseBambooRestUrl", "http://localhost:8080/bamboo/rest/api/latest/");
        expectedVelocityContext.put("bambooServerName", null);
    }

    @Test
    public void testSelectedTabDefaultsToBuildByDateTabIfSubTabsNotSpecified()
    {
        Map<String, Object> expectedVelocityContext = new HashMap<String, Object>();

        bambooPanelHelper.prepareVelocityContext(
                velocityContext,
                bambooPluginModuleKey,
                baseLinkUrl,
                queryString,
                subTabs,
                project
        );


        expectedVelocityContext.put(BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_BUILD_BY_DATE);
        expectedVelocityContext.put("showRss", true);
        expectedVelocityContext.put("isSystemAdmin", false);
        setUpCommonEntriesInExpectedVelocityContext(expectedVelocityContext);

        assertEquals(expectedVelocityContext, velocityContext);
    }

    @Test
    public void testSelectedTabBasedOnSession()
    {
        Map<String, Object> expectedVelocityContext = new HashMap<String, Object>();

        session.put(BambooPanelHelper.BAMBOO_PLUGIN_KEY + "." + BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_PLAN_STATUS);
        subTabs.add("fake");

        bambooPanelHelper.prepareVelocityContext(
                velocityContext,
                bambooPluginModuleKey,
                baseLinkUrl,
                queryString,
                subTabs,
                project
        );

        expectedVelocityContext.put(BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_PLAN_STATUS);
        expectedVelocityContext.put("availableTabs", subTabs);
        expectedVelocityContext.put("isSystemAdmin", false);
        setUpCommonEntriesInExpectedVelocityContext(expectedVelocityContext);

        assertEquals(expectedVelocityContext, velocityContext);
    }

    @Test
    public void testSelectedTabInRequestParameterOverridesTheOneInSession()
    {
        Map<String, Object> expectedVelocityContext = new HashMap<String, Object>();

        session.put(BambooPanelHelper.BAMBOO_PLUGIN_KEY + "." + BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_PLAN_STATUS);
        params.put(BambooPanelHelper.SELECTED_SUB_TAB_KEY, new String[] { BambooPanelHelper.SUB_TAB_BUILD_BY_PLAN });
        subTabs.add("fake");

        bambooPanelHelper.prepareVelocityContext(
                velocityContext,
                bambooPluginModuleKey,
                baseLinkUrl,
                queryString,
                subTabs,
                project
        );

        expectedVelocityContext.put(BambooPanelHelper.SELECTED_SUB_TAB_KEY, BambooPanelHelper.SUB_TAB_BUILD_BY_PLAN);
        expectedVelocityContext.put("availableTabs", subTabs);
        expectedVelocityContext.put("isSystemAdmin", false);
        setUpCommonEntriesInExpectedVelocityContext(expectedVelocityContext);

        assertEquals(expectedVelocityContext, velocityContext);
        assertEquals(BambooPanelHelper.SUB_TAB_BUILD_BY_PLAN, session.get(BambooPanelHelper.BAMBOO_PLUGIN_KEY + "." + BambooPanelHelper.SELECTED_SUB_TAB_KEY));
    }

    private class TestBambooPanelHelper extends BambooPanelHelper
    {
        private TestBambooPanelHelper()
        {
            super(webResourceManager, bambooApplicationLinkManager, permissionManager, jiraAuthenticationContext);
        }
    }
}
