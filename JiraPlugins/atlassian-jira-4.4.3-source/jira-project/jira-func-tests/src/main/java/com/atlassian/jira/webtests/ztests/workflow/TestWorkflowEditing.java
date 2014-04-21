package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Test that workflow editing works ok.
 * TODO: currently only checks that subtask blocking conditions work
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowEditing extends JIRAWebTest
{

    public TestWorkflowEditing(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("editableworkflow.xml");
    }

    /**
     * Confirming the add, edit and delete operations on subtask blocking conditions.
     * Originally added to cover JRA-9934.
     */
    public void testSubtaskBlockingConditions()
    {
        gotoAdmin();
        clickLink("workflows");
        clickLink("steps_live_Copy of jira");
        clickLinkWithText("Close Issue");

        // add new subtask blocking condition
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:subtaskblocking-condition");
        submit("Add");
        checkCheckbox("issue_statuses", "1");
        checkCheckbox("issue_statuses", "3");
        checkCheckbox("issue_statuses", "4");
        submit("Add");
        assertTextSequence(new String[] {
                "All sub-tasks must have one of the following statuses to allow parent issue transitions:",
                "Open", "In Progress", "or", "Reopened" });

        // now edit and add a status
        clickLinkWithText("Edit", 3);
        checkCheckbox("issue_statuses", "1");
        checkCheckbox("issue_statuses", "3");
        checkCheckbox("issue_statuses", "4");
        checkCheckbox("issue_statuses", "5");
        submit("Update");
        assertTextSequence(new String[] {
                "All sub-tasks must have one of the following statuses to allow parent issue transitions:",
                "Open",
                "In Progress",
                "Reopened",
                "or",
                "Resolved" });

        // test the condition is deleted
        clickLinkWithText("Delete", 3);
        assertTextNotPresent("All sub-tasks must have one of the following statuses to allow parent issue transitions:");
    }

    public void testWorkflowAddFromXmlNotAvailableToAdmins()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            gotoAdmin();
            clickLink("workflows");

            assertLinkWithTextNotPresent("import a workflow from XML");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testWorkflowAddFromXmlAvailableToSysAdmins()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            logout();
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            gotoAdmin();
            clickLink("workflows");

            assertLinkPresentWithText("import a workflow from XML");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }
}
