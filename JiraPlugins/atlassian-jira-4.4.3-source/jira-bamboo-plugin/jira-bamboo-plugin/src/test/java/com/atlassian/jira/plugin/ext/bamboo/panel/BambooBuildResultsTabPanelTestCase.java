package com.atlassian.jira.plugin.ext.bamboo.panel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import com.opensymphony.user.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BambooBuildResultsTabPanelTestCase
{
    @Mock
    private BambooPanelHelper bambooPanelHelper;

    @Mock
    private BambooApplicationLinkManager bambooApplicationLinkManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private Issue issue;

    private BambooBuildResultsTabPanel bambooBuildResultsTabPanel;

    @Before
    public void setUp() throws Exception
    {
        bambooBuildResultsTabPanel = new TestBambooBuildResultsTabPanel();
    }

    @Test
    public void testGetActionsReturnsBambooBuildResultsAction()
    {
        String issueKey = "TST-1";

        when(issue.getKey()).thenReturn(issueKey);

        List actions = bambooBuildResultsTabPanel.getActions(issue, null);

        assertEquals(1, actions.size());
        assertEquals(BambooBuildResultsAction.class, actions.get(0).getClass());

        BambooBuildResultsAction bambooBuildResultsAction = (BambooBuildResultsAction) actions.get(0);

        Map<String, Object> actualVelocityContext = new HashMap<String, Object>();
        Map<String, Object> expectedVelocityContext = new HashMap<String, Object>();

        bambooBuildResultsAction.populateVelocityParams(actualVelocityContext);

        assertFalse(bambooBuildResultsAction.isDisplayActionAllTab());

        expectedVelocityContext.put("issueKey", issueKey);

        assertEquals(expectedVelocityContext, actualVelocityContext);

        verify(bambooPanelHelper).prepareVelocityContext(
                actualVelocityContext,
                "bamboo-build-results-tabpanel",
                "/browse/" + issueKey +
                     "?selected=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":bamboo-build-results-tabpanel",
                "issueKey=" + issueKey,
                null,
                issue.getProjectObject()
        );

    }

    @Test
    public void testPanelNotShownIfUserDoesNotHaveViewVersionControlPermission()
    {
        assertFalse(bambooBuildResultsTabPanel.showPanel(issue, null));
    }

    @Test
    public void testPanelNotShownIfBambooNotConfigured()
    {
        when(
                permissionManager.hasPermission(
                        eq(Permissions.VIEW_VERSION_CONTROL),
                        eq(issue),
                        (User) anyObject()
                )
        ).thenReturn(true);

        assertFalse(bambooBuildResultsTabPanel.showPanel(issue, null));
    }

    @Test
    public void testPanelShownIfBambooConfiguredAndUserHasViewVcPermission()
    {
        when(
                permissionManager.hasPermission(
                        eq(Permissions.VIEW_VERSION_CONTROL),
                        eq(issue),
                        (User) anyObject()
                )
        ).thenReturn(true);
        when(bambooApplicationLinkManager.hasApplicationLinks()).thenReturn(true);

        assertTrue(bambooBuildResultsTabPanel.showPanel(issue, null));
    }

    private class TestBambooBuildResultsTabPanel extends BambooBuildResultsTabPanel
    {
        private TestBambooBuildResultsTabPanel()
        {
            super(permissionManager, bambooPanelHelper, bambooApplicationLinkManager);
        }
    }
}
