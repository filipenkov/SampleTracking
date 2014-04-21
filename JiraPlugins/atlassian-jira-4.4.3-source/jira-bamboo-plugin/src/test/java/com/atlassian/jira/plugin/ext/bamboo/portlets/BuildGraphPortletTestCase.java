package com.atlassian.jira.plugin.ext.bamboo.portlets;

import java.util.Map;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BuildGraphPortletTestCase
{
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private BambooApplicationLinkManager applicationLinkManager;

    @Mock
    private PortletConfiguration portletConfiguration;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private ApplicationProperties applicationProperties;

    private BuildGraphPortlet buildGraphPortlet;

    @Before
    public void setUp() throws Exception
    {
        buildGraphPortlet = new TestBuildGraphPortlet();
    }

    @Test
    public void testPortletNotUpgraded()
    {
        Map<String, Object> velocityContext = buildGraphPortlet.getVelocityParams(portletConfiguration);
        assertEquals("bamboo.jiraportlet.error.portletNotUpgraded", velocityContext.get("responseHtml"));
    }

    private class TestBuildGraphPortlet extends BuildGraphPortlet
    {
        private TestBuildGraphPortlet()
        {
            super(BuildGraphPortletTestCase.this.jiraAuthenticationContext,
                  BuildGraphPortletTestCase.this.permissionManager,
                  BuildGraphPortletTestCase.this.applicationProperties,
                  BuildGraphPortletTestCase.this.applicationLinkManager,
                  BuildGraphPortletTestCase.this.projectManager
            );
        }

        @Override
        protected String getText(String i18nKey)
        {
            return i18nKey;
        }
    }
}
