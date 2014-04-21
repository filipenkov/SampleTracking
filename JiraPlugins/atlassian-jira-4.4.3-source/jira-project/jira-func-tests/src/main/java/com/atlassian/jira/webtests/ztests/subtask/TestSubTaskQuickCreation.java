package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestSubTaskQuickCreation extends FuncTestCase
{
    private static final String ISSUE_PARENT = "HSP-6";
    private static final String SUB_TASKS_TABLE_ID = "issuetable";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestTimeTrackingAggregates.xml");
    }

    public void testSubTaskDisplayOptions() throws Exception
    {
        // HSP-7 and HSP-8 are children of HSP-6
        navigation.issue().resolveIssue("HSP-7", "Fixed", "");

        navigation.issue().gotoIssue(ISSUE_PARENT);

        // should be in "Show All" view
        text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 1", "Resolved", "sub 2", "Open");

        // click "Show Open"
        tester.clickLink("subtasks-show-open");

        // now only open sub tasks are shown
        text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 2", "Open");
        text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID), "sub 1");
        text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID), "Resolved");

        // click "Show All"
        tester.clickLink("subtasks-show-all");

        // all sub tasks are visible again
        text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 1", "Resolved", "sub 2", "Open");
    }

    public void testFormHasVerticalLayout() throws SAXException
    {
        navigation.issue().gotoIssue(ISSUE_PARENT);
        assertVerticalFormPresent();
        assertFormFieldsPresent();
        assertOriginalEstimateFieldIsPresent();
    }

    public void testOriginalEstimateVisibility() throws Exception
    {
        navigation.issue().gotoIssue(ISSUE_PARENT);
        assertVerticalFormPresent();
        assertFormFieldsPresent();
        assertOriginalEstimateFieldIsPresent();

        administration.timeTracking().disable();
        navigation.issue().gotoIssue(ISSUE_PARENT);
        assertVerticalFormPresent();
        assertFormFieldsPresent();
        assertOriginalEstimateFieldIsNotPresent();

        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        navigation.issue().gotoIssue(ISSUE_PARENT);
        assertVerticalFormPresent();
        assertFormFieldsPresent();
        assertOriginalEstimateFieldIsPresent();
    }

    public void testCreateSubTask() throws Exception
    {
        navigation.issue().gotoIssue(ISSUE_PARENT);
        tester.setFormElement("summary", "New test sub-task");
        tester.selectOption("issuetype", "Sub-task");
        tester.selectOption("assignee", FRED_FULLNAME);
        tester.setFormElement("timetracking", "8h");
        tester.clickButton("stqc_submit");

        assertVerticalFormPresent();
        assertFormFieldsPresent();
        assertOriginalEstimateFieldIsPresent();

        // assert new sub-task was created ok
        text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "New test sub-task", "Open", FRED_FULLNAME, "0%");

        navigation.issue().gotoIssue("HSP-21");
        text.assertTextPresent(locator.id("project-name-val"),"homosapien");
        text.assertTextPresent(locator.id("parent_issue_summary"),"HSP-6 parent 1");
        text.assertTextPresent(locator.id("issue_header_summary"),"New test sub-task");
    }

    public void testCreateSubTaskNotVisibleWithoutPermission()
    {
        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkPresent("stqc_show");

        // now change permissions so that the current user doesn't have permission to create sub-tasks.
        administration.permissionSchemes().defaultScheme().removePermission(11, "jira-users");

        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkNotPresent("stqc_show");
    }

    private void assertVerticalFormPresent()
    {
        tester.assertElementPresent("subtask_container_vertical");
        text.assertTextPresent(locator.id("stqc_form_header"),"Create Sub-Task");
    }

    /**
     * assert the default fields are present in the quick create form
     */
    private void assertFormFieldsPresent()
    {
        tester.assertTextPresent("Summary:");
        tester.assertTextPresent("Issue Type:");
        tester.assertTextPresent("Assignee:");
        tester.assertFormElementPresent("summary");
        tester.assertFormElementPresent("issuetype");
        tester.assertFormElementPresent("assignee");
    }

    /**
     * assert the original estimate field is present in the quick create form
     */
    private void assertOriginalEstimateFieldIsPresent()
    {
        tester.assertTextPresent("Original Estimate:");
        tester.assertFormElementPresent("timetracking");
    }

    /**
     * assert the original estimate field is not present in the quick create form
     */
    private void assertOriginalEstimateFieldIsNotPresent()
    {
        tester.assertTextNotPresent("Original Estimate:");
        tester.assertFormElementNotPresent("timetracking");
    }
}
