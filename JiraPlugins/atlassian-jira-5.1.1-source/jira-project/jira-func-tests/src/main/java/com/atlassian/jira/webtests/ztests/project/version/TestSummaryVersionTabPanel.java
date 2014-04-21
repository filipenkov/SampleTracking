package com.atlassian.jira.webtests.ztests.project.version;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.joda.time.DateTime;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Locale;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestSummaryVersionTabPanel extends FuncTestCase
{
    private static final String NEW_VERSION_1 = "New Version 1";
    private static final String NEW_VERSION_4 = "New Version 4";

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    // Version Description Fragment

    public void testVersionDescription() throws Exception
    {
        // initially, there is only a description on the component, so the fragment will not be hidden
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='fragversiondescription']//h3"), "Description");
        tester.assertTextPresent("Test Version Description 1");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='fragversiondescription']//li"), "Due:");

        // set the description to some HTML
        administration.project().editVersionDetails("HSP", NEW_VERSION_1, null, "A description which should be <b>HTML</b> escaped", null);
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragversiondescription']//h3"), "Description");
        tester.assertTextPresent("A description which should be &lt;b&gt;HTML&lt;/b&gt; escaped");
        tester.assertTextNotPresent("A description which should be <b>HTML</b> escaped");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='fragversiondescription']//li"), "Due:");

        // remove description
        administration.project().editVersionDetails("HSP", NEW_VERSION_1, null, "", null);
        assertFragmentNotPresent("fragversiondescription");

        // add a release date
        administration.project().editVersionDetails("HSP", NEW_VERSION_1, null, null, "30/Oct/06");
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='fragversiondescription']//li"), "Due:", "30/Oct/06");
    }

    // Due Issues Fragment
    public void testDueIssues() throws Exception
    {
        DateTime cal = new DateTime();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy EEEE", Locale.getDefault());

        cal = cal.minusDays(14);
        DateTuple twoWeeksAgo = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(7);
        DateTuple oneWeekAgo = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(6);
        DateTuple yesterday = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(1);
        DateTuple today = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(1);
        DateTuple tomorrow = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(6);
        DateTuple oneWeekLater = new DateTuple(dateFormat.format(cal.toDate()));

        cal = cal.plusDays(7);
        DateTuple twoWeeksLater = new DateTuple(dateFormat.format(cal.toDate()));

        // since there are no issues yet, assert that the fragment is not displayed
        assertFragmentNotPresent("fragdueissues");

        // create some issues with due date relative to today's date
        String issue1 = createIssueAndSetDueDate(twoWeeksAgo, "Bug due two weeks ago", NEW_VERSION_1);
        String issue2 = createIssueAndSetDueDate(oneWeekAgo, "Bug due one week ago", NEW_VERSION_1);
        String issue3 = createIssueAndSetDueDate(yesterday, "Bug due yesterday", NEW_VERSION_1);
        String issue4 = createIssueAndSetDueDate(today, "Bug due today", NEW_VERSION_1);
        String issue5 = createIssueAndSetDueDate(tomorrow, "Bug due tomorrow", NEW_VERSION_1);
        String issue6 = createIssueAndSetDueDate(oneWeekLater, "Bug due one week later", NEW_VERSION_1);
        String issue7 = createIssueAndSetDueDate(twoWeeksLater, "Bug due two weeks later", NEW_VERSION_1);

        // note: issue 8 is in a different version to the one we are concerned with, so it should never show up
        String issueOtherVersion = createIssueAndSetDueDate(twoWeeksAgo, "Bug due two weeks ago", NEW_VERSION_4);

        // inspect the Due Issues fragment: should be the first 3 issues listed
        // * Two Weeks Ago should be DMY
        // * One Week Ago should be "Last (Day Of Week)"
        // * Yesterday should be "Yesterday"
        assertTop3DueIssues(issue1, twoWeeksAgo.dateString, issue2, "Last " + oneWeekAgo.dayOfWeek, issue3, "Yesterday", issue4, issue5, issue6, issue7, issueOtherVersion);

        // make the top 3 have the same due date, then ensure that order is sorted by priority
        setFixForDueDateAndPriority(issue2, null, yesterday.dateString, "Blocker");
        setFixForDueDateAndPriority(issue3, null, yesterday.dateString, "Minor");
        setFixForDueDateAndPriority(issue1, null, yesterday.dateString, "Trivial");
        assertTop3DueIssues(issue2, "Yesterday", issue3, "Yesterday", issue1, "Yesterday", issue4, issue5, issue6, issue7, issueOtherVersion);

        // resolve top 3 issues so that they do not appear in the list
        resolveIssue(issue1);
        resolveIssue(issue2);
        resolveIssue(issue3);

        // top 3 should now be Today, Tomorrow, (Day Of Week)
        assertTop3DueIssues(issue4, "Today", issue5, "Tomorrow", issue6, oneWeekLater.dayOfWeek, issue1, issue2, issue3, issue7, issueOtherVersion);

        // remove due date from Today, it should fall off the list
        setFixForDueDateAndPriority(issue4, null, "", null);

        // top 3 should now be Tomorrow, (Day Of Week), Two Weeks Later (DMY)
        assertTop3DueIssues(issue5, "Tomorrow", issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue1, issue2, issue3, issue4, issueOtherVersion);

        // remove due date from Tomorrow, it should fall off the list, and Today will return (but with no due date)
        setFixForDueDateAndPriority(issue5, null, "", null);
        assertTop3DueIssues(issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue4, "", issue1, issue2, issue3, issue5, issueOtherVersion);

        // set Tomorrow's priority higher and it should replace Today in the list
        setFixForDueDateAndPriority(issue5, null, "", "Blocker");
        assertTop3DueIssues(issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue5, "", issue1, issue2, issue3, issue4, issueOtherVersion);

        // check the More link goes to the correct search request
        tester.clickLink("fragdueissues_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearcherField("fieldresolution", "Unresolved");
        assertSearchOrder("Due Date ascending", "then Priority descending", "then Created ascending");
    }

    public void testRecentlyUpdatedIssues() throws Exception
    {
        // since there are no issues yet, assert that the fragment is not displayed
        assertFragmentNotPresent("fragrecentissues");

        // restore some data with 4 issues
        administration.restoreData("TestRecentlyUpdatedIssuesFragment.xml");

        // top 3 should be HSP-4, 2, 3 (2 comes before 3 because it has higher priority, even though they have the same last updated time)
        assertTop3RecentIssues("HSP-4", "HSP-2", "HSP-3", "HSP-1");

        // update HSP-1 so that it becomes the most recently updated
        setFixForDueDateAndPriority("HSP-1", null, "", "Blocker");
        assertTop3RecentIssues("HSP-1", "HSP-4", "HSP-2", "HSP-3");

        // set HSP-2 to New Version 4 so that it no longer appears in the list
        setFixForDueDateAndPriority("HSP-2", NEW_VERSION_4, "", null);
        assertTop3RecentIssues("HSP-1", "HSP-4", "HSP-3", "HSP-2");

        // check the More link goes to the correct search request
        tester.clickLink("fragrecentissues_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearcherField("fieldfixfor", NEW_VERSION_1);
        assertSearchOrder("Updated descending", "then Priority descending", "then Created ascending");
    }

    private void assertFragmentNotPresent(String fragmentId)
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='"+ fragmentId+ "']/h3");
        final Node[] nodes = pathLocator.getNodes();
        assertTrue(nodes == null || nodes.length == 0);
    }

    private void assertTop3DueIssues(String firstIssue, String firstDueDate, String secondIssue, String secondDueDate, String thirdIssue, String thirdDueDate, String... issuesNotPresent)
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragdueissues']//ul[@class='issues']/li");
        text.assertTextSequence(pathLocator, new String[] {firstIssue, firstDueDate, secondIssue, secondDueDate, thirdIssue, thirdDueDate});
        for (String issue : issuesNotPresent)
        {
            text.assertTextNotPresent(pathLocator, issue);
        }
    }

    private void assertTop3RecentIssues(String firstIssue, String secondIssue, String thirdIssue, String... issuesNotPresent)
    {
        navigation.browseVersionTabPanel("HSP", NEW_VERSION_1, "summary");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragrecentissues']//ul[@class='issues']/li");
        text.assertTextSequence(pathLocator, new String[] {firstIssue, secondIssue, thirdIssue});
        for (String issue : issuesNotPresent)
        {
            text.assertTextNotPresent(pathLocator, issue);
        }
    }

    private String createIssueAndSetDueDate(final DateTuple dateTuple, final String summary, final String component)
    {
        String key = navigation.issue().createIssue("homosapien", "Bug", summary);
        setFixForDueDateAndPriority(key, component, dateTuple.dateString, null);
        return key;
    }

    private void setFixForDueDateAndPriority(final String key, final String fixForVersionName, final String dueDate, final String priority)
    {
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("duedate", dueDate);
        if (priority != null)
        {
            tester.selectOption("priority", priority);
        }
        if (fixForVersionName != null)
        {
            tester.selectOption("fixVersions", fixForVersionName);
        }
        tester.submit();
    }

    private void resolveIssue(final String key)
    {
        navigation.issue().viewIssue(key);
        tester.clickLinkWithText("Resolve Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
    }

    private void assertSearchOrder(final String... expectedSequence)
    {
        final XPathLocator pathLocator = new XPathLocator(tester, "//*[@id='filter-summary']/div");
        text.assertTextSequence(pathLocator, "Sorted by", expectedSequence);
    }

    private void assertSearcherField(final String elementId, final String expectedText)
    {
        text.assertTextPresent(new XPathLocator(tester, "//span[@id='" + elementId + "']"), expectedText);
    }

    private static class DateTuple
    {
        String dateString;
        String dayOfWeek;

        DateTuple(String format)
        {
            String[] parts = format.split(" ");
            this.dateString = parts[0];
            this.dayOfWeek = parts[1];
        }
    }

}
