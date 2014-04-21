package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.HashMap;
import java.util.Map;
/**
 * Tests TaskAdmin function
 *
 * @since 4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestTaskAdmin extends JIRAWebTest
{
    private static final String PROJECT_WHAT1 = "WHAT1";
    private static final String PROJECT_WHAT2 = "WHAT2";
    private static final String DEFAULT_WORKFLOW_SCHEME = "Default";
    private static final String RUBBISH_WORKFLOW_SCHEME = "Rubbish";

    private static final String TASK_HEADER_CSS_CLASS = "pb_description";
    private static final String TASK_HEADER_LOCATOR = String.format("//span[@class='%s']", TASK_HEADER_CSS_CLASS);

    public TestTaskAdmin(String name)
    {
        super(name);
    }

    public void testTaskView()
    {
        restoreData("TestTaskAdminTwoAdmins.xml");

        // go from DEFAULT to RUBBISH in WHAT1
        Map statusMapping = new HashMap();
        administration.project().associateWorkflowScheme(PROJECT_WHAT1, RUBBISH_WORKFLOW_SCHEME, statusMapping, false);
        /// wait till it gets to ACK stage
        long taskId1 = getSubmittedTaskId();
        waitForTaskAcknowledgement(taskId1);

        // go from DEFAULT to RUBBISH in WHAT2
        statusMapping = new HashMap();
        administration.project().associateWorkflowScheme(PROJECT_WHAT2, RUBBISH_WORKFLOW_SCHEME, statusMapping, false);
        /// wait till it gets to ACK stage
        long taskId2 = getSubmittedTaskId();
        waitForTaskAcknowledgement(taskId2);

        // Now migrate back to DEFAULT from RUBBISH in WHAT1
        statusMapping = new HashMap();
        statusMapping.put("mapping_1_10000", STATUS_OPEN);
        administration.project().associateWorkflowScheme(PROJECT_WHAT1, DEFAULT_WORKFLOW_SCHEME, statusMapping, false);
        /// wait till it gets to ACK stage
        long taskId3 = getSubmittedTaskId();
        waitForTaskAcknowledgement(taskId3);

        // OK we should now have 3 tasks in our task manager that are finished but not ACK'ed
        gotoPage("/secure/admin/jira/TaskAdmin.jspa");

        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Default'");

        // assert we have a link to Acknowledge the tasks
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId1 + "&schemeId=10000");
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId2 + "&schemeId=10000");
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId3);

        // login as a different admin
        logout();
        login("admin2", "admin2");

        gotoPage("/secure/admin/jira/TaskAdmin.jspa");

        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Default'");

        // assert we have a NO link to Acknowledge the tasks
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId1 + "&schemeId=10000");
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId2 + "&schemeId=10000");
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId3);

        // ok start another migrate as admin2
        // Now migrate back to DEFAULT from RUBBISH in WHAT1
        statusMapping = new HashMap();
        statusMapping.put("mapping_1_10000", STATUS_OPEN);
        administration.project().associateWorkflowScheme(PROJECT_WHAT2, DEFAULT_WORKFLOW_SCHEME, statusMapping, false);
        /// wait till it gets to ACK stage
        long taskId4 = getSubmittedTaskId();
        waitForTaskAcknowledgement(taskId4);

        gotoPage("/secure/admin/jira/TaskAdmin.jspa");

        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Default'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Default'");

        // assert we have a NO link to Acknowledge the tasks
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId1 + "&schemeId=10000");
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId2 + "&schemeId=10000");
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId3);

        // but we should have one for one started by admin2
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId4);

        // and now back to admin to test the reverse.
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        gotoPage("/secure/admin/jira/TaskAdmin.jspa");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Rubbish'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT1' to workflow scheme 'Default'");
        assertTextPresentInTaskHeader("Migrate the issues in project 'WHAT2' to workflow scheme 'Default'");

        // assert we have a link to Acknowledge the tasks
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId1 + "&schemeId=10000");
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId2 + "&schemeId=10000");
        assertTextPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId3);

        // but we NOT should have one for one started by admin2
        assertTextNotPresent("secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10001&taskId=" + taskId4);

    }

    private void assertTextPresentInTaskHeader(String textToTest)
    {
        assertions.assertNodeHasText(TASK_HEADER_LOCATOR, textToTest);
    }

}
