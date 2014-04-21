package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestIssuesProjectTabPanel extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssuesProjectTabPanel.xml");
    }

    public void testUnresolvedByComponentVisibility() throws Exception
    {
        // should only be displayed if user has permission to view components field
        assertFragmentNotPresent(
                "TestIssuesProjectTabPanel_ComponentsHiddenInFieldScheme.xml",
                "fragunresolvedissuesbycomponent",
                "Unresolved: By Component");
    }

    public void testUnresolvedByPriorityVisibility() throws Exception
    {
        // should only be displayed if user has permission to view priority field
        assertFragmentNotPresent(
                "TestIssuesProjectTabPanel_PriorityHiddenInFieldScheme.xml",
                "fragunresolvedissuesbypriority",
                "Unresolved: By Priority");
    }

    public void testStatusSummaryNoIssues()
    {
        deleteAllIssuesAndAssertNoIssues("fragstatussummary");
    }

    public void testUnresolvedByComponentNoIssues()
    {
        deleteAllIssuesAndAssertNoIssues("fragunresolvedissuesbycomponent");
    }

    public void testUnresolvedByPriorityNoIssues()
    {
        deleteAllIssuesAndAssertNoIssues("fragunresolvedissuesbypriority");
    }

    public void testStatusSummaryLinkOnPriority()
    {
        navigation.browseProjectTabPanel("HSP", "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Open");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldstatus", "Open");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByPriorityLinkOnPriority()
    {
        navigation.browseProjectTabPanel("HSP", "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Blocker");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldpriority", "Blocker");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Key descending");
    }

    public void testUnresolvedByComponentLinkOnNoComponent()
    {
        navigation.browseProjectTabPanel("HSP", "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("No Component");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "no components");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByPriorityHeaderLinkAndStats()
    {
        navigation.browseProjectTabPanel("HSP", "issues");
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
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");

        // change the issues and reassert the display statistics
        setPriority("HSP-4", "Critical");
        navigation.browseProjectTabPanel("HSP", "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "17%",
                "Critical", "1", "17%",
                "Major", "3", "50%",
                "Trivial", "1", "17%");

        // resolve issues
        resolveIssue("HSP-4", null);
        navigation.browseProjectTabPanel("HSP", "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "20%",
                "Major", "3", "60%",
                "Trivial", "1", "20%");
    }

    public void testUnresolvedByComponentHeaderLink()
    {
        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']//h3"), "Unresolved: By Component");

        // follow the link of the heading
        tester.clickLink("fragunresolvedissuesbycomponent_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Component/s ascending");
    }

    public void testStatusSummaryHeaderLink()
    {
        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragstatussummary']//h3"), "Status Summary");

        // follow the link of the heading
        tester.clickLink("fragstatussummary_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearchOrder("Status descending");
    }

    public void testStatusSummaryStats() throws Exception
    {
        administration.restoreData("TestIssuesProjectTabPanel_StatusSummary.xml");
        navigation.browseProjectTabPanel("HSP", "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragstatussummary']//table//td");
        text.assertTextSequence(pathLocator,
                "Open", "6", "50%",
                "In Progress", "1", "8%",
                "Reopened", "1", "8%",
                "Resolved", "2", "17%",
                "Closed", "2", "17%");
    }

    public void testUnresolvedByComponentStats()
    {
        navigation.browseProjectTabPanel("HSP", "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']//div");
        text.assertTextSequence(pathLocator, "6", "No Component");
        text.assertTextNotPresent(pathLocator, "New Component");

        navigation.issue().setComponents("HSP-1", "New Component 1");
        navigation.issue().setComponents("HSP-2", "New Component 2");
        navigation.issue().setComponents("HSP-3", "New Component 3");
        navigation.issue().setComponents("HSP-4", "New Component 1");

        navigation.browseProjectTabPanel("HSP", "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']//div");
        text.assertTextSequence(pathLocator,
                "2", "New Component 1",
                "1", "New Component 2",
                "1", "New Component 3",
                "2", "No Component");

        // resolve issues
        resolveIssue("HSP-3", null);
        navigation.browseProjectTabPanel("HSP", "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']//div");
        text.assertTextSequence(pathLocator,
                "2", "New Component 1",
                "1", "New Component 2",
                "2", "No Component");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("New Component 1");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 1");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");
        
        navigation.browseProjectTabPanel("HSP", "issues");
        // check that the link goes to the correct search request
        tester.clickLinkWithText("New Component 2");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 2");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByAssignee()
    {
        administration.restoreData("TestIssuesProjectTabPanel_UnresolvedIssuesByAssignee.xml");
        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");

        // check that the More link goes to the correct search request
        navigation.browseProjectTabPanel("HSP", "issues");
        tester.clickLink("fragunresolvedissuesbyassignee_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Assignee ascending");

        // check histogram
        navigation.browseProjectTabPanel("HSP", "issues");
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
        assignIssue("HSP-2", "Harry Henderson");
        assignIssue("HSP-3", "George Gray");

        navigation.browseProjectTabPanel("HSP", "issues");
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
        navigation.browseProjectTabPanel("HSP", "issues");
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
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "Unassigned");
        assertSearchOrder("Priority descending");

        navigation.browseProjectTabPanel("HSP", "issues");
        tester.clickLinkWithText("Harry Henderson");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "harry");
        assertSearchOrder("Priority descending");

        // remove all issues
        for (int i = 1; i <= 11; i++)
        {
            navigation.issue().deleteIssue("HSP-" + i);
        }

        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//p"), "No issues");
        tester.assertLinkNotPresent("fragunresolvedissuesbyassignee_more");

        // should only be displayed if user has permission to view fix version field
        assertFragmentNotPresent(
                "TestIssuesProjectTabPanel_AssigneeHiddenInFieldScheme.xml",
                "fragunresolvedissuesbyassignee",
                "Unresolved: By Assignee");
    }

    public void testUnresolvedByVersion()
    {
        administration.restoreData("TestIssuesProjectTabPanel_UnresolvedIssuesByVersion.xml");

        // check that the More link goes to the correct search request
        navigation.browseProjectTabPanel("HSP", "issues");
        tester.clickLink("fragunresolvedissuesbyfixversion_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Fix Version/s ascending");

        // check histogram
        navigation.browseProjectTabPanel("HSP", "issues");
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

        navigation.browseProjectTabPanel("HSP", "issues");
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

        navigation.browseProjectTabPanel("HSP", "issues");
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
        navigation.browseProjectTabPanel("HSP", "issues");
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
        navigation.browseProjectTabPanel("HSP", "issues");
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
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldfixfor", "no versions");
        assertSearchOrder("Priority descending");

        navigation.browseProjectTabPanel("HSP", "issues");
        tester.clickLinkWithText("New Version A");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldfixfor", "New Version A");
        assertSearchOrder("Priority descending");

        // remove all issues
        for (int i = 1; i <= 12; i++)
        {
            navigation.issue().deleteIssue("HSP-" + i);
        }

        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyfixversion']//p"), "No issues");
        tester.assertLinkNotPresent("fragunresolvedissuesbyfixversion_more");

        // should only be displayed if user has permission to view fix version field
        assertFragmentNotPresent(
                "TestIssuesProjectTabPanel_FixVersionHiddenInFieldScheme.xml",
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
        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//h3"), title);
    }

    private void deleteAllIssuesAndAssertNoIssues(String fragId)
    {
        for (String key : new String[] { "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6" })
        {
            navigation.issue().deleteIssue(key);
        }
        navigation.browseProjectTabPanel("HSP", "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//p"), "No issues");
    }

}
