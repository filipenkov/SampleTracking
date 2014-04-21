package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import junit.framework.AssertionFailedError;
import org.junit.Ignore;

/**
 * Functional test case for user management pages.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserManagement extends JIRAWebTest
{
    public static final String JIRA_DEVELOPERS_GROUP_NAME = "jira-developers";
    public static final String JIRA_ADMINISTRATORS_GROUP_NAME = "jira-administrators";
    public static final String ISO_8859_1_JAVA_CHARS = "!@?[]~'{};&abc123\u00a3 \u00a9 \u00e5 \u00eb \u00f8 \u00e2 \u00ee \u00f4 \u00fd \u00ff \u00fc";
    public static final String ISO_8859_1_HTML_CHARS = "!@?[]~'{};&amp;abc123&pound; &copy; &aring; &euml; &oslash; &acirc; &icirc; &ocirc; &yacute; &yuml; &uuml;";
    public static final String NON_ISO_8859_1_CHARACTERS = "\uFFFF???";
    private static final String DUPLICATE_GROUP_NAME = "duplicate_group";
    public static final String NON_ISO_8859_1_CHAR = "\u00e4";

    public TestUserManagement(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testUserManagement()
    {
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        if (getDialog().isLinkPresentWithText(BOB_USERNAME))
        {
            deleteUser(BOB_USERNAME);
        }
        //the following tests are dependant on each other. (eg. all require user Bob created from createUser()) 
        createUser();
        createValidGroup();
        createInvalidUsers();
        createInvalidGroups();
        addUserToGroup();
        loginWithNewUser();
        removeUserFromGroup();
        setUserPassword();
        deleteUser();
        loginWithInvalidUser();
    }

    /**
     * Test that enabling the "external user management" hides all but the "Project Roles" operation.
     */
    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testUserBrowserOperationsVisibility()
    {
        toggleExternalUserManagement(false);
        //check that the operation links are visible
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkPresent("editgroups_admin");
        assertLinkPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkPresent("edituser_link_admin");
        assertLinkPresent("deleteuser_link_admin");
        assertLinkPresent("deleteuser_link_fred");

        //enable external user management
        toggleExternalUserManagement(true);
        //check that only the view project roles operation is available
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkNotPresent("editgroups_admin");
        assertLinkNotPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkNotPresent("edituser_link_admin");
        assertLinkNotPresent("deleteuser_link_admin");
        assertLinkNotPresent("deleteuser_link_fred");

        //disable external user management
        toggleExternalUserManagement(false);
        //check that the operation links are back to default
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkPresent("editgroups_admin");
        assertLinkPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkPresent("edituser_link_admin");
        assertLinkPresent("deleteuser_link_admin");
        assertLinkPresent("deleteuser_link_fred");
    }

    // These two methods do not work. There seems to be an issue with httpunit where the character \u00e4 is
    // escaped in a URL as %C3%A4, but when the link is clicked via httpunit it gets sent across as
    // %C3%83%C2%A4. This means that we can not correctly browse to the user page to delete the user with this
    // username. The same goes for the createNonISOGroup method.
    public void createNonISOUser()
    {
        addUser(NON_ISO_8859_1_CHAR + BOB_USERNAME, BOB_PASSWORD, "Non-ISO-8859-1 Characters", BOB_EMAIL);
        assertTextPresent("&auml;bob");
        deleteUser(NON_ISO_8859_1_CHAR + BOB_USERNAME);
    }

    public void createNonISOGroup()
    {
        //create group with non-ISO-8859-1 characters
        addGroup(NON_ISO_8859_1_CHAR + BOB_USERNAME);
        assertTextPresent(NON_ISO_8859_1_CHAR + BOB_USERNAME);
        removeGroup(NON_ISO_8859_1_CHAR + BOB_USERNAME);
    }

    public void createInvalidUsers()
    {
        log("Testing User Creation Validation");
        addUser("", BOB_PASSWORD, "No Username", BOB_EMAIL);
        assertTextPresent("You must specify a username.");

        addUser("Bob", BOB_PASSWORD, "Capital Letters Used", BOB_EMAIL);
        assertTextPresent("The username must be all lowercase.");

        addUser(BOB_USERNAME, BOB_PASSWORD, "duplicate_user", BOB_EMAIL);
        addUser(BOB_USERNAME, BOB_PASSWORD, "duplicate_user", BOB_EMAIL);
        assertTextPresent("A user with that username already exists.");

        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, "");
        assertTextPresent("You must specify an email address.");
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, "asf.com");
        assertTextPresent("You must specify a valid email address.");
    }

    public void createInvalidGroups()
    {
        log("Testing Group Creation Validation");
        //create group with already existing group name
        createGroup(DUPLICATE_GROUP_NAME);
        addGroup(DUPLICATE_GROUP_NAME);
        assertTextPresent("A group or user with this name already exists.");

        removeGroup(DUPLICATE_GROUP_NAME);
    }

    private void addGroup(String groupName)
    {
        clickOnAdminPanel("admin.usersgroups", "group_browser");
        setFormElement("addName", groupName);
        submit();
    }

    public void createUser()
    {
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        assertTextPresent("User: " + BOB_FULLNAME);
        assertTextPresentBeforeText("Username:", BOB_USERNAME);
        assertTextPresentBeforeText("Email", BOB_EMAIL);

        //create a user with valid ISO-8859-1 chracters
//        addUser(ISO_8859_1_JAVA_CHARS, "", "valid user", EMAIL_BOB);
//        assertTextPresent("User: " + "valid user");
//        assertTextPresentBeforeText("Username:", ISO_8859_1_JAVA_CHARS);
//        assertTextPresentBeforeText("Email", EMAIL_BOB);
//        deleteUser(ISO_8859_1_JAVA_CHARS);
    }

    public void createValidGroup()
    {
        createGroup("Valid Group");
        removeGroup("Valid Group");

        // NOTE: this should be commented in when the issue with httpunit handling i18n characters correctly is
        // solved.
//        createGroup("group"+ISO_8859_1_JAVA_CHARS);
//        removeGroup("group"+ISO_8859_1_JAVA_CHARS);
    }

    public void addUserToGroup()
    {
        addUserToGroup(BOB_USERNAME, JIRA_DEVELOPERS_GROUP_NAME);
        addUserToGroup(BOB_USERNAME, JIRA_ADMINISTRATORS_GROUP_NAME);
    }

    public void loginWithNewUser()
    {
        // log out from default login fired in setUp()
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        assertRedirectPath(getEnvironmentData().getContext() + "/secure/Dashboard.jspa");
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void removeUserFromGroup()
    {
        removeUserFromGroup(BOB_USERNAME, JIRA_ADMINISTRATORS_GROUP_NAME);
    }

    public void setUserPassword()
    {
        String NEW_PASSWORD = "new";
        String DIFFERENT_PASSWORD = "diff";

        //check set user password validation
        navigateToUser(BOB_USERNAME);
        assertTextPresentBeforeText("User:", BOB_FULLNAME);
        clickLinkWithText("Set Password");
        assertTextPresentBeforeText("Set Password:", BOB_FULLNAME);

        //error validation case 1 (empty input)
        setFormElement("password", "");
        setFormElement("confirm", "");
        submit("Update");
        assertTextPresent("You must specify a password");

        //error validation case 2 (only one of the fields entered)
        setFormElement("password", "");
        setFormElement("confirm", NEW_PASSWORD);
        submit("Update");
        assertTextPresent("You must specify a password");
        setFormElement("password", NEW_PASSWORD);
        setFormElement("confirm", "");
        submit("Update");
        assertTextPresent("The two passwords entered do not match.");

        //error validation case 3 (mismatching password)
        setFormElement("password", NEW_PASSWORD);
        setFormElement("confirm", DIFFERENT_PASSWORD);
        submit("Update");
        assertTextPresent("The two passwords entered do not match.");

        //successful validation and change
        setFormElement("password", NEW_PASSWORD);
        setFormElement("confirm", NEW_PASSWORD);
        submit("Update");
        assertTextPresentBeforeText("Password for user " + BOB_USERNAME + " has successfully been set", BOB_FULLNAME);

        //check that the new password has been set for the user (ie. logout and login with the user)
        logout();
        navigation.loginAttempt(BOB_USERNAME, BOB_PASSWORD);
        assertTextPresent("Sorry, your username and password are incorrect - please try again.");
        navigation.login(BOB_USERNAME, NEW_PASSWORD);

        XPathLocator currentUserName = new XPathLocator(tester, "//li[@id='header-details-user']/a");
        assertEquals(BOB_FULLNAME, currentUserName.getText());

        //log back in as admin for subsequent tests
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void deleteUser()
    {
        deleteUser(BOB_USERNAME);
        assertTextPresent("UserBrowser");
        assertTextPresent("Displaying users");

        // Restore user Bob for later use
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
    }

    public void testDeleteUserProjectLead()
    {
        restoreData("TestUserManagement.xml");
        navigateToUser("detkin");
        clickLink("deleteuser_link");
        assertTextPresent("This user cannot be deleted at this time because there are issues assigned to them, they have reported issues, or they are currently the lead of a project.");
        assertTextPresent("Another Project");
        assertTextPresent("Project 3");
        assertTextPresent("6 projects lead");
    }

    public void testDeleteUserComponentLead()
    {
        restoreData("TestUserManagementComponentLead.xml");
        navigateToUser("detkin");
        clickLink("deleteuser_link");

        // there are six components in the imported XML file
        final int NUMBER_OF_COMPONENTS = 6;
        int count = 0;
        for (int i = 1; i <= NUMBER_OF_COMPONENTS; i++)
        {
            try
            {
                assertTextPresent("comp " + i);
                count++;
            }
            catch (AssertionFailedError e)
            {
                // do nothing, not all components are shown and the order is not guaranteed
            }
        }

        // number of displayed components is same
        assertTrue(count == NUMBER_OF_COMPONENTS);

        // this message appears if there are more components than displayed
        assertTextPresent(NUMBER_OF_COMPONENTS + " components lead");

        assertSubmitButtonPresent("Delete");
    }

    public void loginWithInvalidUser()
    {
        // log out from default login fired in setUp()
        logout();
        navigation.loginAttempt(BOB_USERNAME, null);
        assertTextPresent("Sorry, your username and password are incorrect - please try again.");
    }
}
