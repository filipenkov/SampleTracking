package com.atlassian.jira.webtests.ztests.charts;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.CHARTING })
public class TestCharting extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testCreatedVsResolvedReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:createdvsresolved-report");
        tester.assertTextPresent("Report: Created vs Resolved Issues Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //now lets create an issue and do the report again.
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");

        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:createdvsresolved-report");
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", "Created vs Resolved Issues Report");
        text.assertTextSequence(locator, "Project:", "homosapien");
        text.assertTextSequence(locator, "This chart shows the number of issues", "created",
                "vs the number of issues", "resolved", "in the last", "30", "days.");
        tester.assertTextPresent("Data Table");

        final TableLocator tableLocator = new TableLocator(tester, "createdvsresolved-report-datatable");
        text.assertTextPresent(tableLocator, "Period");
        text.assertTextPresent(tableLocator, "Created");
        text.assertTextPresent(tableLocator, "Resolved");
    }

    public void testResolutionTimeReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:resolutiontime-report");
        tester.assertTextPresent("Report: Resolution Time Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //can't actually create some data here since the resolution time will be too short.  The TestChartingData
        // test takes care of the data tests though.
    }

    public void testPieReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:pie-report");
        tester.assertTextPresent("Report: Pie Chart Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.selectOption("statistictype", "Issue Type");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", "Pie Chart Report");
        text.assertTextSequence(locator, "Project:", "homosapien", "(Issue Type)");
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //now lets create some issues
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");

        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:pie-report");
        tester.assertTextPresent("Report: Pie Chart Report");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.selectOption("statistictype", "Issue Type");
        tester.submit("Next");

        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", "Pie Chart Report");
        text.assertTextSequence(locator, "Project:", "homosapien", "(Issue Type)");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 0, 1), "Issues");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 0, 2), "%");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 0), "Improvement");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 1), "1");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 2), "50%");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 0), "Bug");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 1), "1");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 2), "50%");
    }

    public void testAverageAgeReport()
    {
        final String name = "Average Age Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:averageage-report", name, "There are no matching issues to report on.");
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", name);
        text.assertTextSequence(locator, "Project:", "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 1), "Issues Unresolved");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 2), "Total Age");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 3), "Avg. Age");
    }

    public void testRecentlyCreatedReport()
    {
        final String name = "Recently Created Issues Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:recentlycreated-report", name, null);
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", name);
        text.assertTextSequence(locator, "Project:", "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 1), "Created Issues (Unresolved)");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 2), "Created Issues (Resolved)");
    }

    public void testTimeSinceReport()
    {
        final String name = "Time Since Issues Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:timesince-report", name, null);
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", name);
        text.assertTextSequence(locator, "Project:", "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "timesince-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "timesince-report-datatable", 0, 1), "Created");
    }

    private void assertReportValidation(String url, String reportName, String noIssuesMsg)
    {
        tester.gotoPage(url);
        tester.assertTextPresent("Report: " + reportName);

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Report:", reportName);
        text.assertTextSequence(locator, "Project:", "homosapien");
        if (noIssuesMsg != null)
        {
            text.assertTextPresent(locator, noIssuesMsg);
        }

        //now lets create some issues
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");

        tester.gotoPage(url);
        tester.assertTextPresent("Report: " + reportName);

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");
    }

    public void testRedirect()
    {
        //Created vs resolved chart
        tester.gotoPage("/secure/RunPortlet.jspa?cumulative=true&portletKey=com.atlassian.jira.ext.charting:createdvsresolved&daysprevious=30&periodName=daily&projectOrFilterId=project-10000&showUnresolvedTrend=false&versionLabels=major");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?cumulative=true&portletKey=com.atlassian.jira.plugin.system.portlets:createdvsresolved&daysprevious=30&periodName=daily&projectOrFilterId=project-10000&showUnresolvedTrend=false&versionLabels=major"));

        text.assertTextSequence(new WebPageLocator(tester), "Created vs Resolved Issues", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        //ResolutionTime
        tester.gotoPage("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.ext.charting:resolutiontime&projectOrFilterId=project-10000&periodName=daily");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.plugin.system.portlets:resolutiontime&projectOrFilterId=project-10000&periodName=daily"));

        text.assertTextSequence(new WebPageLocator(tester), "Resolution Time:", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        //Piechart
        tester.gotoPage("/secure/RunPortlet.jspa?statistictype=issuetype&portletKey=com.atlassian.jira.ext.charting:singlefieldpie&projectOrFilterId=project-10000");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?statistictype=issuetype&portletKey=com.atlassian.jira.plugin.system.portlets:pie&projectOrFilterId=project-10000"));

        text.assertTextSequence(new WebPageLocator(tester), "Pie Chart:", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        //AverageAge
        tester.gotoPage("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.ext.charting:averageage&projectOrFilterId=project-10000&periodName=daily");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.plugin.system.portlets:averageage&projectOrFilterId=project-10000&periodName=daily"));

        text.assertTextSequence(new WebPageLocator(tester), "Average Age:", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        //Recently Created
        tester.gotoPage("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.ext.charting:recentlycreated&projectOrFilterId=project-10000&periodName=daily");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.plugin.system.portlets:recentlycreated&projectOrFilterId=project-10000&periodName=daily"));

        text.assertTextSequence(new WebPageLocator(tester), "Recently Created Issues:", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        //Time Since
        tester.gotoPage("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.ext.charting:timesince&projectOrFilterId=project-10000&periodName=daily&dateField=created&cumulative=true");
        assertTrue(tester.getDialog().getResponse().getURL().toString().endsWith("/secure/RunPortlet.jspa?daysprevious=30&portletKey=com.atlassian.jira.plugin.system.portlets:timesince&projectOrFilterId=project-10000&periodName=daily&dateField=created&cumulative=true"));

        text.assertTextSequence(new WebPageLocator(tester), "Time Since Issues:", "homosapien", "There were no matching issues found. Please make sure that you have selected a valid project or filter.");

        tester.gotoPage("/secure/RunPortlet.jspa?daysprevious=30&portletKey=blub&projectOrFilterId=project-10000&periodName=daily&dateField=created&cumulative=true");
        text.assertTextSequence(new WebPageLocator(tester), "A gadget with the key blub does not exist.");
    }
}
