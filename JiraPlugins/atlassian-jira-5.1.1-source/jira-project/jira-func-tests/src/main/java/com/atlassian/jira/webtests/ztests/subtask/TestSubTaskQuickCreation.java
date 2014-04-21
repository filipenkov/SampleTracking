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

    public void testCreateSubTaskNotVisibleWithoutPermission()
    {
        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkPresent("stqc_show");

        // now change permissions so that the current user doesn't have permission to create sub-tasks.
        administration.permissionSchemes().defaultScheme().removePermission(11, "jira-users");

        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkNotPresent("stqc_show");
    }
}
