package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.PERMISSIONS, Category.SCHEMES })
public class TestPermissionSchemes extends JIRAWebTest
{
    public TestPermissionSchemes(String name)
    {
        super(name);
    }

    private static final String MOVE_TABLE_ID = "move_confirm_table";
    private static final int MOVE_TABLE_FIELD_NAME_COLUMN_INDEX = 0;
    private static final int MOVE_TABLE_OLD_VALUE_COLUMN_INDEX = 1;
    private static final int MOVE_TABLE_NEW_VALUE_COLUMN_INDEX = 2;

    public void testPermissionSchemes()
    {
        restoreBlankInstance();
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project '" + PROJECT_HOMOSAP + "' exists");
        }
        else
        {
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        }

        if (projectExists(PROJECT_NEO))
        {
            log("Project '" + PROJECT_NEO + "' exists");
        }
        else
        {
            addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        }

        if (projectExists(PROJECT_MONKEY))
        {
            log("Project '" + PROJECT_MONKEY + "' exists");
        }
        else
        {
            addProject(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ADMIN_USERNAME);
        }

        if (permissionSchemeExists(PERM_SCHEME_NAME))
        {
            deletePermissionScheme(PERM_SCHEME_NAME);
        }

        resetFields();
        deleteAllIssuesInAllPages();
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 1", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description for permission schemes", null, null, null);

        permissionSchemesCreateScheme();
        permissionSchemeAssociateScheme();
        permissionSchemeAddDuplicateScheme();
        permissionSchemeAddInvalidScheme();
        permissionSchemesMoveIssueToProjectWithAssignablePermission(issueKeyNormal);
        permissionSchemesMoveIssueToProjectWithAssignPermission();
        permissionSchemesMoveIssueWithSchedulePermission(issueKeyNormal);
        permissionSchemesMoveIssueToProjectWithCreatePermission(issueKeyNormal);

        permissionSchemeDeleteScheme();

        deleteIssue(issueKeyNormal);
    }

    public void testProjectRolePermissionScheme()
    {
        logSection("Test to check that project role permission scheme works");
        restoreData("TestSchemesProjectRoles.xml");
        gotoPermissionSchemes();
        clickLinkWithText(DEFAULT_PERM_SCHEME);
        assertTextPresent("Edit Permissions &mdash; " + DEFAULT_PERM_SCHEME);

        clickLink("add_perm_" + MOVE_ISSUE);

        assertTextPresent("Choose a project role");

        checkCheckbox("type", "projectrole");
        selectOption("projectrole", "test role");
        submit();
        assertTextPresent("(test role)");
    }

    public void permissionSchemesCreateScheme()
    {
        log("Permission Schemes: Create a new permission scheme");
        createPermissionScheme(PERM_SCHEME_NAME, PERM_SCHEME_DESC);
        assertLinkPresentWithText(PERM_SCHEME_NAME);
        assertTextPresent(PERM_SCHEME_DESC);
    }

    public void permissionSchemeDeleteScheme()
    {
        log("Permission Schemes:Delete a permission scheme");
        deletePermissionScheme(PERM_SCHEME_NAME);
        assertLinkNotPresentWithText(PERM_SCHEME_NAME);
    }

    public void permissionSchemeAssociateScheme()
    {
        log("Permission Schemes: Associate a permission scheme with a project");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        assertions.assertNodeByIdHasText("project-config-permissions-scheme-name", PERM_SCHEME_NAME);
        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Create a scheme with a duplicate name
     */
    public void permissionSchemeAddDuplicateScheme()
    {
        log("Permission Schemes: Attempt to create a scheme with a duplicate name");
        createPermissionScheme(PERM_SCHEME_NAME, "");
        assertTextPresent("Add Permission Scheme");
        assertTextPresent("A Scheme with this name already exists.");
    }

    /**
     * Create a scheme with an invalid name
     */
    public void permissionSchemeAddInvalidScheme()
    {
        log("Permission Schemes: Attempt to create a scheme with an invalid name");
        createPermissionScheme("", "");
        assertTextPresent("Add Permission Scheme");
        assertTextPresent("Please specify a name for this Scheme.");
    }

    /**
     * Tests the ability to move an issue to a project WITHOUT the 'Assignable User' Permission
     */
    private void permissionSchemesMoveIssueToProjectWithAssignablePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Assign Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        setUnassignedIssuesOption(true);
        // Give jira-users 'Create' Permission
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        // give jira-developers 'Assignable Users' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.DEVELOPERS);

        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_NEO);
        submit();
        assertTextPresent("Step 3 of 4");

        assertTextNotPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        removeGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_NEO);
        submit();
        assertTextPresent("Step 3 of 4");
        setWorkingForm("jiraform");
        submit();

        assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);


        setUnassignedIssuesOption(false);
        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_NEO);
        submit();
        assertTextPresent("Step 3 of 4");
        assertTextNotPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        removeGroupPermission(PERM_SCHEME_NAME,CREATE_ISSUE, Groups.USERS);
        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Test that assignee is autoassigned for move issue operation if user does not have 'Assign' permission
     */
    public void permissionSchemesMoveIssueToProjectWithAssignPermission()
    {
        log("Move Operation: Test that assignee is autoassigned if assignee does not have assign permission");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        // Give jira-users 'Create' Permission
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        // Give jira-admin 'Assign' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGN_ISSUE, Groups.ADMINISTRATORS);
        // Give jira-users 'Assignable Users' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.USERS);
        // Give jira-users 'Browse Project' Permission
        grantGroupPermission(PERM_SCHEME_NAME, BROWSE, Groups.USERS);
        grantGroupPermission(PERM_SCHEME_NAME, MOVE_ISSUE, Groups.USERS);

        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        String issueKey;
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            issueKey = addIssue(PROJECT_NEO, PROJECT_NEO_KEY, "Bug", "test 1", "Minor", null, null, null, BOB_FULLNAME, "Original Assignee - Bob\n New Assignee - " + ADMIN_FULLNAME, "This issue should be moved and auto-assigned to " + ADMIN_FULLNAME, null, null, null);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }

        removeGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);

        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_HOMOSAP);
        submit();
        assertTextPresent("Step 3 of 4");

        setWorkingForm("jiraform");
        submit();
        try
        {
            WebTable fieldTable = getDialog().getResponse().getTableWithID(MOVE_TABLE_ID);
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, MOVE_TABLE_FIELD_NAME_COLUMN_INDEX);
                if (field.indexOf("Assignee") > -1)
                {
                    String oldValue = fieldTable.getCellAsText(i, MOVE_TABLE_OLD_VALUE_COLUMN_INDEX);
                    String newValue = fieldTable.getCellAsText(i, MOVE_TABLE_NEW_VALUE_COLUMN_INDEX);
                    assertTrue(oldValue.indexOf(BOB_FULLNAME) > -1);
                    assertTrue(newValue.indexOf(ADMIN_FULLNAME) > -1);

                    grantGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
                    removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, ASSIGN_ISSUE, Groups.ADMINISTRATORS);
                    removeGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, BROWSE, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, MOVE_ISSUE, Groups.USERS);
                    associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
                    deleteIssue(issueKey);
                    deleteUser(BOB_USERNAME);
                    return;
                }
            }
            fail("Cannot find field chamge for 'Assignee'");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + MOVE_TABLE_ID + "'.");
            e.printStackTrace();
        }
    }

    /**
     * Test the abilty to move an issue with 'Schedule Issues' Permission and 'Due Date' Required
     */
    public void permissionSchemesMoveIssueWithSchedulePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Schedule Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        removeGroupPermission(PERM_SCHEME_NAME,SCHEDULE_ISSUE, Groups.DEVELOPERS);
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        setDueDateToRequried();

        gotoIssue(issueKey);
        clickLink("move-issue");
        selectOption("pid", PROJECT_NEO);
        submit();

        assertTextPresent("Step 3 of 4");

        setWorkingForm("jiraform");
        submit();
        assertTextPresent("&quot;Due Date&quot; field is required and you do not have permission to Schedule Issues for project &quot;" + PROJECT_NEO + "&quot;.");

        // restore settings
        resetFields();
        grantGroupPermission(PERM_SCHEME_NAME, SCHEDULE_ISSUE, Groups.DEVELOPERS);
        removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Tests the ability to move an issue to a project WITHOUT the 'Create Issue' Permission
     */
    public void permissionSchemesMoveIssueToProjectWithCreatePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Create Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);

        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        clickLink("move-issue");
        assertOptionsEqual("pid", new String[] {PROJECT_HOMOSAP, PROJECT_NEO, PROJECT_HOMOSAP, PROJECT_MONKEY, PROJECT_NEO});
        removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        clickLink("move-issue");
        assertOptionsEqual("pid", new String[] {PROJECT_HOMOSAP, PROJECT_HOMOSAP, PROJECT_MONKEY});

        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }
}

