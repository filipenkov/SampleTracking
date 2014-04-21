package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Functional tests for log work
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.TIME_TRACKING })
public class TestTimeTrackingNavagableFields extends JIRAWebTest
{


    public TestTimeTrackingNavagableFields(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestTimeTrackingAggregates.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        //restoreBlankInstance();
        super.tearDown();
    }

    public void testTimeTrackingDisabled()
    {
        displayAllIssues();
        assertTextPresent("Original Estimate");
        assertTextPresent("Remaining Estimate");
        assertTextPresent("Time Spent");

        clickLinkWithText("Configure");
        selectOption("fieldId", "Original Estimate");
        selectOption("fieldId", "Remaining Estimate");
        selectOption("fieldId", "Time Spent");
        selectOption("fieldId", "\u03A3 Original Estimate");
        selectOption("fieldId", "\u03A3 Remaining Estimate");
        selectOption("fieldId", "\u03A3 Time Spent");

        deactivateTimeTracking();

        displayAllIssues();
        assertTextNotPresent("Original Estimate");
        assertTextNotPresent("Remaining Estimate");
        assertTextNotPresent("Time Spent");

        clickLinkWithText("Configure");
        assertOptionValueNotPresent("fieldId", "Original Estimate");
        assertOptionValueNotPresent("fieldId", "Remaining Estimate");
        assertOptionValueNotPresent("fieldId", "Time Spent");
        assertOptionValueNotPresent("fieldId", "\u03A3 Original Estimate");
        assertOptionValueNotPresent("fieldId", "\u03A3 Remaining Estimate");
        assertOptionValueNotPresent("fieldId", "\u03A3 Time Spent");
    }

    public void testSubTasksDisabled()
    {
        displayAllIssues();
        assertTextPresent("Original Estimate");
        assertTextPresent("Remaining Estimate");
        assertTextPresent("Time Spent");

        clickLinkWithText("Configure");
        selectOption("fieldId", "Original Estimate");
        selectOption("fieldId", "Remaining Estimate");
        selectOption("fieldId", "Time Spent");
        selectOption("fieldId", "\u03A3 Original Estimate");
        selectOption("fieldId", "\u03A3 Remaining Estimate");
        selectOption("fieldId", "\u03A3 Time Spent");


        removeAssociationOfSecuritySchemeFromProject("homosapien");
        deactivateSubTasks();

        createIssuesInBulk(2, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "Dyn issues", "Minor", null, null, null, null, null, "some description", null, null);

        displayAllIssues();
        assertTextNotPresent("Original Estimate");
        assertTextNotPresent("Remaining Estimate");
        assertTextNotPresent("Time Spent");

        clickLinkWithText("Configure");
        selectOption("fieldId", "Original Estimate");
        selectOption("fieldId", "Remaining Estimate");
        selectOption("fieldId", "Time Spent");
        assertOptionValueNotPresent("fieldId", "\u03A3 Original Estimate");
        assertOptionValueNotPresent("fieldId", "\u03A3 Remaining Estimate");
        assertOptionValueNotPresent("fieldId", "\u03A3 Time Spent");
    }

    public void testFieldsAddedToNav()
    {
        displayAllIssues();
        assertTextPresent("Original Estimate");
        assertTextPresent("Remaining Estimate");
        assertTextPresent("Time Spent");

        clickLinkWithText("Configure");
        selectOption("fieldId", "Original Estimate");
        submit("add");
        selectOption("fieldId", "Remaining Estimate");
        submit("add");
        selectOption("fieldId", "Time Spent");
        submit("add");
        displayAllIssues();
        assertTextPresent("Original Estimate");
        assertTextPresent("Remaining Estimate");
        assertTextPresent("Time Spent");
        assertTextNotPresent("\u03A3 Original Estimate");
        assertTextNotPresent("\u03A3 Remaining Estimate");
        assertTextNotPresent("\u03A3 Time Spent");

        clickLinkWithText("Configure");
        selectOption("fieldId", "\u03A3 Original Estimate");
        submit("add");
        selectOption("fieldId", "\u03A3 Remaining Estimate");
        submit("add");
        selectOption("fieldId", "\u03A3 Time Spent");
        submit("add");
        displayAllIssues();
        assertTextPresent(" Original Estimate");
        assertTextPresent(" Remaining Estimate");
        assertTextPresent(" Time Spent");
    }
}