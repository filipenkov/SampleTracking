package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.LinkGroup;

import java.util.Arrays;
import java.util.List;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceOpsbar extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    public void testLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        assertEquals(3, opsbarGroup.getGroups().size());
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(0), "edit-issue");
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(1), "assign-issue", "comment-issue");
        //On Bamboo the reference plugin adds some issue operations and workflow transitions to the opsbar
        if(isReferencePluginEnabled())
        {
            assertGroupContainsLinkIds(opsbarGroup.getGroups().get(2), "reference-transition-item", "action_id_4");
        }
        else
        {
            assertGroupContainsLinkIds(opsbarGroup.getGroups().get(2), "action_id_4", "action_id_5");
        }

        final List<LinkGroup> workflowGroups = opsbarGroup.getGroups().get(2).getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertEquals(1, workflowGroups.get(0).getGroups().size());
        //On Bamboo the reference plugin adds some issue operations and workflow transitions to the opsbar
        if(isReferencePluginEnabled())
        {
            assertGroupContainsLinkIds(workflowGroups.get(0).getGroups().get(0), "action_id_5", "action_id_2");
        }
        else
        {
            assertGroupContainsLinkIds(workflowGroups.get(0).getGroups().get(0), "action_id_2");
        }

        assertToolsGroup(linkGroups.get(1));
    }

    private boolean isReferencePluginEnabled()
    {
        return administration.plugins().referencePlugin().isInstalled() && administration.plugins().referencePlugin().isEnabled();
    }

    public void testCanEditWhenNotLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");
        navigation.logout();

        Issue issue = issueClient.anonymous().get("ANONED-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        assertEquals(3, opsbarGroup.getGroups().size());
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(0), "edit-issue");
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(1), "edit-labels");
        assertNoLinks(opsbarGroup.getGroups().get(2));

        final List<LinkGroup> workflowGroups = opsbarGroup.getGroups().get(2).getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertNoGroups(workflowGroups.get(0));

        assertToolsGroup(linkGroups.get(1));
    }

    public void testCannotEditWhenNotLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");
        navigation.logout();

        Issue issue = issueClient.anonymous().get("ANON-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        assertEquals(3, opsbarGroup.getGroups().size());
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(0), "ops-login-lnk");
        assertNoLinks(opsbarGroup.getGroups().get(1));
        assertNoLinks(opsbarGroup.getGroups().get(2));

        final List<LinkGroup> workflowGroups = opsbarGroup.getGroups().get(2).getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertNoGroups(workflowGroups.get(0));

        assertToolsGroup(linkGroups.get(1));
    }

    private static void assertToolsGroup(final LinkGroup toolsGroup)
    {
        assertEquals("jira.issue.tools", toolsGroup.getId());
        assertNoLinks(toolsGroup);
        assertGroupContainsLinkLabels(getOnlyGroup(toolsGroup), "XML", "Word", "Printable");
    }

    private static LinkGroup getOnlyGroup(final LinkGroup group)
    {
        assertEquals(1, group.getGroups().size());
        return group.getGroups().get(0);
    }

    private static void assertGroupContainsLinkIds(final LinkGroup group, final String... linkIds)
    {
        for (int i = 0; i < linkIds.length; i++)
        {
            assertEquals("Link not found. Expected these link ids [" + Arrays.toString(linkIds) + "], but got [" + group.getLinks() + "]",
                    linkIds[i], group.getLinks().get(i).id);
        }
    }

    private static void assertGroupContainsLinkLabels(final LinkGroup group, final String... linkLabels)
    {
        for (int i = 0; i < linkLabels.length; i++)
        {
            assertEquals("Link not found. Expected these link labels [" + Arrays.toString(linkLabels) + "], but got [" + group.getLinks() + "]",
                    linkLabels[i], group.getLinks().get(i).label);
        }
    }

    private static void assertNoLinks(final LinkGroup group)
    {
        assertTrue(group.getLinks().isEmpty());
    }

    private static void assertNoGroups(final LinkGroup group)
    {
        assertTrue(group.getGroups().isEmpty());
    }
}
