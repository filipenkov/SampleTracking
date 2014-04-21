package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.ImageCell;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.REPORTS })
public class TestVersionWorkloadReport extends FuncTestCase
{
    private static final String VERSION_WITH_ESTIMATES = "Version with estimates";
    private static final String VERSION_WITH_NO_ESTIMATE = "Version with no estimate";
    private static final String VERSION_WITH_NO_ISSUES = "Version with no issues";
    private static final String NO_ESTIMATE = "No Estimate";
    private static final Object[] TABLE_HEADER_COLUMNS = new Object[]{"Type", "Key", "P", "Summary", "Estimated Time Remaining"};
    private static final Object[] NO_ESTIMATE_TOTAL = new Object[]{"Totals", "-", "-", "-", "-", "-", "0 minutes"};
    private static final String DEVELOPER_FULLNAME = "\"Developer<input>";
    private static final String HTML_SUMMARY = "\"summary<input>";
    private static final ImageCell IMAGE_BUG_CELL = new ImageCell(ISSUE_IMAGE_BUG);
    private static final ImageCell IMAGE_NEWFEATURE_CELL = new ImageCell(ISSUE_IMAGE_NEWFEATURE);
    private static final ImageCell IMAGE_IMPROVEMENT_CELL = new ImageCell(ISSUE_IMAGE_IMPROVEMENT);
    private static final ImageCell IMAGE_SUB_TASK_CELL = new ImageCell(ISSUE_IMAGE_SUB_TASK);
    private static final ImageCell IMAGE_GENERIC_CELL = new ImageCell("/images/icons/genericissue.gif");
    private static final ImageCell IMAGE_BLOCKER_CELL = new ImageCell(PRIORITY_IMAGE_BLOCKER);
    private static final ImageCell IMAGE_CRITICAL_CELL = new ImageCell(PRIORITY_IMAGE_CRITICAL);
    private static final ImageCell IMAGE_MAJOR_CELL = new ImageCell(PRIORITY_IMAGE_MAJOR);
    private static final ImageCell IMAGE_MINOR_CELL = new ImageCell(PRIORITY_IMAGE_MINOR);
    private static final ImageCell IMAGE_TRIVIAL_CELL = new ImageCell(PRIORITY_IMAGE_TRIVIAL);
    private static final String DEV_USERNAME = "dev";
    private static final String IGNORE = null;
    private static final Long PROJECT_HOMOSAP_ID = (long) 10000;
    private static final String GRAND_TOTAL = "Grand total";
    private static final TimeTracking.Format FORMAT_HOURS = TimeTracking.Format.HOURS;
    private static final TimeTracking.Format FORMAT_DAYS = TimeTracking.Format.DAYS;
    private static final TimeTracking.Format FORMAT_PRETTY = TimeTracking.Format.PRETTY;
    private static final String XSS = "\"></a><script>alert(\"danger\")</script>";


    @Override
    public void setUpTest()                                    
    {
        backdoor.restoreData("TestVersionWorkloadReport.xml");
    }

    /**
     * Test the version workload reports validation on the configuration page
     */
    public void testVersionWorkloadReportValidation()
    {
        navigation.gotoPage("/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        tester.selectOption("versionId", "Unreleased Versions");
        tester.submit("Next");
        tester.assertTextPresent("Please select an actual version.");
    }

    public void testVersionWorkLoadReport()
    {
        backdoor.restoreData("TestVersionWorkloadReportFormat.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("6 days, 16 hours");//check total timeremaining
        TimeTracking timeTracking = administration.timeTracking();
        

        timeTracking.switchFormat(FORMAT_HOURS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("160h");//assert total time remaining changed into (6*24 + 16)h

        timeTracking.switchFormat(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("6d 16h");

        timeTracking.switchFormat(FORMAT_PRETTY);
        setFixForVersion("HSP-2", VERSION_NAME_FOUR);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("5 days, 19 hours, 30 minutes"); //total work should be one day less

        timeTracking.switchFormat(FORMAT_HOURS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("139.5h");//(24*5 + 19 + 0.5)h

        timeTracking.switchFormat(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        tester.assertTextPresent("5d 19.5h");

        timeTracking.switchFormat(FORMAT_PRETTY);
        navigation.issue().logWork("HSP-1", "5d");
        navigation.issue().addComment("HSP-1", "the workload should decreased by 5 days");
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        //assert time remaining has been decreased by 5 days
        tester.assertTextNotPresent("5 days, 19 hours, 30 minutes");
        tester.assertTextPresent("19 hours, 30 minutes");

        timeTracking.switchFormat(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_FOUR);
        tester.assertTextPresent("HSP-2");
        tester.assertTextPresent("20.5h");
    }

    /**
     * Test the version workload report with no issues
     *
     * @throws org.xml.sax.SAXException on bad html.
     */
    public void testVersionWorkloadReportNoIssues() throws SAXException
    {
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_NO_ISSUES);
        //check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_NO_ISSUES + ")");

        //check the summary
        WebTable reportSummaryTable = new TableLocator(tester, "report-summary").getTable();

        assertEquals(2, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());

        assertReportSummaryHeader(reportSummaryTable);

        assertions.getTableAssertions().assertTableRowEquals(reportSummaryTable, 1, NO_ESTIMATE_TOTAL);
    }

    /**
     * Test the version workload report with issues but no estimates
     *
     * @throws org.xml.sax.SAXException like wheneva.
     */
    public void testVersionWorkloadReportNoEstimates() throws SAXException
    {
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_NO_ESTIMATE);
        //check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_NO_ESTIMATE + ")");

        //check the summary
        WebTable reportSummaryTable = new TableLocator(tester, "report-summary").getTable();

        assertEquals(3, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());

        assertReportSummaryHeader(reportSummaryTable);
        TableAssertions tableAssertions = assertions.getTableAssertions();
        //check that the admin is listed with no estimates
        tableAssertions.assertTableRowEquals(reportSummaryTable, 1, NO_ESTIMATE_TOTAL); // <tfoot> comes before <tbody> in the markup
        tableAssertions.assertTableRowEquals(reportSummaryTable, 2, new Object[]{ADMIN_FULLNAME, "-", "-", "-", "-", "-", NO_ESTIMATE});

        // Check the individual results for Administrator
        WebTable reportAdminTable = new TableLocator(tester,"report-individual-1").getTable();


        // Quick row/column count
        assertEquals(6, reportAdminTable.getRowCount());
        assertEquals(5, reportAdminTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportAdminTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the total is calculated correctly in the tfoot
        tableAssertions.assertTableCellHasText(reportAdminTable, 1, 3, GRAND_TOTAL);
        tableAssertions.assertTableCellHasText(reportAdminTable, 1, 4, NO_ESTIMATE);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportAdminTable, 2, 0, ISSUE_TYPE_BUG);
        tableAssertions.assertTableRowEquals(reportAdminTable, 3, new Object[]{ISSUE_TYPE_BUG, "HSP-1", IMAGE_CRITICAL_CELL, "Issue with no estimate", NO_ESTIMATE});
        tableAssertions.assertTableRowEquals(reportAdminTable, 4, new Object[]{ISSUE_TYPE_BUG, "HSP-2", IMAGE_TRIVIAL_CELL, "Issue without estimate", NO_ESTIMATE});

    }

    /**
     * Test the version workload report displays correct information
     *
     * @throws org.xml.sax.SAXException on bad html
     */
    public void testVersionWorkloadReportWithEstimates() throws SAXException
    {
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES);

        // Check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_ESTIMATES + ")");

        // Check the summary table
        WebTable reportSummaryTable = new TableLocator(tester, "report-summary").getTable();
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertReportSummaryHeader(reportSummaryTable);
        TableAssertions tableAssertions = assertions.getTableAssertions();

        // Check that the admin, dev and unassignedestimates are correct
        tableAssertions.assertTableRowEquals(reportSummaryTable, 1, new Object[] { "Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "6 days", "6 minutes", "8 weeks, 1 day, 9 hours, 14 minutes" });
        tableAssertions.assertTableRowEquals(reportSummaryTable, 2, new Object[] { DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "6 minutes", "6 days, 6 hours, 6 minutes" });
        tableAssertions.assertTableRowEquals(reportSummaryTable, 3, new Object[] { ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "-", "1 week, 2 days, 3 hours, 9 minutes" });
        tableAssertions.assertTableRowEquals(reportSummaryTable, 4, new Object[] { "Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes" });


        // Check the individual results for Administrator
        WebTable reportAdminTable = new TableLocator(tester,"report-individual-2").getTable();

        // Quick row/column count
        assertEquals(6, reportAdminTable.getRowCount());
        assertEquals(5, reportAdminTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportAdminTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the total is calculated correctly in the tfoot
        tableAssertions.assertTableCellHasText(reportAdminTable, 1, 3, GRAND_TOTAL);
        tableAssertions.assertTableCellHasText(reportAdminTable, 1, 4, "1 week, 2 days, 3 hours, 9 minutes");

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportAdminTable, 2, 0, ISSUE_TYPE_BUG);
        tableAssertions.assertTableRowEquals(reportAdminTable, 3, new Object[]{ISSUE_TYPE_BUG, "HSP-3", IMAGE_BLOCKER_CELL, "bug 1 with estimate", "1 week, 2 days, 3 hours, 4 minutes"});
        tableAssertions.assertTableRowEquals(reportAdminTable, 4, new Object[]{ISSUE_TYPE_BUG, "HSP-4", IMAGE_MAJOR_CELL, "bug 2 with estimate", "5 minutes"});


        // Check the individual results for "Developer<input>
        WebTable reportDevTable = new TableLocator(tester,"report-individual-1").getTable();

        // Quick row/column count
        assertEquals(11, reportDevTable.getRowCount());
        assertEquals(5, reportDevTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportDevTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the total is calculated correctly in the tfoot
        tableAssertions.assertTableCellHasText(reportDevTable, 1, 3, GRAND_TOTAL);
        tableAssertions.assertTableCellHasText(reportDevTable, 1, 4, "6 days, 6 hours, 6 minutes");

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportDevTable, 2, 0, ISSUE_TYPE_BUG);
        tableAssertions.assertTableRowEquals(reportDevTable, 3, new Object[]{ISSUE_TYPE_BUG, "HSP-5", IMAGE_MAJOR_CELL, "new bug with estimate", "6 hours"});
        tableAssertions.assertTableCellHasText(reportDevTable, 4, 3, "Total for Bug");
        tableAssertions.assertTableCellHasText(reportDevTable, 4, 4, "6 hours");

        tableAssertions.assertTableCellHasText(reportDevTable, 5, 0, ISSUE_TYPE_IMPROVEMENT);
        tableAssertions.assertTableRowEquals(reportDevTable, 6, new Object[]{ISSUE_TYPE_IMPROVEMENT, "HSP-6", IMAGE_MINOR_CELL, "improvement with estimate", "6 days"});
        tableAssertions.assertTableCellHasText(reportDevTable, 7, 3, "Total for Improvement");
        tableAssertions.assertTableCellHasText(reportDevTable, 7, 4, "6 days");

        tableAssertions.assertTableCellHasText(reportDevTable, 8, 0, ISSUE_TYPE_SUB_TASK);
        tableAssertions.assertTableRowEquals(reportDevTable, 9, new Object[]{ISSUE_TYPE_SUB_TASK, "HSP-7", IMAGE_CRITICAL_CELL, "subtask issue with estimate", "6 minutes"});
        tableAssertions.assertTableCellHasText(reportDevTable, 10, 3, "Total for Sub-task");
        tableAssertions.assertTableCellHasText(reportDevTable, 10, 4, "6 minutes");

        //check the individual estimate of Unassigned
        WebTable reportUnassignedTable = new TableLocator(tester,"report-individual-3").getTable();

        // Quick row/column count
        assertEquals(6, reportUnassignedTable.getRowCount());
        assertEquals(5, reportUnassignedTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportUnassignedTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the total is calculated correctly in the tfoot
        tableAssertions.assertTableCellHasText(reportUnassignedTable, 1, 3, GRAND_TOTAL);
        tableAssertions.assertTableCellHasText(reportUnassignedTable, 1, 4, "5 weeks, 6 days, 23 hours, 59 minutes");

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportUnassignedTable, 2, 0, ISSUE_TYPE_NEWFEATURE);
        tableAssertions.assertTableRowEquals(reportUnassignedTable, 3, new Object[]{ISSUE_TYPE_NEWFEATURE, "HSP-9", IMAGE_MAJOR_CELL, HTML_SUMMARY, NO_ESTIMATE});
        tableAssertions.assertTableRowEquals(reportUnassignedTable, 4, new Object[]{ISSUE_TYPE_NEWFEATURE, "HSP-8", IMAGE_TRIVIAL_CELL, "unassigned issue with estimate", "5 weeks, 6 days, 23 hours, 59 minutes"});

    }

    public void testVersionIsEncoded()
    {
        backdoor.restoreData("TestVersionAndComponentsWithHTMLNames.xml");
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, "\"version<input >");
        text.assertTextPresent("&quot;version&lt;input &gt;");
        text.assertTextNotPresent("\"version<input >");
    }

    public void test_TT_AllSubtasks_DisplayUnestimated() throws SAXException
    {
        test_TT_AllSubtasks(true);
    }

    public void test_TT_AllSubtasks_NoDisplayUnestimated() throws SAXException
    {
        test_TT_AllSubtasks(false);
    }

    private void test_TT_AllSubtasks(boolean displayUnestimated) throws SAXException
    {
        // has time tracking and subtasks in the data
        backdoor.restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ALL_VERSION_SUBTASKS, displayUnestimated);

        // Check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_ESTIMATES + ")");

        // check that a resolved sub-task is not listed
        text.assertTextNotPresent("HSP-21");
        text.assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // Check the individual results for Administrator
        WebTable reportAdminTable = new TableLocator(tester,"report-individual-2").getTable();

        // Quick row/column count
        assertEquals(10, reportAdminTable.getRowCount());
        assertEquals(5, reportAdminTable.getColumnCount());
        TableAssertions tableAssertions = assertions.getTableAssertions();

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportAdminTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportAdminTable, 6, 0, ISSUE_TYPE_SUB_TASK);

        tableAssertions.assertTableContainsRow(reportAdminTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-15", IGNORE, "ST of HSP-6 - admin - developer - another version - 3h", "3 hours"});
        tableAssertions.assertTableContainsRow(reportAdminTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-11", IGNORE, "ST of HSP-6 - admin - no version set - 2h", "2 hours"});


        // Check the individual results for "Developer<input>
        WebTable reportDevTable = new TableLocator(tester,"report-individual-1").getTable();

        // Quick row/column count
        assertEquals(5, reportDevTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportDevTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportDevTable, 8, 0, ISSUE_TYPE_SUB_TASK);


        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-12", IGNORE, IGNORE, IGNORE, "4 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});

        if (displayUnestimated)
        {
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
        }


        // Check the summary table
        WebTable reportSummaryTable = new TableLocator(tester,"report-summary").getTable();
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertReportSummaryHeader(reportSummaryTable);
        
        tableAssertions.assertTableRowEquals(reportSummaryTable, 2, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "23 hours, 6 minutes", "1 week, 5 hours, 6 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 3, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "5 hours", "1 week, 2 days, 8 hours, 9 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 1, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "6 days", "1 day, 4 hours, 6 minutes", "8 weeks, 2 days, 13 hours, 14 minutes"});
    }

    public void test_TT_OnlyVersionOrBlankVersionSubtasks_DisplayUnestimated() throws SAXException
    {
        test_TT_OnlyVersionOrBlankVersionSubtasks(true);
    }

    public void test_TT_OnlyVersionOrBlankVersionSubtasks_NoDisplayUnestimated() throws SAXException
    {
        test_TT_OnlyVersionOrBlankVersionSubtasks(false);
    }

    private void test_TT_OnlyVersionOrBlankVersionSubtasks(boolean displayUnestimated) throws SAXException
    {
        // has time tracking and subtasks in the data
        backdoor.restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ALSO_BLANK_VERSION_SUBTASKS, displayUnestimated);


        // Check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_ESTIMATES + ")");

        // Check that a resolved sub-task is not listed
        text.assertTextNotPresent("HSP-21");
        text.assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // Check the individual results for Administrator
        WebTable reportAdminTable = new TableLocator(tester,"report-individual-2").getTable();
        TableAssertions tableAssertions = assertions.getTableAssertions();
        // Quick row/column count
        assertEquals(5, reportAdminTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportAdminTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportAdminTable, 6, 0, ISSUE_TYPE_SUB_TASK);
        tableAssertions.assertTableRowEquals(reportAdminTable, 7, new Object[]{ISSUE_TYPE_SUB_TASK, "HSP-11", IMAGE_MAJOR_CELL, "ST of HSP-6 - admin - no version set - 2h", "2 hours"});


        // Check the individual results for "Developer<input>
        WebTable reportDevTable = new TableLocator(tester,"report-individual-1").getTable();

        // Quick row/column count
        assertEquals(5, reportDevTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportDevTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportDevTable, 8, 0, ISSUE_TYPE_SUB_TASK);

        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});

        if (displayUnestimated)
        {
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
        }
        tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);


        // Check the summary table
        WebTable reportSummaryTable = new TableLocator(tester,"report-summary").getTable();
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertReportSummaryHeader(reportSummaryTable);

        tableAssertions.assertTableRowEquals(reportSummaryTable, 2, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "19 hours, 6 minutes", "1 week, 1 hour, 6 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 3, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "2 hours", "1 week, 2 days, 5 hours, 9 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 1, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "6 days", "21 hours, 6 minutes", "8 weeks, 2 days, 6 hours, 14 minutes"});
    }

    public void test_TT_OnlyVersionSubtasks_DisplayUnestimated() throws SAXException
    {
        test_TT_OnlyVersionSubtasks(true);
    }

    public void test_TT_OnlyVersionSubtasks_NoDisplayUnestimated() throws SAXException
    {
        test_TT_OnlyVersionSubtasks(false);
    }

    private void test_TT_OnlyVersionSubtasks(boolean displayUnestimated) throws SAXException
    {
        // has time tracking and subtasks in the data
        backdoor.restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ONLY_VERSION_SUBTASKS, displayUnestimated);


        // Check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_ESTIMATES + ")");

        // Check that a resolved sub-task is not listed
        text.assertTextNotPresent("HSP-21");
        text.assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // Check the individual results for Administrator
        WebTable reportAdminTable = new TableLocator(tester,"report-individual-2").getTable();

        // Quick row/column count
        assertEquals(5, reportAdminTable.getColumnCount());
        TableAssertions tableAssertions = assertions.getTableAssertions();
        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportAdminTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableContainsRowCount(reportAdminTable, new String[]{"Sub-task"}, 0);


        // Check the individual results for "Developer<input>
        WebTable reportDevTable = new TableLocator(tester,"report-individual-1").getTable();

        // Quick row/column count
        assertEquals(5, reportDevTable.getColumnCount());

        // Assert thead cells
        tableAssertions.assertTableRowEquals(reportDevTable, 0, TABLE_HEADER_COLUMNS);

        // Assert the tbody rows are correct - note that rowspans show their value across other rows
        tableAssertions.assertTableCellHasText(reportDevTable, 8, 0, ISSUE_TYPE_SUB_TASK);


        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});

        if (displayUnestimated)
        {
            tableAssertions.assertTableContainsRow(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{ISSUE_TYPE_SUB_TASK, "HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
        }
        tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);
        tableAssertions.assertTableContainsRowCount(reportDevTable, new String[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE}, 0);


        // Check the summary table
        WebTable reportSummaryTable = new TableLocator(tester,"report-summary").getTable();
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertReportSummaryHeader(reportSummaryTable);

        tableAssertions.assertTableRowEquals(reportSummaryTable, 2, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "19 hours, 6 minutes", "1 week, 1 hour, 6 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 3, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "-", "1 week, 2 days, 3 hours, 9 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        tableAssertions.assertTableRowEquals(reportSummaryTable, 1, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "6 days", "19 hours, 6 minutes", "8 weeks, 2 days, 4 hours, 14 minutes"});
    }

    public void testXss()
    {
        backdoor.restoreData("TestVersionWorkLoadReportSubTasks.xml");

        backdoor.issues().setSummary("HSP-6", XSS);

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ALL_VERSION_SUBTASKS, false);

        // Check we are seeing the report page
        tester.assertTextPresent(PROJECT_HOMOSAP + " (Version: " + VERSION_WITH_ESTIMATES + ")");
        tester.assertTextNotPresent(XSS);
    }

    //helper methods
    private final static Map SUBTASK_INCLUSION_MAP;
    private static final String ALL_VERSION_SUBTASKS = "ALL_VERSION_SUBTASKS";
    private static final String ALSO_BLANK_VERSION_SUBTASKS = "ALSO_BLANK_VERSION_SUBTASKS";
    private static final String ONLY_VERSION_SUBTASKS = "ONLY_VERSION_SUBTASKS";

    static
    {
        SUBTASK_INCLUSION_MAP = new HashMap();
        SUBTASK_INCLUSION_MAP.put(ALL_VERSION_SUBTASKS, "Including all sub-tasks");
        SUBTASK_INCLUSION_MAP.put(ALSO_BLANK_VERSION_SUBTASKS, "Also including sub-tasks without a version set");
        SUBTASK_INCLUSION_MAP.put(ONLY_VERSION_SUBTASKS, "Only including sub-tasks with the selected version");
    }

    private void generateVersionWorkLoadReport(Long projectId, String versionName, String subtaskInclusion, boolean displayUnestimated)
    {
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        tester.selectOption("versionId", "- " + versionName);
        if (subtaskInclusion != null)
        {
            String optionText = (String) SUBTASK_INCLUSION_MAP.get(subtaskInclusion);
            tester.selectOption("subtaskInclusion", optionText);
        }
        tester.selectOption("displayUnknown", displayUnestimated ? "Yes" : "No");
        tester.submit("Next");
    }

    private void generateVersionWorkLoadReport(long projectId, String versionName)
    {
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        tester.selectOption("versionId", "- " + versionName);
        tester.submit("Next");
    }

    private void setFixForVersion(String issuekey, String version)
    {
        navigation.issue().gotoIssue(issuekey);
        tester.clickLink("edit-issue");
        tester.selectOption("fixVersions", version);
        tester.selectOption("versions", version);
        tester.submit("Update");
    }

    private void assertReportSummaryHeader(WebTable firstTab)
    {
        final String[] expectedColumnNames = new String[]{"User", "Bug", "New Feature", "Task", "Improvement", "Sub-task", "Total Time Remaining"};

        final String[][] tableText = firstTab.asText();
        final List<String> columnNames = new ArrayList<String>(tableText[0].length);
        for (String s:tableText[0])
        {
            columnNames.add(s.trim());
        }
        assertArrayEquals(expectedColumnNames, columnNames.toArray());
    }
}
