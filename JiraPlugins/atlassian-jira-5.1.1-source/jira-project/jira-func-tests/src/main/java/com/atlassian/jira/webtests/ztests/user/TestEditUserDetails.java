package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.commons.lang.StringUtils;

/**
 * Checks the set password and edit user details actions.
 *
 * @since v3.12
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestEditUserDetails extends JIRAWebTest
{

    public TestEditUserDetails(String name)
    {
        super(name);
    }

    public void testAdminCannotSetSysAdminPassword()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(SYS_ADMIN_USERNAME);
            assertLinkNotPresentWithText("Set Password");
            assertTextPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");

            // hack url
            gotoPage(page.addXsrfToken("/secure/admin/user/SetPassword.jspa?name=root&password=root&confirm=root"));
            assertTextPresent("Error");
            assertions.getJiraFormAssertions().assertFormErrMsg("Must be a System Administrator to reset a System Administrator's password.");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testAdminCannotEditSysAdminDetails()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(SYS_ADMIN_USERNAME);
            assertLinkNotPresentWithText("Edit Details");
            assertTextPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");

            // hack url
            gotoPage(page.addXsrfToken("/secure/admin/user/EditUser.jspa?editName=root&fullName=rooty&email=root@example.com"));
            assertTextPresent("Error");
            assertTextPresent("Only System Administrators can edit other System Administrators details.");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testSysAdminCanEditSysAdmin()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            login(SYS_ADMIN_USERNAME);
            addUser("anothersysadmin");
            addUserToGroup("anothersysadmin", "jira-sys-admins");

            navigateToUser("anothersysadmin");
            assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            clickLinkWithText("Edit Details");
            setFormElement("fullName", "Rooty");
            submit("Update");
            assertTextPresent("Rooty");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testSysAdminCanSetSysAdminPassword()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            login(SYS_ADMIN_USERNAME);

            addUser("anothersysadmin", "something", "Another User", "another@example.com");
            addUserToGroup("anothersysadmin", "jira-sys-admins");

            navigateToUser("anothersysadmin");
            assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            clickLinkWithText("Set Password");
            setFormElement("password", "another");
            setFormElement("confirm", "another");
            submit("Update");

            login("anothersysadmin", "another");
            assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }


    public void testAdminCanEditNormalUser()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(FRED_USERNAME);
            assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            clickLinkWithText("Edit Details");
            setFormElement("fullName", "Freddy Kruger");
            submit("Update");
            assertTextPresent("Freddy Kruger");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testAdminCanSetNormalUsersPassword()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(FRED_USERNAME);
            assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            clickLinkWithText("Set Password");
            setFormElement("password", "another");
            setFormElement("confirm", "another");
            submit("Update");

            login(FRED_USERNAME, "another");
            assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");

        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testUserNameWithScriptTags()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");

            final String value = "\"xss user'bad";
            final String valueEncoded = "&quot;xss user&#39;bad";

            addUser(value, "password", value, "email@email.com");
            navigateToUser(value);

            clickLinkWithText("Set Password");
            setFormElement("password", "another");
            setFormElement("confirm", "another");
            submit("Update");
            assertTextPresent(valueEncoded);
            assertTextNotPresent(value);

            login(value, "another");
            assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    // JRADEV-776: testing the Add User form in admin section. Similar tests could be done for sign up and edit profile.
    public void testFieldsExceed255()
    {
        try
        {
            restoreBlankInstance();

            addUser(StringUtils.repeat("abcdefgh", 32), "password", StringUtils.repeat("ABCDEFGH", 32), StringUtils.repeat("x", 246) + "@email.com");
            assertTextPresent("The username must not exceed 255 characters in length.");
            assertTextPresent("The full name must not exceed 255 characters in length.");
            assertTextPresent("The email address must not exceed 255 characters in length.");

            addUser(StringUtils.repeat("abcdefgh", 32).substring(0, 255), "password", StringUtils.repeat("ABCDEFGH", 32).substring(0, 255), (StringUtils.repeat("x", 246) + "@email.com").substring(0, 255));
            assertTextNotPresent("The username must not exceed 255 characters in length.");
            assertTextNotPresent("The full name must not exceed 255 characters in length.");
            assertTextNotPresent("The email address must not exceed 255 characters in length.");

            logout();
            login(StringUtils.repeat("abcdefgh", 32).substring(0, 255), "password");
            assertTextPresent(StringUtils.repeat("ABCDEFGH", 32).substring(0, 255));
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    public void testAutocompleteIsOff()
    {
        try
        {
            final TextAssertions text = new TextAssertionsImpl();

            restoreData("TestWithSystemAdmin.xml");

            navigateToUser(FRED_USERNAME);
            clickLinkWithText("Set Password");

            XPathLocator xpathPassword = new XPathLocator(tester, "//*[@name=\"password\"]");
            text.assertRegexMatch(xpathPassword.getHTML(), "autocomplete=[ ]*\"off\"[ ]*");

            XPathLocator xpathConfirm = new XPathLocator(tester, "//*[@name=\"confirm\"]");
            text.assertRegexMatch(xpathConfirm.getHTML(), "autocomplete=[ ]*\"off\"[ ]*");
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
