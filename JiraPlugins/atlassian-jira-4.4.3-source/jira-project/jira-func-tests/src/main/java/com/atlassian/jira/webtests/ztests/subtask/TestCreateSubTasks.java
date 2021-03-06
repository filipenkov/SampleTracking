package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestCreateSubTasks extends JIRAWebTest
{
    private static final String PROJECT_MONKEY_ID = "10001";

    public TestCreateSubTasks(String name)
    {
        super(name);
    }

    public void testCreateSubTaskInJiraWithSingleSubTaskType()
    {
        // TestOneProjectWithOneIssueType.xml is set up with a single project that uses "Bugs Only" issue type scheme,
        // which has only a single "Bug" issue type.
        administration.restoreData("TestOneProjectWithOneIssueType.xml");

        // the number of projects present should not matter, so add a project too
        administration.project().addProject("neanderthal", "NEA", ADMIN_USERNAME);

        // Associate "Bugs & Subtasks" with homosapien project
        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");
        clickLink("project-config-issuetype-scheme-change");

        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'Bugs & Sub-tasks' from select box 'schemeId'.
        tester.selectOption("schemeId", "Bugs & Sub-tasks");
        tester.submit();

        navigation.issue().createIssue("homosapien", "Bug", "First issue");
        navigation.issue().viewIssue("HSP-1");

        tester.clickLink("create-subtask");
        tester.assertTextNotPresent("Choose the project and issue type"); // step 1 skipped
        tester.assertTextPresent("Create Sub-Task");
        assertTextSequence(new String[] { "Project", "homosapien", "Issue Type", "Sub-task" });
        tester.assertFormElementPresent("summary");
    }

    public void testCreateSubTasks()
    {
        restoreBlankInstance();
        addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        createSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME, CUSTOM_SUB_TASK_TYPE_DESCRIPTION);

        resetFields();
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test for sub tasks", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description for sub tasks", null, null, null);

        subTasksWithSubTasksEnabled(issueKeyNormal);
        subTasksCreateSubTaskWithCustonType(issueKeyNormal);
        subTasksWithCreatePermission(issueKeyNormal);
        subTaskWithNoSummary(issueKeyNormal);
        subTaskWithRequiredFields(issueKeyNormal);
        subTaskWithHiddenFields(issueKeyNormal);
        subTaskWithInvalidDueDate(issueKeyNormal);
        subTaskWithSchedulePermission(issueKeyNormal);
        subTaskWithAssignPermission(issueKeyNormal);
        subTaskWithModifyReporterPermission(issueKeyNormal);
        subTaskWithTimeTracking(issueKeyNormal);
        subTaskWithUnassignableUser(issueKeyNormal);
        subTaskMoveIssueWithSubTask(issueKeyNormal);
//        subTaskMoveSubTask(issueKeyNormal);
////            subTaskWithFieldSchemeRequired();
////            subTaskWithFieldSchemeHidden();
//            subTaskCreateSubTaskWithSecurity();

        deleteIssue(issueKeyNormal);

    }

    public void testCreateSubtaskSkipStep1OnlyOneProjectAndOneIssueType()
    {
        restoreData("TestCreateSubtaskOneProjectOneSubtaskType.xml");
        assertRedirectAndFollow("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000",
                ".*CreateSubTaskIssue\\.jspa\\?parentIssueId=10000&pid=" + PROJECT_MONKEY_ID + "&issuetype=4$");
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });
    }

    public void testCreateIssueSkipStep1IssueTypeSchemeInfersOneProjectAndIssueType()
    {
        // check the preselection of the project in the form to "current project"
        restoreData("TestCreateMonkeyHasOneIssueType.xml");

        // check we redirect to step 2 if we pass a pid url param for a project which has one issue type
        assertRedirectAndFollow("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000",
                ".*CreateSubTaskIssue\\.jspa\\?parentIssueId=10000&pid=" + PROJECT_MONKEY_ID + "&issuetype=5$");
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });

        // check that we do not redirect for homosapien issue since there are multiple issue types to choose from
        gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10001"); // HSP-1
        assertTextSequence(new String[] { "Create Issue", "Choose the issue type" });

        // logout and then rerequest the url
        logout();
        gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000"); // MKY-1
        assertTextPresent("You are not logged in");
        clickLinkWithText("Log In");
        setFormElement("os_username", ADMIN_USERNAME);
        setFormElement("os_password", ADMIN_USERNAME);
        setWorkingForm("login-form");
        submit();
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });
    }

    /**
     * Tests if the 'Sub Task' link is available with 'Sub Tasks' enabled
     */
    public void subTasksWithSubTasksEnabled(String issueKey)
    {
        activateSubTasks();
        log("Sub Task Create: Tests the availability of the 'Sub Task' Link with 'Sub Tasks' enabled");
        gotoIssue(issueKey);
        assertLinkPresent("create-subtask");

        deactivateSubTasks();
        gotoIssue(issueKey);
        assertLinkNotPresent("create-subtask");
    }

    /**
     * Tests the ability to create a sub task using a custom-made sub task type
     */
    public void subTasksCreateSubTaskWithCustonType(String issueKey)
    {
        log("Sub Task Create: Tests the ability to create a sub task using a custom-made sub task type");
        createSubTaskStep1(issueKey, CUSTOM_SUB_TASK_TYPE_NAME);

        setFormElement("summary", CUSTOM_SUB_TASK_SUMMARY);
        submit();
        assertTextPresent(CUSTOM_SUB_TASK_SUMMARY);
        assertTextPresent("test for sub tasks");

        // All sub-tasks must be deleted before Sub-Tasks can be deactivated
        deleteCurrentIssue();
        deactivateSubTasks();
        assertTextPresent("Enable");
    }

    /**
     * Tests if the 'Create Sub Task' Link is available with the 'Create Issue' permission removed
     */
    public void subTasksWithCreatePermission(String issueKey)
    {
        log("Sub Task Create: Test availability of 'Create Sub Task' link with 'Create Issue' permission.");
        activateSubTasks();
        removeGroupPermission(CREATE_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("create-subtask");

        // Grant 'Create Issue' permission
        grantGroupPermission(CREATE_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkPresent("create-subtask");
        deactivateSubTasks();
    }


    public void subTaskWithNoSummary(String issueKey)
    {
        log("Sub Task Create: Adding sub task without summary");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        // Not setting summary

        // Need to set priority as this is automatically set
        selectOption("priority", minorPriority);

        submit();
        assertTextPresent("Create Sub-Task");
        assertTextPresent("You must specify a summary of the issue.");
        deactivateSubTasks();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions required.
     * Attempts to create a sub task with required fields not filled out and with an invalid assignee
     */
    public void subTaskWithRequiredFields(String issueKey)
    {
        // Set fields to be required
        setRequiredFields();

        log("Sub Task Create: Test the creation of a sub task using required fields");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        setFormElement("summary", "This is a new summary");
        setFormElement("reporter", "");

        submit("Create");

        assertTextPresent("Create Sub-Task");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        // Reset fields to be optional
        resetFields();
        deactivateSubTasks();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions hidden.
     */
    public void subTaskWithHiddenFields(String issueKey)
    {
        // Hide fields
        setHiddenFields(COMPONENTS_FIELD_ID);
        setHiddenFields(AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFields(FIX_VERSIONS_FIELD_ID);

        log("Sub Task Create: Test the creation of a sub task using hidden fields");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        assertTextPresent("Create Sub-Task");

        assertLinkNotPresent("components");
        assertLinkNotPresent("versions");
        assertLinkNotPresent("fixVersions");

        // Reset fields to be optional
        resetFields();
        deactivateSubTasks();
    }


    public void subTaskWithInvalidDueDate(String issueKey)
    {
        log("Sub Task Create: Creating sub task with invalid due date");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        setFormElement("summary", "stuff");
        setFormElement("duedate", "stuff");

        submit("Create");

        assertTextPresent("Create Sub-Task");

        assertTextPresent("You did not enter a valid date. Please enter the date in the format &quot;d/MMM/yy&quot;");
        deactivateSubTasks();
    }

    /**
     * Tests if the Due Date' field is available with the 'Schedule Issue' permission removed
     */
    public void subTaskWithSchedulePermission(String issueKey)
    {
        log("Sub Task Create: Test prescence of 'Due Date' field with 'Schedule Issue' permission.");
        removeGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertTextNotPresent("Due Date");
        deactivateSubTasks();

        // Grant Schedule Issue Permission
        grantGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertTextPresent("Due Date");
        deactivateSubTasks();
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assign Issue' permission removed
     */
    public void subTaskWithAssignPermission(String issueKey)
    {
        log("Sub Task Create: Test ability to specify assignee with 'Assign Issue' permission.");
        removeGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementNotPresent("assignee");
        deactivateSubTasks();

        // Grant Assign Issue Permission
        grantGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementPresent("assignee");
        deactivateSubTasks();
    }

    /**
     * Tests if the 'Reporter' Option is available with the 'Modify Reporter' permission removed
     */
    public void subTaskWithModifyReporterPermission(String issueKey)
    {
        log("Sub Task Create: Test availability of Reporter with 'Modify Reporter' permission.");
        removeGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementNotPresent("reporter");
        deactivateSubTasks();

        // Grant Modify Reporter Permission
        grantGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementPresent("reporter");
        deactivateSubTasks();
    }

    /**
     * Tests if the 'Orignial Estimate' Link is available with Time Tracking activated
     */
    public void subTaskWithTimeTracking(String issueKey)
    {
        log("Sub task Create: Test availability of the original esitmate field with time tracking activated");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementNotPresent("timetracking");
        deactivateSubTasks();

        activateTimeTracking();
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        assertFormElementPresent("timetracking");
        deactivateTimeTracking();
        deactivateSubTasks();
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assignable User' permission removed
     */
    public void subTaskWithUnassignableUser(String issueKey)
    {
        log("Sub Task Create: Attempt to set the assignee to be an unassignable user ...");

        // Remove assignable permission
        removeGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        submit("Create");

        assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        //Restore permission
        grantGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        deactivateSubTasks();
    }

    /**
     //     * Tests if a sub task has its security level automatically allocated
     //     */
//    public void subTaskCreateSubTaskWithSecurity()
//    {
//        log("Sub Task Create; Create a sub task from an issue with a security level");
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-9", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary", SUB_TASK_SUMMARY);
//        submit();
//        assertTextPresent("test 9");
//        assertTextPresent(SUB_TASK_SUMMARY);
//        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
//
//        // delete the sub-task
//        clickLink("delete-issue");
//        getDialog().setWorkingForm("delete_confirm_form");
//        submit();
//
//        deactivateSubTasks();
//        setSecurityLevelToRequried();
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-9", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary", SUB_TASK_SUMMARY);
//        submit();
//
//        assertTextPresent("Step 2 of 2");
//        assertTextPresent("Security Level is required. The &quot;Set Issue Security&quot; permission is required in order to set this field.");
////        assertTextPresent("test 9");
////        assertTextPresent(SUB_TASK_SUMMARY);
////        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
//
//        // delete the issue
////        clickLink("delete-issue");
////        getDialog().setWorkingForm("delete_confirm_form");
////        submit();
//
//        setSecurityLevelToRequried();
//        removeGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        deactivateSubTasks();
//    }
//
    /**
     * Tests if the sub test is moved with the issue to a different project
     */
    public void subTaskMoveIssueWithSubTask(String issueKey)
    {
        log("Sub Task Move: Move issue with a sub task");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        setFormElement("summary", SUB_TASK_SUMMARY);
        submit();
        assertTextPresent(SUB_TASK_SUMMARY);
        assertTextPresent("test for sub tasks");

        // Move parent issue
        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_NEO);
        submit();
        getDialog().setWorkingForm("jiraform");
        submit();
        assertTextPresent("New Value (after move)");
        submit("Move");
        assertTextPresent("test for sub tasks");

        // Check the result
        gotoIssue(issueKey);
        clickLinkWithText(SUB_TASK_SUMMARY);
        assertTextPresent(PROJECT_NEO);

        // restore to orignal settings
        deleteCurrentIssue();
        deactivateSubTasks();

    }

    /**
     * Tests if a user can move a sub task to a different sub task type
     */
    public void subTaskMoveSubTask(String issueKey)
    {
        String subTaskKey;
        log("Sub Task Move; Move a sub task to a different sub task type.");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        setFormElement("summary", SUB_TASK_SUMMARY);
        submit();
        assertTextPresent(SUB_TASK_SUMMARY);
        assertTextPresent("test for sub tasks");

        String text;

        try
        {
            text = getDialog().getResponse().getText();
            int projectIdLocation = text.indexOf(PROJECT_NEO_KEY);
            int endOfIssueKey = text.indexOf("]", projectIdLocation);
            subTaskKey = text.substring(projectIdLocation, endOfIssueKey);
        }
        catch (IOException t)
        {
            fail("Unable to obtain sub-task key");
            return;
        }

        removeGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(subTaskKey);
        assertLinkNotPresent("move-issue");

        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(subTaskKey);
        clickLink("move-issue");
        selectOption("issuetype", CUSTOM_SUB_TASK_TYPE_NAME);
        getDialog().setWorkingForm("jiraform");
        submit();

        assertTextPresent("Step 2 of 3");
        getDialog().setWorkingForm("jiraform");
        submit();

        assertTextPresent("Step 4 of 4");
        submit("Move");

        assertTextPresent(CUSTOM_SUB_TASK_TYPE_NAME);
        assertTextPresent("Details");

        // restore settings
        gotoIssue(subTaskKey);
        deleteCurrentIssue();
        deactivateSubTasks();
    }

//    /**
//     * Tests that field layout schemes can be enforced on sub tasks with required fields
//     */
//    public void subTaskWithFieldSchemeRequired()
//    {
//        log("Sub Task Create: Enforce Sub Tasks on a field layout scheme");
//        activateSubTasks();
//        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
//        clickLink("create-subtask");
//        assertTextPresent("Create Sub-Task Issue");
//        selectOption("issuetype", SUB_TASK_DEFAULT_TYPE);
//        submit();
//        setFormElement("summary","test summary");
//        submit();
//
//        assertTextPresent("Step 2 of 2");
//        assertTextPresent("Component/s is required");
//        assertTextPresent("Affects Version/s is required");
//        assertTextPresent("Fix Version/s is required");
//
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        deactivateSubTasks();
//    }
//
//    /**
//     * Tests that field layout schemes can be enforced on sub tasks with hidden field
//     */
//    public void subTaskWithFieldSchemeHidden()
//    {
//        log("Sub Task Create: Enforce Sub Tasks on a field layout scheme");
//        activateSubTasks();
//        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
//        clickLink("create-subtask");
//        assertTextPresent("Create Sub-Task Issue");
//        selectOption("issuetype", SUB_TASK_DEFAULT_TYPE);
//        submit();
//
//        assertFormElementNotPresent("components");
//        assertFormElementNotPresent("versions");
//        assertFormElementNotPresent("fixVersions");
//
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        deactivateSubTasks();
//    }
}
