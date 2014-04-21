package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.USERS_AND_GROUPS })
public class TestDeleteUserAndPermissions extends JIRAWebTest
{
    /**
     * This file import contains two projects. One project has a specific permission scheme that only allows user fred
     * to perform any instructions. Both fred and admin have public filters set up with each subscribing to each other's
     * filters. There are two issues created by fred, one in each of the two projects
     */
    private static final String TWO_PROJECTS_WITH_SUBSCRIPTIONS = "TestDeleteUserAndPermissions.xml";
    
    public void setUp()
    {
        super.setUp();
        restoreData(TWO_PROJECTS_WITH_SUBSCRIPTIONS);
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("jira.no.frother.reporter.field");
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        super.tearDown();
    }

    public TestDeleteUserAndPermissions(String name)
    {
        super(name);
    }

    //------------------------------------------------------------------------------------------------------------ Tests

    public void testDeleteUserNotPossibleWithAssignedIssue()
    {
        logSection("Test Delete User Not Possible With Assigned Issue");

        log("Making sure that you can't see the other issue");
        getNavigation().issueNavigator().displayAllIssues();
        submit("show");
        assertTextPresent("Seen issue");
        assertTextNotPresent("Unseen issue");

        log("Ensuring that you're unable to delete fred and that the correct number of issues are shown");
        gotoAdmin();
        clickLink("user_browser");
        clickLink(FRED_USERNAME);
        clickLink("deleteuser_link");
        assertTextPresent("This user cannot be deleted at this time because there are issues assigned to them");
        assertTextPresentBeforeText("Assigned Issues", "1");
        assertTextPresentBeforeText("Reported Issues", "2");
        assertTextPresentBeforeText("1", "Reported Issues");
    }


    public void testDeletUserRemoveFromPermissionAndNotificationSchemes()
    {
        logSection("Test delete user with shared filters");

        assertFredHasPermissionsAssigned();

        // assert Fred is in issue security scheme
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Test Issue Security Scheme");
        assertTextPresent(FRED_USERNAME);

        // assert Fred is in Default Notification Scheme
        gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");
        assertTextPresent(FRED_USERNAME);
        assertLinkPresent("del_10050");

        // add permissions to admin so we can edit MKY-1
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("10000_edit");
        clickLink("add_perm_12");
        checkCheckbox("type", "user");
        setFormElement("user", ADMIN_USERNAME);
        submit(" Add ");
        clickLink("add_perm_10");
        checkCheckbox("type", "user");
        setFormElement("user", ADMIN_USERNAME);
        submit(" Add ");
        clickLink("add_perm_30");
        checkCheckbox("type", "user");
        setFormElement("user", ADMIN_USERNAME);
        submit(" Add ");

        // remove fred from issue
        gotoIssue("HSP-1");
        clickLink("edit-issue");
        setFormElement("reporter", ADMIN_USERNAME);
        submit("Update");
        gotoIssue("MKY-1");
        clickLink("edit-issue");
        setFormElement("reporter", ADMIN_USERNAME);
        selectOption("assignee", ADMIN_FULLNAME);
        submit("Update");

        log("Deleting Fred");
        gotoAdmin();
        clickLink("user_browser");
        clickLink(FRED_USERNAME);
        clickLink("deleteuser_link");
        assertTextPresent("Delete User");
        submit("Delete");
        assertions.assertNodeHasText(new CssLocator(tester, "#content > header"), "Users");
        assertTextNotPresent(FRED_USERNAME);

        assertFredHasNoPermissionsAssigned();

        // assert Fred isn't in issue security scheme
        gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Test Issue Security Scheme");
        assertTextNotPresent(FRED_USERNAME);

        // assert Fred isn't in Default Notification Scheme
        gotoAdmin();
        clickLink("notification_schemes");
        clickLinkWithText("Default Notification Scheme");
        assertTextNotPresent(FRED_USERNAME);
        assertLinkNotPresent("del_10050");
    }

    public void testAdminCannotDeleteSysadmin()
    {
        restoreBlankInstance();

        try
        {
            // create a sysadmin to attempt to delete (also not a project lead like "admin")
            addUser("sysadmin2");
            addUserToGroup("sysadmin2", "jira-administrators"); // should have system administrator permission now

            // create a normal admin (non system admin)
            addUser("nonsystemadmin");
            createGroup("nonsystemadmins");
            addUserToGroup("nonsystemadmin", "nonsystemadmins");
            grantGlobalPermission(ADMINISTER, "nonsystemadmins");

            login("nonsystemadmin");

            gotoAdmin();
            clickLink("user_browser");
            assertLinkNotPresent("deleteuser_link_sysadmin2");
            // Hack the url
            gotoPage(page.addXsrfToken("/secure/admin/user/DeleteUser!default.jspa?returnUrl=UserBrowser.jspa&name=sysadmin2"));
            assertTextPresent("As a user with JIRA Administrators permission, you cannot delete users with JIRA System Administrators permission.");
            assertButtonNotPresent("delete_submit");

            // try to hack the URL for getting to the form to delete the user
            gotoPage(page.addXsrfToken("/secure/admin/user/DeleteUser.jspa?returnUrl=UserBrowser.jspa&name=sysadmin2&confirm=true"));
            assertTextPresent("As a user with JIRA Administrators permission, you cannot delete users with JIRA System Administrators permission.");

            gotoAdmin();
            clickLink("user_browser");
            assertLinkPresent("sysadmin2"); // check user is still in user list

        }
        finally
        {
            login(ADMIN_USERNAME);
        }
    }

    private void assertFredHasPermissionsAssigned()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
        assertTextNotPresent(FRED_USERNAME);
        assertTextNotPresent("Fred");

        gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Fred's scheme");
        assertLinkPresent("del_perm_23_fred");
        assertLinkPresent("del_perm_10_fred");
        assertLinkPresent("del_perm_11_fred");
        assertLinkPresent("del_perm_12_fred");
        assertLinkPresent("del_perm_28_fred");
        assertLinkPresent("del_perm_25_fred");
        assertLinkPresent("del_perm_17_fred");
        assertLinkPresent("del_perm_14_fred");
        assertLinkPresent("del_perm_18_fred");
        assertLinkPresent("del_perm_30_fred");
        assertLinkPresent("del_perm_15_fred");
        assertLinkPresent("del_perm_36_fred");
        assertLinkPresent("del_perm_16_fred");
        assertLinkPresent("del_perm_20_fred");
        assertLinkPresent("del_perm_21_fred");
        assertLinkPresent("del_perm_19_fred");
        assertLinkPresent("del_perm_38_fred");
        assertLinkPresent("del_perm_29_fred");
        assertLinkPresent("del_perm_31_fred");
        assertLinkPresent("del_perm_32_fred");
        assertLinkPresent("del_perm_26_fred");
    }

    private void assertFredHasNoPermissionsAssigned()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
        assertTextNotPresent(FRED_USERNAME);
        assertTextNotPresent("Fred");

        gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Fred's scheme");
        assertTextNotPresent(FRED_USERNAME);
        for (int i = 0; i < 100; i++)
        {
            assertLinkNotPresent("del_perm_" + i + "_fred");
        }
    }

}
