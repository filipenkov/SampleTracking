package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * Tests DeveloperWorkloadReport (aka User Workload Report on the UI).
 *
 */
@WebTest({ Category.FUNC_TEST, Category.REPORTS })
public class TestDeveloperWorkloadReport extends JIRAWebTest
{
    protected Navigation navigation;
    protected TableAssertions tableAssertions;

    public TestDeveloperWorkloadReport(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestDeveloperWorkloadReport.xml");
        final FuncTestHelperFactory funcTestHelperFactory = new FuncTestHelperFactory(tester, getEnvironmentData());
        navigation = funcTestHelperFactory.getNavigation();
        tableAssertions = funcTestHelperFactory.getAssertions().getTableAssertions();
    }

    public void testDeveloperWorkloadReportBasic()
    {
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", ADMIN_USERNAME);
        submit("Next");

        assertBasicTestReport();
    }

    private void assertBasicTestReport()
    {
        assertTextPresent("User Workload Report");
        final WebTable table = getTable();
        tableAssertions.assertTableContainsRow(table, new String[] { "homosapien", "3", "1 week, 1 day, 1 hour" });
        tableAssertions.assertTableContainsRow(table, new String[] { "monkey", "3", "18 minutes" });
        tableAssertions.assertTableContainsRow(table, new String[] { "Total", "6", "1 week, 1 day, 1 hour, 18 minutes" });
    }

    public void testPreSubtaskInclusionUrls()
    {
        // subtasks and unassigned issues make the precondition for the display of subtask inclusion report option
        activateSubTasks();
        enableUnassignedIssues();

        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        // subtask inclusion options should be present now
        assertTextPresent("Sub-task Inclusion");
        assertTextPresent("Only including sub-tasks assigned to the selected user");
        assertTextPresent("Also including unassigned sub-tasks");

        // use a legacy url which doesn't specify subtask inclusion options
        gotoPage("/secure/ConfigureReport.jspa?developer=admin&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Adeveloper-workload&Next=Next");

        // check the report is the same as it was
        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssignee()
    {
        activateSubTasks();
        enableUnassignedIssues();

        // select options like legacy behaviour: only subtasks assigned to selected user are included
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", ADMIN_USERNAME);
        selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssigneeWithSubtasks()
    {
        activateSubTasks();
        enableUnassignedIssues();

        String subtask = addSubTaskToIssue("MKY-1", ISSUE_TYPE_SUB_TASK, "curious george is a monkey", "he is always getting into trouble", "2h");
        logWorkOnIssueWithComment(subtask, "1h", "work it george"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, "unassigning"); // TODO test this worked

        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", ADMIN_USERNAME);
        selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssigneeWithSubtasksOnIssues()
    {
        activateSubTasks();
        enableUnassignedIssues();

        String subtask = addSubTaskToIssue("MKY-1", ISSUE_TYPE_SUB_TASK, "curious george is a monkey", "he is always getting into trouble", "2h");
        logWorkOnIssue(subtask, "1h"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, null);

        // the subtask isn't assigned to admin, so it shouldn't show up using the only "assigned option"
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", ADMIN_USERNAME);
        selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionAlsoUnassigned()
    {
        activateSubTasks();
        enableUnassignedIssues();

        String subtask = addSubTaskToIssue("MKY-1", ISSUE_TYPE_SUB_TASK, "curious george is a monkey", "he is always getting into trouble", "2h");
        logWorkOnIssue(subtask, "1h"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, null);

        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", ADMIN_USERNAME);
        selectOption("subtaskInclusion", "Also including unassigned sub-tasks");
        submit("Next");

        // now assert we have the extra time from our subtask
        assertTextPresent("User Workload Report");
        final WebTable table = getTable();
        tableAssertions.assertTableContainsRow(table, new String[] { "homosapien", "3", "1 week, 1 day, 1 hour" });
        tableAssertions.assertTableContainsRow(table, new String[] { "monkey", "4", "1 hour, 18 minutes" });
        tableAssertions.assertTableContainsRow(table, new String[] { "Total", "7", "1 week, 1 day, 2 hours, 18 minutes" });
    }

    private WebTable getTable()
    {
        try
        {
            return getDialog().getResponse().getTableWithID("dwreport");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }


}
