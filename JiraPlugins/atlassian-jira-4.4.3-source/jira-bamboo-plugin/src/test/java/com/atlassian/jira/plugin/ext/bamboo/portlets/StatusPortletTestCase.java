package com.atlassian.jira.plugin.ext.bamboo.portlets;

import java.util.Map;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StatusPortletTestCase
{
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private BambooApplicationLinkManager applicationLinkManager;

    @Mock
    private HttpClient httpClient;

    @Mock
    private GetMethod getMethod;

    @Mock
    private PortletConfiguration portletConfiguration;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ProjectManager projectManager;

    private StatusPortlet statusPortlet;

    @Before
    public void setUp() throws Exception
    {
        statusPortlet = new TestStatusPortlet();
    }

    @Test
    public void testPortletNotUpgraded()
    {
        Map<String, Object> velocityContext = statusPortlet.getVelocityParams(portletConfiguration);
        assertEquals("bamboo.jiraportlet.error.portletNotUpgraded", velocityContext.get("responseHtml"));
    }

    private class TestStatusPortlet extends StatusPortlet
    {
        private TestStatusPortlet()
        {
            super(
                    StatusPortletTestCase.this.jiraAuthenticationContext,
                    StatusPortletTestCase.this.permissionManager,
                    StatusPortletTestCase.this.applicationProperties,
                    StatusPortletTestCase.this.applicationLinkManager,
                    StatusPortletTestCase.this.projectManager
                    );
        }

        @Override
        protected String getText(String i18nKey)
        {
            return i18nKey;
        }
    }
}
