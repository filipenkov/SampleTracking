package com.atlassian.jira.webtests.ztests.project.component;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestIssuesComponentTabPanel extends FuncTestCase
{
    private static final String NEW_COMPONENT_1 = "New Component 1";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssuesComponentTabPanel.xml");
    }

    public void testUnresolvedByPriorityVisibility() throws Exception
    {
        // should only be displayed if user has permission to view priority field
        assertFragmentNotPresent(
                "TestIssuesComponentTabPanel_PriorityHiddenInFieldScheme.xml",
                "fragunresolvedissuesbypriority",
                "Unresolved: By Priority");
    }

    public void testStatusSummaryNoIssues()
    {
        deleteAllIssuesAndAssertNoIssues("fragstatussummary");
    }

    public void testUnresolvedByPriorityNoIssues()
    {
        deleteAllIssuesAndAssertNoIssues("fragunresolvedissuesbypriority");
    }

    public void testStatusSummaryLinkOnPriority()
    {
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Open");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldstatus", "Open");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByPriorityLinkOnPriority()
    {
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Blocker");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldpriority", "Blocker");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Key descending");
    }

    public void testUnresolvedByPriorityHeaderLinkAndStats()
    {
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//h3"), "Unresolved: By Priority");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "17%",
                "Major", "3", "50%",
                "Minor", "1", "17%",
                "Trivial", "1", "17%");

        // follow the link of the heading
        tester.clickLink("fragunresolvedissuesbypriority_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");

        // change the issues and reassert the display statistics
        setPriority("HSP-4", "Critical");
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "17%",
                "Critical", "1", "17%",
                "Major", "3", "50%",
                "Trivial", "1", "17%");

        // resolve issues
        resolveIssue("HSP-4", null);
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "20%",
                "Major", "3", "60%",
                "Trivial", "1", "20%");
    }

    public void testStatusSummaryHeaderLink()
    {
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragstatussummary']//h3"), "Status Summary");

        // follow the link of the heading
        tester.clickLink("fragstatussummary_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearchOrder("Status descending");
    }

    public void testStatusSummaryStats() throws Exception
    {
        administration.restoreData("TestIssuesComponentTabPanel_StatusSummary.xml");
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragstatussummary']//table//td");
        text.assertTextSequence(pathLocator,
                "Open", "6", "50%",
                "In Progress", "1", "8%",
                "Reopened", "1", "8%",
                "Resolved", "2", "17%",
                "Closed", "2", "17%");
    }

    public void testUnresolvedByAssignee()
    {
        administration.restoreData("TestIssuesComponentTabPanel_UnresolvedIssuesByAssignee.xml");

        // check that the More link goes to the correct search request
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        tester.clickLink("fragunresolvedissuesbyassignee_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Assignee ascending");

        // check histogram
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//table//td");
        text.assertTextSequence(pathLocator,
                ADMIN_FULLNAME, "1", "9%",
                "Big Boo", "1", "9%",
                "Charles", "1", "9%",
                "David Developer", "1", "9%",
                "Erik Eagle", "1", "9%",
                FRED_FULLNAME, "1", "9%",
                "George Gray", "1", "9%",
                "Harry Henderson", "1", "9%",
                "Sleepy ZZZ", "1", "9%",
                "Xanadu", "1", "9%",
                "Unassigned", "1", "9%");

        assignIssue("HSP-1", "Unassigned");
        try
        {
            backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
            assignIssue("HSP-2", "Harry Henderson");
            assignIssue("HSP-3", "George Gray");
        }
        finally
        {
            backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        }

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//table//td");
        text.assertTextSequence(pathLocator,
                "Charles", "1", "9%",
                "David Developer", "1", "9%",
                "Erik Eagle", "1", "9%",
                "George Gray", "2", "18%",
                "Harry Henderson", "2", "18%",
                "Sleepy ZZZ", "1", "9%",
                "Xanadu", "1", "9%",
                "Unassigned", "2", "18%");

        // resolve issues
        resolveIssue("HSP-3", null);
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//table//td");
        text.assertTextSequence(pathLocator,
                "Charles", "1", "10%",
                "David Developer", "1", "10%",
                "Erik Eagle", "1", "10%",
                "George Gray", "1", "10%",
                "Harry Henderson", "2", "20%",
                "Sleepy ZZZ", "1", "10%",
                "Xanadu", "1", "10%",
                "Unassigned", "2", "20%");

        // check links
        tester.clickLinkWithText("Unassigned");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "Unassigned");
        assertSearchOrder("Priority descending");

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        tester.clickLinkWithText("Harry Henderson");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "harry");
        assertSearchOrder("Priority descending");

        // remove all issues
        for (int i = 1; i <= 11; i++)
        {
            navigation.issue().deleteIssue("HSP-" + i);
        }

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//p"), "No issues");
        tester.assertLinkNotPresent("fragunresolvedissuesbyassignee_more");

        // should only be displayed if user has permission to view fix version field
        assertFragmentNotPresent(
                "TestIssuesComponentTabPanel_AssigneeHiddenInFieldScheme.xml",
                "fragunresolvedissuesbyassignee",
                "Unresolved: By Assignee");
    }

    public void testUnresolvedByVersion()
    {
        administration.restoreData("TestIssuesComponentTabPanel_UnresolvedIssuesByVersion.xml");

        // check that the More link goes to the correct search request
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        tester.clickLink("fragunresolvedissuesbyfixversion_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Fix Version/s ascending");

        // check histogram
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//div");
        text.assertTextSequence(pathLocator,
                "1", "New Version 1",
                "1", "New Version 2",
                "1", "New Version 3",
                "1", "New Version 4",
                "1", "New Version 5",
                "1", "New Version 6",
                "1", "New Version 7",
                "1", "New Version 8",
                "1", "New Version 9",
                "1", "New Version A",
                "1", "New Version B",
                "1", "Unscheduled");

        scheduleIssueFixVersion("HSP-1", "Unknown");
        scheduleIssueFixVersion("HSP-2", "New Version 9");
        scheduleIssueFixVersion("HSP-3", "New Version A");
        scheduleIssueFixVersion("HSP-4", "New Version B");

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//div");
        text.assertTextSequence(pathLocator,
                "1", "New Version 5",
                "1", "New Version 6",
                "1", "New Version 7",
                "1", "New Version 8",
                "2", "New Version 9",
                "2", "New Version A",
                "2", "New Version B",
                "2", "Unscheduled");

        // resolve issues
        resolveIssue("HSP-5", null);
        resolveIssue("HSP-6", null);

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//div");
        text.assertTextSequence(pathLocator,
                "1", "New Version 7",
                "1", "New Version 8",
                "2", "New Version 9",
                "2", "New Version A",
                "2", "New Version B",
                "2", "Unscheduled");
        text.assertTextNotPresent(pathLocator, "New Version 5");
        text.assertTextNotPresent(pathLocator, "New Version 6");

        // release version - shouldn't change
        administration.project().releaseVersion("HSP", "New Version 7", null);
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//div");
        text.assertTextSequence(pathLocator,
                "1", "New Version 7",
                "1", "New Version 8",
                "2", "New Version 9",
                "2", "New Version A",
                "2", "New Version B",
                "2", "Unscheduled");

        // archive version - shouldn't change
        administration.project().archiveVersion("HSP", "New Version 8");
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//div");
        text.assertTextSequence(pathLocator,
                "1", "New Version 7",
                "1", "New Version 8",
                "2", "New Version 9",
                "2", "New Version A",
                "2", "New Version B",
                "2", "Unscheduled");

        // check links
        tester.clickLinkWithText("Unscheduled");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldfixfor", "no versions");
        assertSearchOrder("Priority descending");

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        tester.clickLinkWithText("New Version A");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldfixfor", "New Version A");
        assertSearchOrder("Priority descending");

        // remove all issues
        for (int i = 1; i <= 12; i++)
        {
            navigation.issue().deleteIssue("HSP-" + i);
        }

        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//p"), "No issues");
        tester.assertLinkNotPresent("fragunresolvedissuesbyfixversion_more");

        // should only be displayed if user has permission to view fix version field
        assertFragmentNotPresent(
                "TestIssuesComponentTabPanel_FixVersionHiddenInFieldScheme.xml",
                "fragunresolvedissuesbyfixversion",
                "Unresolved: By Version");
    }

    private void assertSearchOrder(final String expected)
    {
        final XPathLocator pathLocator = new XPathLocator(tester, "//*[@id='filter-summary']//div");
        text.assertTextSequence(pathLocator, "Sorted by", expected);
    }

    private void assignIssue(final String issueKey, final String assigneeName)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.setWorkingForm("assign-issue");
        tester.selectOption("assignee", assigneeName);
        tester.submit("Assign");
    }

    private void scheduleIssueFixVersion(final String issueKey, final String fixVersion)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.selectOption("fixVersions", fixVersion);
        tester.submit("Update");
    }

    private void resolveIssue(final String issueKey, final String fixVersion)
    {
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        if (fixVersion != null)
        {
            tester.selectOption("fixVersions", fixVersion);
        }
        tester.submit("Transition");
    }

    private void assertSearcherField(final String elementId, final String expectedText)
    {
        text.assertTextPresent(new XPathLocator(tester, "//span[@id='" + elementId + "']"), expectedText);
    }

    private void setPriority(final String key, final String priority)
    {
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.selectOption("priority", priority);
        tester.submit();
    }

    private void assertFragmentNotPresent(String xmlBackup, String fragId, String title)
    {
        administration.restoreData(xmlBackup);
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//h3"), title);
    }

    private void deleteAllIssuesAndAssertNoIssues(String fragId)
    {
        for (String key : new String[] { "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6" })
        {
            navigation.issue().deleteIssue(key);
        }
        navigation.browseComponentTabPanel("HSP", NEW_COMPONENT_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//p"), "No issues");
    }

}
