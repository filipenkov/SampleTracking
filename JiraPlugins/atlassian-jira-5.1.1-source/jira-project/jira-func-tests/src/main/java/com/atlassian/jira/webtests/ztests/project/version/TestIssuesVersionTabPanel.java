package com.atlassian.jira.webtests.ztests.project.version;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestIssuesVersionTabPanel extends FuncTestCase
{
    private static final String NEW_VERSION_1 = "New Version 1";
    private static final String NEW_VERSION_4 = "New Version 4";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssuesVersionTabPanel.xml");
    }

    public void testUnresolvedByComponentVisibility() throws Exception
    {
        // should only be displayed if user has permission to view components field
        assertFragmentNotPresent(
                "TestIssuesVersionTabPanel_ComponentsHiddenInFieldScheme.xml",
                "fragunresolvedissuesbycomponent",
                "Unresolved: By Component");
    }

    public void testUnresolvedByPriorityVisibility() throws Exception
    {
        // should only be displayed if user has permission to view priority field
        assertFragmentNotPresent(
                "TestIssuesVersionTabPanel_PriorityHiddenInFieldScheme.xml",
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
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Open");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldstatus", "Open");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByComponentLinkOnNoComponent()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("No Component");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldcomponent", "no components");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByPriorityLinkOnPriority()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("Blocker");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldpriority", "Blocker");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Key descending");
    }

    public void testUnresolvedByPriorityHeaderLinkAndStats()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
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
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");

        // change the issues and reassert the display statistics
        setPriority("HSP-4", "Critical");
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "17%",
                "Critical", "1", "17%",
                "Major", "3", "50%",
                "Trivial", "1", "17%");

        // resolve issues
        resolveIssue("HSP-4", null);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "20%",
                "Major", "3", "60%",
                "Trivial", "1", "20%");

        // reassign fix for version
        scheduleIssueFixVersion("HSP-5", NEW_VERSION_4);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbypriority']//table//td");
        text.assertTextSequence(pathLocator,
                "Blocker", "1", "25%",
                "Major", "3", "75%");
    }

    public void testUnresolvedByComponentHeaderLink()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']//h3"), "Unresolved: By Component");

        // follow the link of the heading
        tester.clickLink("fragunresolvedissuesbycomponent_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Component/s ascending");
    }

    public void testStatusSummaryHeaderLink()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragstatussummary']//h3"), "Status Summary");

        // follow the link of the heading
        tester.clickLink("fragstatussummary_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearchOrder("Status descending");
    }

    public void testStatusSummaryStats() throws Exception
    {
        administration.restoreData("TestIssuesVersionTabPanel_StatusSummary.xml");
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragstatussummary']//table//td");
        text.assertTextSequence(pathLocator,
                "Open", "6", "50%",
                "In Progress", "1", "8%",
                "Reopened", "1", "8%",
                "Resolved", "2", "17%",
                "Closed", "2", "17%");

        // reassign fix for version
        scheduleIssueFixVersion("HSP-5", NEW_VERSION_4);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragstatussummary']//table//td");
        text.assertTextSequence(pathLocator,
                "Open", "5", "45%",
                "In Progress", "1", "9%",
                "Reopened", "1", "9%",
                "Resolved", "2", "18%",
                "Closed", "2", "18%");
    }

    public void testUnresolvedByComponentStats()
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']/div");
        text.assertTextSequence(pathLocator, "6", "No Component");
        text.assertTextNotPresent(pathLocator, "New Component");

        navigation.issue().setComponents("HSP-1", "New Component 1");
        navigation.issue().setComponents("HSP-2", "New Component 2");
        navigation.issue().setComponents("HSP-3", "New Component 3");
        navigation.issue().setComponents("HSP-4", "New Component 1");

        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']/div");
        text.assertTextSequence(pathLocator,
                "2", "New Component 1",
                "1", "New Component 2",
                "1", "New Component 3",
                "2", "No Component");

        // resolve issues
        resolveIssue("HSP-3", null);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']/div");
        text.assertTextSequence(pathLocator,
                "2", "New Component 1",
                "1", "New Component 2",
                "2", "No Component");

        // reassign fix for version
        scheduleIssueFixVersion("HSP-5", NEW_VERSION_4);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbycomponent']/div");
        text.assertTextSequence(pathLocator,
                "2", "New Component 1",
                "1", "New Component 2",
                "1", "No Component");

        // check that the link goes to the correct search request
        tester.clickLinkWithText("New Component 1");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");

        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        // check that the link goes to the correct search request
        tester.clickLinkWithText("New Component 2");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldcomponent", "New Component 2");
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Priority descending");
    }

    public void testUnresolvedByAssignee()
    {
        administration.restoreData("TestIssuesVersionTabPanel_UnresolvedIssuesByAssignee.xml");

        // check that the More link goes to the correct search request
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        tester.clickLink("fragunresolvedissuesbyassignee_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Assignee ascending");

        // check histogram
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
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

        try
        {
            backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
            assignIssue("HSP-1", "Unassigned");
            assignIssue("HSP-2", "Harry Henderson");
            assignIssue("HSP-3", "George Gray");
        }
        finally
        {
            backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        }

        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
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
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
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

        // reassign fix for version
        scheduleIssueFixVersion("HSP-5", NEW_VERSION_4);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        pathLocator = new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//table//td");
        text.assertTextSequence(pathLocator,
                "Charles", "1", "11%",
                "David Developer", "1", "11%",
                "Erik Eagle", "1", "11%",
                "George Gray", "1", "11%",
                "Harry Henderson", "2", "22%",
                "Xanadu", "1", "11%",
                "Unassigned", "2", "22%");

        // check links
        tester.clickLinkWithText("Unassigned");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "Unassigned");
        assertSearchOrder("Priority descending");

        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        tester.clickLinkWithText("Harry Henderson");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearcherField("fieldassignee", "harry");
        assertSearchOrder("Priority descending");

        // remove all issues
        for (int i = 1; i <= 11; i++)
        {
            navigation.issue().deleteIssue("HSP-" + i);
        }

        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragunresolvedissuesbyassignee']//p"), "No issues");
        tester.assertLinkNotPresent("fragunresolvedissuesbyassignee_more");

        // should only be displayed if user has permission to view fix version field
        assertFragmentNotPresent(
                "TestIssuesVersionTabPanel_AssigneeHiddenInFieldScheme.xml",
                "fragunresolvedissuesbyassignee",
                "Unresolved: By Assignee");
    }

    private void assertSearchOrder(final String expected)
    {
        final XPathLocator pathLocator = new XPathLocator(tester, "//*[@id='filter-summary']/div");
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
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//h3"), title);
    }

    private void deleteAllIssuesAndAssertNoIssues(String fragId)
    {
        for (String key : new String[] { "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6" })
        {
            navigation.issue().deleteIssue(key);
        }
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "issues");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='" + fragId + "']//p"), "No issues");
    }

}
