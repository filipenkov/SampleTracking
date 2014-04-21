package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.UPGRADE_TASKS, Category.WORKLOGS })
public class TestWorklogUpgradeTasks extends JIRAWebTest
{
    public TestWorklogUpgradeTasks(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWorklogUpgradeTasks.xml");
    }

    public void testCustomNotificationSchemeIsUpdated()
    {
        navigation.gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Copy of Default Notification Scheme");

        assertTableCellHasText("notificationSchemeTable", 14, 0, "Issue Worklog Updated");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "Reporter");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "All Watchers");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "Current Assignee");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "Project Role");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "(Administrators)");

        assertTableCellHasText("notificationSchemeTable", 15, 0, "Issue Worklog Deleted");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "Reporter");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "All Watchers");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "Current Assignee");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "Project Role");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "(Administrators)");
    }

    public void testDefaultNotificationSchemeIsUpdated()
    {
        navigation.gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");

        assertTableCellHasText("notificationSchemeTable", 14, 0, "Issue Worklog Updated");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "Reporter");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "All Watchers");
        assertTableCellHasText("notificationSchemeTable", 14, 1, "Current Assignee");

        assertTableCellHasText("notificationSchemeTable", 15, 0, "Issue Worklog Deleted");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "Reporter");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "All Watchers");
        assertTableCellHasText("notificationSchemeTable", 15, 1, "Current Assignee");
    }
}
