package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.joda.time.DateTime;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Locale;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestSummaryProjectTabPanel extends FuncTestCase
{
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }


    public void testProjectAdminLink()
    {
        navigation.browseProject("HSP");
        assertions.assertNodeByIdExists("project-admin-link");

        navigation.login("fred");
        navigation.browseProject("HSP");
        assertions.assertNodeByIdDoesNotExist("project-admin-link");

        navigation.login("admin");
    }

    // Project Description Fragment

    public void testProjectDescription() throws Exception
    {
        navigation.browseProject("HSP");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragprojectdescription']//p"), "project for homosapiens");

        // remove description
        navigation.gotoAdminSection("view_projects");
        tester.clickLinkWithText("Edit", 0);
        tester.setWorkingForm("project-edit");
        tester.setFormElement("description", "");
        tester.submit();

        // recheck description
        navigation.browseProject("HSP");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='fragprojectdescription']//p"), "project for homosapiens");

        // enter HTML description
        navigation.gotoAdminSection("view_projects");
        tester.clickLinkWithText("Edit", 0);
        tester.setWorkingForm("project-edit");
        tester.setFormElement("description", "project <b>for</b> homosapiens");
        tester.submit();

        // check that description wasn't HTML encoded (HTML is valid in description)
        navigation.browseProject("HSP");
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='fragprojectdescription']//p").getHTML(), "project <b>for</b> homosapiens");
        text.assertTextNotPresent(new XPathLocator(tester, "//div[@id='fragprojectdescription']//p").getHTML(), "project &lt;b&gt;for&lt;/b&gt; homosapiens");
    }

    public void testProjectUrl() throws Exception
    {
        // no URL in restored data
        navigation.browseProject("HSP");
        assertions.assertNodeDoesNotHaveText(new CssLocator(tester, "#fragprojectdescription .mod-content"), "URL:");

        // add URL
        navigation.gotoAdminSection("view_projects");
        tester.clickLinkWithText("Edit", 0);
        tester.setWorkingForm("project-edit");
        tester.setFormElement("url", "http://www.homosapien.com");
        tester.submit();

        // recheck url
        navigation.browseProject("HSP");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragprojectdescription .mod-content"), "URL:");
        assertions.assertNodeHasText(new CssLocator(tester, "#pd-url"), "http://www.homosapien.com");
    }

    public void testProjectKey() throws Exception
    {
        navigation.browseProject("HSP");
        text.assertTextSequence(new CssLocator(tester, "#fragprojectdescription .mod-content .item-details"), "Key:", "HSP");
    }

    public void testProjectLead() throws Exception
    {
        navigation.browseProject("HSP");
        assertions.assertProfileLinkPresent("project_summary_admin", ADMIN_FULLNAME);

        // use data with project lead with no full name
        administration.restoreData("TestProjectLeadWithNoFullName.xml");
        navigation.browseProject("MKY");
        tester.assertLinkPresent("project_summary_fred");
        tester.assertLinkNotPresentWithText(FRED_FULLNAME);

        // use data with a bad project lead (adminXXX)
        administration.restoreData("TestNonExistantLead.xml");
        navigation.browseProject("HSP");
        tester.assertLinkNotPresent("project_summary_adminXXX");
        tester.assertLinkNotPresentWithText("adminXXX");
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
        String issue1 = createIssueAndSetDueDate("homosapien", twoWeeksAgo, "Bug due two weeks ago");
        String issue2 = createIssueAndSetDueDate("homosapien", oneWeekAgo, "Bug due one week ago");
        String issue3 = createIssueAndSetDueDate("homosapien", yesterday, "Bug due yesterday");
        String issue4 = createIssueAndSetDueDate("homosapien", today, "Bug due today");
        String issue5 = createIssueAndSetDueDate("homosapien", tomorrow, "Bug due tomorrow");
        String issue6 = createIssueAndSetDueDate("homosapien", oneWeekLater, "Bug due one week later");
        String issue7 = createIssueAndSetDueDate("homosapien", twoWeeksLater, "Bug due two weeks later");

        // note: issue 8 is in a different project to the one we are concerned with, so it should never show up
        String issueOtherProject = createIssueAndSetDueDate("monkey", twoWeeksLater, "Bug due two weeks later");

        // inspect the Due Issues fragment: should be the first 3 issues listed
        // * Two Weeks Ago should be DMY
        // * One Week Ago should be "Last (Day Of Week)"
        // * Yesterday should be "Yesterday"
        assertTop3DueIssues(issue1, twoWeeksAgo.dateString, issue2, "Last " + oneWeekAgo.dayOfWeek, issue3, "Yesterday", issue4, issue5, issue6, issue7, issueOtherProject);

        // make the top 3 have the same due date, then ensure that order is sorted by priority
        setDueDateAndPriority(issue2, yesterday.dateString, "Blocker");
        setDueDateAndPriority(issue3, yesterday.dateString, "Minor");
        setDueDateAndPriority(issue1, yesterday.dateString, "Trivial");
        assertTop3DueIssues(issue2, "Yesterday", issue3, "Yesterday", issue1, "Yesterday", issue4, issue5, issue6, issue7, issueOtherProject);

        // resolve top 3 issues so that they do not appear in the list
        resolveIssue(issue1);
        resolveIssue(issue2);
        resolveIssue(issue3);

        // top 3 should now be Today, Tomorrow, (Day Of Week)
        assertTop3DueIssues(issue4, "Today", issue5, "Tomorrow", issue6, oneWeekLater.dayOfWeek, issue1, issue2, issue3, issue7, issueOtherProject);

        // remove due date from Today, it should fall off the list
        setDueDateAndPriority(issue4, "", null);

        // top 3 should now be Tomorrow, (Day Of Week), Two Weeks Later (DMY)
        assertTop3DueIssues(issue5, "Tomorrow", issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue1, issue2, issue3, issue4, issueOtherProject);

        // remove due date from Tomorrow, it should fall off the list, and Today will return (but with no due date)
        setDueDateAndPriority(issue5, "", null);
        assertTop3DueIssues(issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue4, "", issue1, issue2, issue3, issue5, issueOtherProject);

        // set Tomorrow's priority higher and it should replace Today in the list
        setDueDateAndPriority(issue5, "", "Blocker");
        assertTop3DueIssues(issue6, oneWeekLater.dayOfWeek, issue7, twoWeeksLater.dateString, issue5, "", issue1, issue2, issue3, issue4, issueOtherProject);

        // check the More link goes to the correct search request
        tester.clickLink("fragdueissues_more");
        assertSearcherField("fieldpid", "homosapien");
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
        setDueDateAndPriority("HSP-1", "", "Blocker");
        assertTop3RecentIssues("HSP-1", "HSP-4", "HSP-2", "HSP-3");

        // check the More link goes to the correct search request
        tester.clickLink("fragrecentissues_more");
        assertSearcherField("fieldpid", "homosapien");
        assertSearchOrder("Updated descending", "then Priority descending", "then Created ascending");
    }

    public void testDueVersions() throws Exception
    {
        // restore some data with 4 versions
        administration.restoreData("TestSummaryProjectTabPanel_DueVersions.xml");

        // first 3 versions should be present
        assertTop3DueVersions(new String[] { "New Version 1", "New Version 4", "New Version 5" }, "New Version 7");

        // archive New Version 1
        administration.project().archiveVersion("HSP", "New Version 1");

        assertTop3DueVersions(new String[] { "New Version 4", "New Version 5", "New Version 7" }, "New Version 1");

        // release New Version 5
        administration.project().releaseVersion("HSP", "New Version 5", null);

        assertTop3DueVersions(new String[] { "New Version 4", "New Version 7" }, "New Version 1", "New Version 5");

        // release and archive New Version 4
        administration.project().releaseVersion("HSP", "New Version 4", null);
        administration.project().archiveVersion("HSP", "New Version 4");

        assertTop3DueVersions(new String[] { "New Version 7" }, "New Version 1", "New Version 4", "New Version 5");

        // release and archive New Version 7
        administration.project().releaseVersion("HSP", "New Version 7", null);
        administration.project().archiveVersion("HSP", "New Version 7");

        assertFragmentNotPresent("fragdueversions");

        // unrelease New Version 5
        administration.project().unreleaseVersion("HSP", "New Version 5", null);

        assertTop3DueVersions(new String[] { "New Version 5" }, "New Version 1", "New Version 4", "New Version 7");

        // hide versions in all field schemes
        tester.gotoPage("/secure/admin/IssueFieldHide.jspa?hide=8");
        assertFragmentNotPresent("fragdueversions");
    }

    public void testDueVersionsNoVersions() throws Exception
    {
        // remove all versions
        administration.project().deleteVersion("HSP", "New Version 1");
        administration.project().deleteVersion("HSP", "New Version 4");
        assertFragmentNotPresent("fragdueversions");
    }

    public void testCreatedVsResolved()
    {
        navigation.browseProject("HSP");
        tester.assertTextPresent("Issues: 30 Day Summary");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='fragcreatedvsresolved']//div"), "Issues", "0", "created and ", "0", "resolved");

        navigation.issue().createIssue("homosapien", "Bug", "Test summary");

        navigation.browseProject("HSP");
        tester.assertTextPresent("Issues: 30 Day Summary");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='fragcreatedvsresolved']//div"), "Issues", "1", "created and ", "0", "resolved");
    }

    private void assertTop3DueVersions(String[] presentVersions, String... notPresentVersions)
    {
        navigation.browseProject("HSP");
        XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragdueversions' ]//ul[@class='issues']/li");
        for (String version : notPresentVersions)
        {
            text.assertTextNotPresent(pathLocator, version);
        }
        text.assertTextSequence(pathLocator, presentVersions);
    }

    private void assertFragmentNotPresent(String fragmentId)
    {
        navigation.browseProject("HSP");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='" + fragmentId + "']/h3");
        final Node[] nodes = pathLocator.getNodes();
        assertTrue(nodes == null || nodes.length == 0);
    }

    private void assertTop3DueIssues(String firstIssue, String firstDueDate, String secondIssue, String secondDueDate, String thirdIssue, String thirdDueDate, String... issuesNotPresent)
    {
        navigation.browseProject("HSP");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragdueissues']//ul[@class='issues']/li");
        text.assertTextSequence(pathLocator, new String[] { firstIssue, firstDueDate, secondIssue, secondDueDate, thirdIssue, thirdDueDate });
        for (String issue : issuesNotPresent)
        {
            text.assertTextNotPresent(pathLocator, issue);
        }
    }

    private void assertTop3RecentIssues(String firstIssue, String secondIssue, String thirdIssue, String... issuesNotPresent)
    {
        navigation.browseProject("HSP");
        final XPathLocator pathLocator = new XPathLocator(tester, "//div[@id='fragrecentissues']//ul[@class='issues']/li");
        text.assertTextSequence(pathLocator, new String[] { firstIssue, secondIssue, thirdIssue });
        for (String issue : issuesNotPresent)
        {
            text.assertTextNotPresent(pathLocator, issue);
        }
    }

    private String createIssueAndSetDueDate(final String projectName, final DateTuple dateTuple, final String summary)
    {
        String key = navigation.issue().createIssue(projectName, "Bug", summary);
        setDueDateAndPriority(key, dateTuple.dateString, null);
        return key;
    }

    private void setDueDateAndPriority(final String key, final String dueDate, final String priority)
    {
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("duedate", dueDate);
        if (priority != null)
        {
            tester.selectOption("priority", priority);
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
