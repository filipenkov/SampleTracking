package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.TableData;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.REPORTS })
public class TestVersionWorkloadReport extends JIRAWebTest
{
    private static final String VERSION_WITH_ESTIMATES = "Version with estimates";
    private static final String VERSION_WITH_NO_ESTIMATE = "Version with no estimate";
    private static final String VERSION_WITH_NO_ISSUES = "Version with no issues";
    private static final String NO_ESTIMATE = "No Estimate";
    private static final Object[] TABLE_HEADER_COLUMNS = new Object[]{"Key", "Type", "Priority", "Summary", "Estimated Time Remaining"};
    private static final Object[] NO_ESTIMATE_TOTAL = new Object[]{"Totals", NO_ESTIMATE, NO_ESTIMATE, NO_ESTIMATE, NO_ESTIMATE, NO_ESTIMATE, "0 minutes"};
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
    private static final Object IGNORE = null;
    private static final Long PROJECT_HOMOSAP_ID = (long) 10000;

    public TestVersionWorkloadReport(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestVersionWorkloadReport.xml");
    }

    /**
     * Test the version workload reports validation on the configuration page
     */
    public void testVersionWorkloadReportValidation()
    {
        gotoPage("/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        selectOption("versionId", "Unreleased Versions");
        submit("Next");
        assertTextPresent("Please select an actual version.");
    }

    public void testVersionWorkLoadReport()
    {
        restoreData("TestVersionWorkloadReportFormat.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("6 days, 16 hours");//check total timeremaining

        reconfigureTimetracking(FORMAT_HOURS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("160h");//assert total time remaining changed into (6*24 + 16)h

        reconfigureTimetracking(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("6d 16h");

        reconfigureTimetracking(FORMAT_PRETTY);
        setFixForVersion("HSP-2", VERSION_NAME_FOUR);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("5 days, 19 hours, 30 minutes"); //total work should be one day less

        reconfigureTimetracking(FORMAT_HOURS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("139.5h");//(24*5 + 19 + 0.5)h

        reconfigureTimetracking(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        assertTextPresent("5d 19.5h");

        reconfigureTimetracking(FORMAT_PRETTY);
        logWorkOnIssueWithComment("HSP-1", "5d", "the workload should decreased by 5 days");
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_ONE);
        //assert time remaining has been decreased by 5 days
        assertTextNotPresent("5 days, 19 hours, 30 minutes");
        assertTextPresent("19 hours, 30 minutes");

        reconfigureTimetracking(FORMAT_DAYS);
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_NAME_FOUR);
        assertTextPresent("HSP-2");
        assertTextPresent("20.5h");
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
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_NO_ISSUES);

        //check the summary
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");

        assertEquals(2, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());

        assertReportSummaryHeader(reportSummaryTable);

        assertTableRowEquals(reportSummaryTable, 1, NO_ESTIMATE_TOTAL);
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
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_NO_ESTIMATE);

        //check the summary
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");

        assertEquals(3, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());

        assertReportSummaryHeader(reportSummaryTable);

        //check that the admin is listed with no estimates
        assertTableRowEquals(reportSummaryTable, 1, new Object[]{ADMIN_FULLNAME, "-", "-", "-", "-", "-", NO_ESTIMATE});
        assertTableRowEquals(reportSummaryTable, 2, NO_ESTIMATE_TOTAL);

        //check the individual estimate
        WebTable reportAdminTable = getDialog().getResponse().getTableWithID("report_" + ADMIN_USERNAME);

        assertEquals(6, reportAdminTable.getRowCount());
        assertEquals(5, reportAdminTable.getColumnCount());

        assertTableRowEquals(reportAdminTable, 0, new Object[]{ADMIN_FULLNAME, NO_ESTIMATE});
        assertTableRowEquals(reportAdminTable, 1, new Object[]{ISSUE_TYPE_BUG, NO_ESTIMATE});

        assertTableRowEquals(reportAdminTable, 2, TABLE_HEADER_COLUMNS);

        //look for the two entries on row 4 and 5
        assertTableHasMatchingRowFromTo(reportAdminTable, 3, 5, new Object[]{"HSP-1", IMAGE_BUG_CELL, IMAGE_CRITICAL_CELL, "Issue with no estimate", NO_ESTIMATE});
        assertTableHasMatchingRowFromTo(reportAdminTable, 3, 5, new Object[]{"HSP-2", IMAGE_BUG_CELL, IMAGE_TRIVIAL_CELL, "Issue without estimate", NO_ESTIMATE});

        assertTrue(tableCellHasText(reportAdminTable, 5, 0, "Return to Summary"));

    }

    /**
     * Test the version workload report displays correct information
     *
     * @throws org.xml.sax.SAXException on bad html
     */
    public void testVersionWorkloadReportWithEstimates() throws SAXException
    {
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES);
        //check we are seeing the report page
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_ESTIMATES);
        //check the summary table
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertReportSummaryHeader(reportSummaryTable);
        //check that the admin, dev and unassignedestimates are correct

        assertTableRowEquals(reportSummaryTable, 1, new Object[] { DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "6 minutes", "6 days, 6 hours, 6 minutes" });
        assertTableRowEquals(reportSummaryTable, 2, new Object[] { ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "-", "1 week, 2 days, 3 hours, 9 minutes" });
        assertTableRowEquals(reportSummaryTable, 3, new Object[] { "Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes" });
        assertTableRowEquals(reportSummaryTable, 4, new Object[] { "Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", NO_ESTIMATE, "6 days", "6 minutes", "8 weeks, 1 day, 9 hours, 14 minutes" });

        //check the individual estimate of admin
        WebTable reportAdminTable = getDialog().getResponse().getTableWithID("report_" + ADMIN_USERNAME);

        assertEquals(6, reportAdminTable.getRowCount());
        assertEquals(5, reportAdminTable.getColumnCount());

        assertTableRowEquals(reportAdminTable, 0, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes"});
        assertTableRowEquals(reportAdminTable, 1, new Object[]{ISSUE_TYPE_BUG, "1 week, 2 days, 3 hours, 9 minutes"});
        assertTableRowEquals(reportAdminTable, 2, TABLE_HEADER_COLUMNS);

        //look for the two entries on row 4 and 5
        assertTableHasMatchingRowFromTo(reportAdminTable, 3, 5, new Object[]{"HSP-3", IMAGE_BUG_CELL, IMAGE_BLOCKER_CELL, "bug 1 with estimate", "1 week, 2 days, 3 hours, 4 minutes"});
        assertTableHasMatchingRowFromTo(reportAdminTable, 3, 5, new Object[]{"HSP-4", IMAGE_BUG_CELL, IMAGE_MAJOR_CELL, "bug 2 with estimate", "5 minutes"});

        assertTrue(tableCellHasText(reportAdminTable, 5, 0, "Return to Summary"));

        //check the individual estimate of dev
        WebTable reportDevTable = getDialog().getResponse().getTableWithID("report_" + DEV_USERNAME);

        assertEquals(11, reportDevTable.getRowCount());
        assertEquals(5, reportDevTable.getColumnCount());

        assertTableRowEquals(reportDevTable, 0, new Object[]{DEVELOPER_FULLNAME, "6 days, 6 hours, 6 minutes"});

        TableData expectedSubTable = new TableData()
                .addRow(new Object[]{ISSUE_TYPE_BUG, "6 hours"})
                .addRow(TABLE_HEADER_COLUMNS)
                .addRow(new Object[]{"HSP-5", IMAGE_BUG_CELL, IMAGE_MAJOR_CELL, "new bug with estimate", "6 hours"});
        assertTableHasSubTable(reportDevTable, expectedSubTable.toArray());

        expectedSubTable = new TableData()
                .addRow(new Object[]{ISSUE_TYPE_IMPROVEMENT, "6 days"})
                .addRow(TABLE_HEADER_COLUMNS)
                .addRow(new Object[]{"HSP-6", IMAGE_IMPROVEMENT_CELL, IMAGE_MINOR_CELL, "improvement with estimate", "6 days"});
        assertTableHasSubTable(reportDevTable, expectedSubTable.toArray());

        expectedSubTable = new TableData()
                .addRow(new Object[] { ISSUE_TYPE_SUB_TASK, "6 minutes" })
                .addRow(TABLE_HEADER_COLUMNS)
                .addRow(new Object[] { "HSP-6", IMAGE_SUB_TASK_CELL, IMAGE_CRITICAL_CELL, "subtask issue with estimate", "6 minutes" });
        assertTableHasSubTable(reportDevTable, expectedSubTable.toArray());
        expectedSubTable = new TableData()
                .addRow(new Object[] { ISSUE_TYPE_SUB_TASK, "6 minutes" })
                .addRow(TABLE_HEADER_COLUMNS)
                .addRow(new Object[] { "HSP-7", IMAGE_SUB_TASK_CELL, IMAGE_CRITICAL_CELL, "subtask issue with estimate", "6 minutes" });
        assertTableHasSubTable(reportDevTable, expectedSubTable.toArray());

        //check the individual estimate of Unassigned
        WebTable reportUnassignedTable = getDialog().getResponse().getTableWithID("report_" + "Unassigned");

        assertEquals(6, reportUnassignedTable.getRowCount());
        assertEquals(5, reportUnassignedTable.getColumnCount());

        assertTableRowEquals(reportUnassignedTable, 0, new Object[]{"Unassigned", "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableRowEquals(reportUnassignedTable, 1, new Object[]{ISSUE_TYPE_NEWFEATURE, "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableRowEquals(reportUnassignedTable, 2, TABLE_HEADER_COLUMNS);

        //look for the two entries on row 4 and 5
        assertTableHasMatchingRowFromTo(reportUnassignedTable, 3, 5, new Object[]{"HSP-8", IMAGE_NEWFEATURE_CELL, IMAGE_TRIVIAL_CELL, "unassigned issue with estimate", "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableHasMatchingRowFromTo(reportUnassignedTable, 3, 5, new Object[]{"HSP-9", IMAGE_NEWFEATURE_CELL, IMAGE_MAJOR_CELL, HTML_SUMMARY, NO_ESTIMATE});

        assertTrue(tableCellHasText(reportUnassignedTable, 5, 0, "Return to Summary"));
    }

    public void testVersionIsEncoded()
    {
        restoreData("TestVersionAndComponentsWithHTMLNames.xml");
        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, "\"version<input >");
        assertTextPresent("&quot;version&lt;input &gt;");
        assertTextNotPresent("\"version<input >");
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
        restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ALL_VERSION_SUBTASKS, displayUnestimated);
        //check we are seeing the report page
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_ESTIMATES);

        // check that a resolved sub-task is not listed
        assertTextNotPresent("HSP-21");
        assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // check subtasks information for ADMIN
        WebTable reportAdminTable = getDialog().getResponse().getTableWithID("report_" + ADMIN_USERNAME);

        assertEquals(5, reportAdminTable.getColumnCount());
        int rowCount = reportAdminTable.getRowCount();
        int rowIndex = tableIndexOf(reportAdminTable, new Object[]{"Sub-task", "5 hours"});
        assertTrue("Sub task row is not present in report", rowIndex != -1);
        assertTableRowEquals(reportAdminTable, rowIndex + 1, TABLE_HEADER_COLUMNS);
        assertTableHasMatchingRowFromTo(reportAdminTable, rowIndex, rowCount, new Object[]{"HSP-15", IGNORE, IGNORE, IGNORE, "3 hours"});
        assertTableHasMatchingRowFromTo(reportAdminTable, rowIndex, rowCount, new Object[]{"HSP-11", IGNORE, IGNORE, IGNORE, "2 hours"});

        // check subtask info for DEV
        WebTable reportDevTable = getDialog().getResponse().getTableWithID("report_" + DEV_USERNAME);
        assertEquals(5, reportDevTable.getColumnCount());

        rowCount = reportDevTable.getRowCount();
        rowIndex = tableIndexOf(reportDevTable, new Object[]{"Sub-task", "23 hours, 6 minutes"});
        assertTrue("Sub task row is not present in report", rowIndex != -1);
        assertTableRowEquals(reportDevTable, rowIndex + 1, TABLE_HEADER_COLUMNS);

        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-12", IGNORE, IGNORE, IGNORE, "4 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});
        if (displayUnestimated)
        {
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }

        //check the summary table
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertTableRowEquals(reportSummaryTable, 1, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "23 hours, 6 minutes", "1 week, 5 hours, 6 minutes"});
        assertTableRowEquals(reportSummaryTable, 2, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "5 hours", "1 week, 2 days, 8 hours, 9 minutes"});
        assertTableRowEquals(reportSummaryTable, 3, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", NO_ESTIMATE, "6 days", "1 day, 4 hours, 6 minutes", "8 weeks, 2 days, 13 hours, 14 minutes"});
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
        restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ALSO_BLANK_VERSION_SUBTASKS, displayUnestimated);
        //check we are seeing the report page
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_ESTIMATES);

        // check that a resolved sub-task is not listed
        assertTextNotPresent("HSP-21");
        assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // check subtasks information for ADMIN
        WebTable reportAdminTable = getDialog().getResponse().getTableWithID("report_" + ADMIN_USERNAME);

        int rowCount = reportAdminTable.getRowCount();
        assertEquals(5, reportAdminTable.getColumnCount());

        int rowIndex = tableIndexOf(reportAdminTable, new Object[]{"Sub-task", "2 hours"});
        assertTrue("Sub task row is not present in report", rowIndex != -1);
        assertTableRowEquals(reportAdminTable, rowIndex + 1, TABLE_HEADER_COLUMNS);
        assertTableHasMatchingRowFromTo(reportAdminTable, rowIndex + 2, rowCount, new Object[]{"HSP-11", null, null, null, "2 hours"});

        WebTable reportDevTable = getDialog().getResponse().getTableWithID("report_" + DEV_USERNAME);
        rowCount = reportDevTable.getRowCount();

        assertEquals(5, reportDevTable.getColumnCount());

        rowIndex = tableIndexOf(reportDevTable, new Object[]{"Sub-task", "19 hours, 6 minutes"});
        assertTrue("Sub task row is not present in report", rowIndex != -1);
        assertTableRowEquals(reportDevTable, rowIndex + 1, TABLE_HEADER_COLUMNS);

        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});
        if (displayUnestimated)
        {
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});

        //check the summary table
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertTableRowEquals(reportSummaryTable, 1, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "19 hours, 6 minutes", "1 week, 1 hour, 6 minutes"});
        assertTableRowEquals(reportSummaryTable, 2, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "2 hours", "1 week, 2 days, 5 hours, 9 minutes"});
        assertTableRowEquals(reportSummaryTable, 3, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", NO_ESTIMATE, "6 days", "21 hours, 6 minutes", "8 weeks, 2 days, 6 hours, 14 minutes"});
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
        restoreData("TestVersionWorkLoadReportSubTasks.xml");

        generateVersionWorkLoadReport(PROJECT_HOMOSAP_ID, VERSION_WITH_ESTIMATES, ONLY_VERSION_SUBTASKS, displayUnestimated);
        //check we are seeing the report page
        assertTextPresent(PROJECT_HOMOSAP + " (" + PROJECT_HOMOSAP_KEY + ") - " + VERSION_WITH_ESTIMATES);

        // check that a resolved sub-task is not listed
        assertTextNotPresent("HSP-21");
        assertTextNotPresent("ST of HSP-6 - developer - VWE - resolved");

        // check subtasks information for ADMIN
        WebTable reportAdminTable = getDialog().getResponse().getTableWithID("report_" + ADMIN_USERNAME);
        assertEquals(5, reportAdminTable.getColumnCount());

        int rowIndex = tableIndexOf(reportAdminTable, new Object[]{"Sub-task"});
        assertTrue("Sub task row SHOULD NOT be present in report", rowIndex == -1);

        WebTable reportDevTable = getDialog().getResponse().getTableWithID("report_" + DEV_USERNAME);
        int rowCount = reportDevTable.getRowCount();

        assertEquals(5, reportDevTable.getColumnCount());

        rowIndex = tableIndexOf(reportDevTable, new Object[]{"Sub-task", "19 hours, 6 minutes"});
        assertTrue("Sub task row is not present in report", rowIndex != -1);
        assertTableRowEquals(reportDevTable, rowIndex + 1, TABLE_HEADER_COLUMNS);

        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-7", IGNORE, IGNORE, IGNORE, "6 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-10", IGNORE, IGNORE, IGNORE, "3 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-13", IGNORE, IGNORE, IGNORE, "4 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-14", IGNORE, IGNORE, IGNORE, "0 minutes"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-16", IGNORE, IGNORE, IGNORE, "5 hours"});
        assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-17", IGNORE, IGNORE, IGNORE, "7 hours"});
        if (displayUnestimated)
        {
            assertTableHasMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        else
        {
            assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-18", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        }
        assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-19", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});
        assertTableHasNoMatchingRowFromTo(reportDevTable, rowIndex, rowCount, new Object[]{"HSP-20", IGNORE, IGNORE, IGNORE, NO_ESTIMATE});

        //check the summary table
        WebTable reportSummaryTable = getDialog().getResponse().getTableWithID("report_summary");
        assertEquals(5, reportSummaryTable.getRowCount());
        assertEquals(7, reportSummaryTable.getColumnCount());
        assertTableRowEquals(reportSummaryTable, 1, new Object[]{DEVELOPER_FULLNAME, "6 hours", "-", "-", "6 days", "19 hours, 6 minutes", "1 week, 1 hour, 6 minutes"});
        assertTableRowEquals(reportSummaryTable, 2, new Object[]{ADMIN_FULLNAME, "1 week, 2 days, 3 hours, 9 minutes", "-", "-", "-", "-", "1 week, 2 days, 3 hours, 9 minutes"});
        assertTableRowEquals(reportSummaryTable, 3, new Object[]{"Unassigned", "-", "5 weeks, 6 days, 23 hours, 59 minutes", "-", "-", "-", "5 weeks, 6 days, 23 hours, 59 minutes"});
        assertTableRowEquals(reportSummaryTable, 4, new Object[]{"Totals", "1 week, 2 days, 9 hours, 9 minutes", "5 weeks, 6 days, 23 hours, 59 minutes", NO_ESTIMATE, "6 days", "19 hours, 6 minutes", "8 weeks, 2 days, 4 hours, 14 minutes"});
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
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        selectOption("versionId", "- " + versionName);
        if (subtaskInclusion != null)
        {
            String optionText = (String) SUBTASK_INCLUSION_MAP.get(subtaskInclusion);
            selectOption("subtaskInclusion", optionText);
        }
        selectOption("displayUnknown", displayUnestimated ? "Yes" : "No");
        submit("Next");
    }

    private void generateVersionWorkLoadReport(long projectId, String versionName)
    {
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:version-workload");
        selectOption("versionId", "- " + versionName);
        submit("Next");
    }

    private void setFixForVersion(String issuekey, String version)
    {
        gotoIssue(issuekey);
        clickLink("editIssue");
        selectOption("fixVersions", version);
        selectOption("versions", version);
        submit("Update");
    }

    private void assertReportSummaryHeader(WebTable firstTab)
    {
        final List expectedColumnNames;
        expectedColumnNames = EasyList.build("User", "Bug", "New Feature", "Task", "Improvement", "Sub-task", "Total Time Remaining");

        List columnNames = getTableRowAsList(firstTab, 0);
        assertEquals(expectedColumnNames, columnNames);
    }
}
