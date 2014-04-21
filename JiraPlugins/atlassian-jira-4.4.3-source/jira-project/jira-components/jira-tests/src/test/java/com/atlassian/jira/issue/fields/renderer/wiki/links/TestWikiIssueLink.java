package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AbstractWikiTestCase;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

/**
 * Tests the creation of a link to a jira issue. This also test the portion of the JiraLinkResolver that delegates
 * to JiraIssueLinks. This also tests the JiraIssueLinkResolver which identifies the
 * issue keys which are turned into links.
 */
public class TestWikiIssueLink extends AbstractWikiTestCase
{
    private static String OPEN_P = "<p>";
    private static String CLOSE_P = "</p>";
    private static String OPEN_ISSUE_LINK = "<p><a href=\"null/browse/TST-1\" title=\"summary\">TST-1</a></p>";
    private static String RESOLVED_ISSUE_LINK = "<a href=\"null/browse/TST-2\" title=\"summary\"><del>TST-2</del></a>";
    private static String OPEN_ISSUE_LINK_NO_TITLE = "<p><a href=\"null/browse/TST-1\">TST-1</a></p>";
    private static String RESOLVED_ISSUE_LINK_NO_TITLE = "<a href=\"null/browse/TST-2\"><del>TST-2</del></a>";

    private Mock mockIssueManager;
    private Mock mockPermissionManager;
    private MockIssue issueOpen;
    private MockIssue issueResolved;

    private IssueManager oldIssueManager;
    private PermissionManager oldPermissionManager;

    @Override
    protected void setUp() throws Exception
    {
        if (is14OrGreater())
        {
            super.setUp();

            UtilsForTestSetup.deleteAllEntities();
            issueOpen = new MockIssue(new Long(1));
            issueOpen.setKey("TST-1");
            issueOpen.setSummary("summary");
            issueOpen.setSecurityLevelId(new Long(1));

            issueResolved = new MockIssue(new Long(2));
            issueResolved.setKey("TST-2");
            issueResolved.setSummary("summary");
            issueResolved.setSecurityLevelId(new Long(1));
            issueResolved.setResolutionObject((Resolution) new Mock(Resolution.class).proxy());

            oldIssueManager = ManagerFactory.getIssueManager();
            oldPermissionManager = ManagerFactory.getPermissionManager();
            mockIssueManager = new Mock(IssueManager.class);
            mockPermissionManager = new Mock(PermissionManager.class);
            ManagerFactory.addService(IssueManager.class, (IssueManager) mockIssueManager.proxy());
            ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

            OPEN_ISSUE_LINK = "<p><a href=\"" + ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL) + "/browse/TST-1\" title=\"summary\">TST-1</a></p>";
            RESOLVED_ISSUE_LINK = "<a href=\"" + ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL) + "/browse/TST-2\" title=\"summary\"><del>TST-2</del></a>";
            OPEN_ISSUE_LINK_NO_TITLE = "<p><a href=\"" + ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL) + "/browse/TST-1\">TST-1</a></p>";
            RESOLVED_ISSUE_LINK_NO_TITLE = "<a href=\"" + ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL) + "/browse/TST-2\"><del>TST-2</del></a>";

        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (is14OrGreater())
        {
            ManagerFactory.addService(IssueManager.class, oldIssueManager);
            ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
            CoreFactory.getGenericDelegator().clearAllCaches();
            issueOpen = null;
            issueResolved = null;
            mockIssueManager = null;
            mockPermissionManager = null;
            super.tearDown();
        }
    }

    public void testOpenIssueLink()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssueObject", P.args(P.eq("TST-1")), issueOpen);
            mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issueOpen), P.IS_NULL), Boolean.TRUE);
            assertEquals(OPEN_ISSUE_LINK, getRenderer().convertWikiToXHtml(getRenderContext(), "TST-1"));
        }
    }

    public void testResolvedIssueLink()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssueObject", P.args(P.eq("TST-2")), issueResolved);
            mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issueResolved), P.IS_NULL), Boolean.TRUE);
            assertEquals(OPEN_P + RESOLVED_ISSUE_LINK + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(), "TST-2"));
        }
    }

    public void testOpenIssueLinkNoPerm()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssueObject", P.args(P.eq("TST-1")), issueOpen);
            mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issueOpen), P.IS_NULL), Boolean.FALSE);
            // JRA-14893: do not hyperlink an inaccessible issue
            assertEquals(OPEN_P + "TST-1" + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(), "TST-1"));
        }
    }

    public void testResolvedIssueLinkNoPerm()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssueObject", P.args(P.eq("TST-2")), issueResolved);
            mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issueResolved), P.IS_NULL), Boolean.FALSE);
            // JRA-14893: do not hyperlink an inaccessible issue
            assertEquals(OPEN_P + "TST-2" + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(), "TST-2"));
        }
    }

    public void testMultipleIssueLinks()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssueObject", P.args(P.eq("TST-2")), issueResolved);
            mockPermissionManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issueResolved), P.IS_NULL), Boolean.TRUE);
            assertEquals(OPEN_P + RESOLVED_ISSUE_LINK + " " + RESOLVED_ISSUE_LINK + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(),
                "TST-2 TST-2"));
        }
    }

    public void testNonExistentIssueLink()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssue", P.args(P.eq("TST-3")), null);
            assertEquals(OPEN_P + "TST-3" + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(), "TST-3"));
        }
    }

    public void testNonExistentIssueLinkInBrackets()
    {
        if (is14OrGreater())
        {
            mockIssueManager.expectAndReturn("getIssue", P.args(P.eq("TST-3")), null);
            assertEquals(OPEN_P + "<span class=\"error\">&#91;TST-3&#93;</span>" + CLOSE_P, getRenderer().convertWikiToXHtml(getRenderContext(),
                "[TST-3]"));
        }
    }
}
