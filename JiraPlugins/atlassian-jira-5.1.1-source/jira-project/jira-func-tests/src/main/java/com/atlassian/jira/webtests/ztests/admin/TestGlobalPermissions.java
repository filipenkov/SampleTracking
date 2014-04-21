package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestGlobalPermissions extends JIRAWebTest
{
    public TestGlobalPermissions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testCanRemoveAnyoneFromJiraUsers() throws Exception
    {
        restoreData("TestGlobalPermssionsJRA13577.xml");
        gotoGlobalPermissions();

        clickLink("global_permissions");
        assertTextPresent("Global Permissions");
        assertTextPresent("JIRA Permissions");

        clickLink("del_1_");
        assertTextPresent("Delete Global Permission");
        assertTextSequence(new String[] {
                "Are you sure you want to delete",
                "Anyone",
                "group from the",
                "JIRA Users",
                "permission?"
        });

        submit("Delete");
        assertTextPresent("Global Permissions");
        assertTextPresent("JIRA Permissions");
        assertLinkNotPresent("del_1_");
    }

    public void testErrorOnSysAdminDelete()
    {
        //shouldn't be able to delte admin group as there is only one admin group
        gotoGlobalPermissions();
        assertTextPresent("JIRA System Administrators");
        assertTextPresent("jira-administrators");
        clickLink("del_44_jira-administrators");
        assertTextPresent("You cannot delete this permission. You are not a member of any of the other system administration permissions.");
    }

    public void testAddThenDeletePermission()
    {
        //Check that you can add a permission then delete it.
        gotoGlobalPermissions();
        assertTextPresent("Browse Users");
        assertTextPresent("jira-developers");

        //Check for the delete link
        assertLinkPresent("del_27_jira-developers");
        selectOption("permType", "Browse Users");
        selectOption("groupName", "Anyone");
        submit("Add");                                       //add the group
        assertLinkPresent("del_27_");

        //Delete the group
        clickLink("del_27_");
        assertTextPresent("Delete Global Permission");
        assertTextPresent("Are you sure you want to delete the");   //check for confirmation
        assertTextPresent("Anyone");
        assertTextPresent("group from the");
        assertTextPresent("Browse Users");
        assertTextPresent("permission?");
        submit("Delete");

        // Make sure the delete link for the Anyone permission does not exist
        assertLinkNotPresent("del_27_");
    }

    public void testAddNoPermission()
    {
        //should be prompted to select a permission from the dropdown
        gotoGlobalPermissions();
        submit("Add");
        assertTextPresent("You must select a permission");

        clickOnAdminPanel("admin.globalsettings", "global_permissions");
        assertTextNotPresent("You must select a permission");
        selectOption("permType", "Please select a permission");
        submit("Add");
        assertTextPresent("You must select a permission");
    }

    public void testNotAllowedToAddAnyoneToJiraUsers()
    {
        gotoGlobalPermissions();
        assertTextPresent("JIRA Administrators");
        assertTextPresent("jira-administrators");
        assertCannotAddAnyoneToJiraUsers();
        assertCannotAddAnyoneToJiraAdministrators();
        assertCannotAddAnyoneToSystemAdministrators();
    }


    private void assertCannotAddAnyoneToJiraUsers()
    {
        selectOption("permType", "JIRA Users");
        selectOption("groupName", "Anyone");
        submit("Add");
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }

    /**
     *  JRA-26627 - no longer allow anyone to be added to the Administrators group
     *
     */
    private void assertCannotAddAnyoneToJiraAdministrators()
    {
        selectOption("permType", "JIRA Administrators");
        selectOption("groupName", "Anyone");
        submit("Add"); //add the group
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }

    private void assertCannotAddAnyoneToSystemAdministrators()
    {
        selectOption("permType", "JIRA System Administrators");
        selectOption("groupName", "Anyone");
        submit("Add"); //add the group
        assertions.getJiraFormAssertions().assertFieldErrMsg("The group 'Anyone' is not allowed to be added to the permission");
    }


    public void testSystemAdminNotVisibleToNonAdmins()
    {
        try
        {
            // restore data that has the admin user not as a sys admin
            restoreData("TestWithSystemAdmin.xml");

            gotoGlobalPermissions();

            // Confirm that we are not able to see the sys admin stuff
            gotoGlobalPermissions();
            assertTextNotPresent("<b>JIRA System Administrators</b>");

            // Try to add something we are not allowed to add
            final String addUrl = page.addXsrfToken("/secure/admin/jira/GlobalPermissions.jspa?action=add&permType=44&groupName=jira-users");
            tester.gotoPage(addUrl);
            assertTextPresent("You can not add a group to a global permission you do not have permission to see.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    public void testAdminCannotDeleteSysAdminGroups()
    {
        try
        {
            // restore data that has the admin user not as a sys admin
            restoreData("TestWithSystemAdmin.xml");

            final String removeUrl = page.addXsrfToken("/secure/admin/jira/GlobalPermissions.jspa?permType=44&action=del&groupName=jira-sys-admins");
            tester.gotoPage(removeUrl);

            assertTextPresent("Only system administrators can delete groups from the system administrator permission.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    public void testFilterPermsHaveCorrectVisibility()
    {
        gotoGlobalPermissions();

        assertTextPresent("Create Shared Objects");
        assertTextPresent("Manage Group Filter Subscriptions");
    }

    public void testRemoveGroupDoesntExist()
    {
        restoreData("TestRemoveGroupDoesntExist.xml");
        gotoAdmin();

        //first check the group doesn't exist.
        tester.clickLink("group_browser");
        tester.assertTextNotPresent("Stuff");

        //now check it's present in the global permissions.
        tester.clickLink("global_permissions");
        assertTextPresent("Stuff");

        //try to remove the group stuff.  It doesn't exist any longer.
        tester.clickLink("del_1_Stuff");
        tester.assertTextPresent("Delete Global Permission");
        assertTextSequence(new String[] {"Are you sure you want to delete the", "Stuff", "group from the", "JIRA Users", "permission"});
        tester.submit("Delete");

        assertTextPresent("Global Permissions");
        assertTextNotPresent("Stuff");

        //also try removing a group that's not a member of a permission which should throw an error.
        final String removeUrl = page.addXsrfToken("secure/admin/jira/GlobalPermissions.jspa?groupName=bad&permType=44&action=confirm");
        tester.gotoPage(removeUrl);
        assertions.getJiraFormAssertions().assertFormErrMsg("Group 'bad' cannot be removed from permission "
                + "'JIRA System Administrators' since it is not a member of this permission.");
    }

    private void gotoGlobalPermissions()
    {
        clickOnAdminPanel("admin.globalsettings", "global_permissions");
        assertTextPresent("Global Permissions");
    }

}
