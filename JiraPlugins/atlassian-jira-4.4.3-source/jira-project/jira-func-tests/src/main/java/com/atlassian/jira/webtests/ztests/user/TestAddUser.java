package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.ztests.tpm.ldap.UserDirectoryTable;
import com.meterware.httpunit.WebLink;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestAddUser extends EmailFuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testEmptyData()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");

        tester.setFormElement("username", "");
        tester.setFormElement("fullname", "");
        tester.setFormElement("email", "");
        tester.submit("Create");

        tester.assertTextPresent("Create New User");
        tester.assertTextPresent("You must specify a username.");
        tester.assertTextPresent("You must specify a full name.");
        tester.assertTextPresent("You must specify an email address.");
    }

    public void testCreateDuplicateUser()
    {
        checkSuccessUserCreate();

        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");
        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit("Create");
        tester.assertTextPresent("A user with that username already exists.");
    }

    public void testCreateUserSuccess()
    {
        checkSuccessUserCreate();
    }


    public void testCreateUsernameUppercase()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");
        tester.setFormElement("username", "User");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit("Create");
        tester.assertTextPresent("The username must be all lowercase.");
    }

    public void testCreateUserInvalidEmail()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");
        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username.email.com");
        tester.submit("Create");
        tester.assertTextPresent("You must specify a valid email address.");
    }

    public void testCreateUserPassword()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");
        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit("Create");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("confirm", "confirm");
        tester.submit("Create");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "abc");
        tester.setFormElement("confirm", "def");
        tester.submit("Create");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.submit("Create");

        tester.assertTextPresent("User: User Tested");
        final String[] userDetails = { "Username:", "user",
                "Full Name:", "User Tested",
                "Email:", "username@email.com"
        };
        text.assertTextSequence(new WebPageLocator(tester), userDetails);
    }

    public void testNoPermission()
    {
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        tester.gotoPage("http://localhost:8090/jira/secure/admin/user/AddUser!default.jspa");

        tester.assertTextPresent("Welcome to jWebTest JIRA installation");
        tester.assertTextNotPresent("Project: newproject");
        tester.assertTextNotPresent("Add A New Project");
    }

    public void testCreateUserExternalUserConfiguration()
    {
        administration.generalConfiguration().setExternalUserManagement(true);

        navigation.gotoAdminSection("user_browser");
        tester.assertLinkNotPresent("add_user");
        tester.assertTextNotPresent("Add User");

        tester.clickLink("general_configuration");
        tester.clickLinkWithText("Edit Configuration");
        tester.checkCheckbox("externalUM", "false");
        tester.submit("Update");

        navigation.gotoAdminSection("user_browser");
        tester.assertLinkPresent("add_user");
        tester.assertTextPresent("Add User");

        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");

        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit("Create");
        tester.assertTextPresent("User: User Tested");

        final String[] userDetails = { "Username:", "user",
                "Full Name:", "User Tested",
                "Email:", "username@email.com"
        };
        text.assertTextSequence(new WebPageLocator(tester), userDetails);
    }

    public void testCreateUserEmailSent() throws InterruptedException, IOException, MessagingException
    {
        configureAndStartSmtpServer();

        checkSuccessUserCreate(true);

        // now see if we got an email with the right details
        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = getGreenMail().getReceivedMessages();
        assertEquals(1, mimeMessages.length);

        final MimeMessage msg = mimeMessages[0];
        String body = msg.getContent().toString();
        assertTrue(find(body, Pattern.compile("Username: user")));
        assertTrue(find(body, Pattern.compile("Email: username@email.com")));
        assertTrue(find(body, Pattern.compile("Full Name: User Tested")));

        // does it have the reset password part
        assertTrue(find(body, Pattern.compile("If you follow the link below you will be able to personally set your own password")));
        assertTrue(find(body, Pattern.compile("secure/ResetPassword!default.jspa\\?os_username=user&token=")));

        // and make sure it has NO password in there
        assertFalse(find(body, Pattern.compile("evilwoman")));

    }

    private void checkSuccessUserCreate()
    {
        checkSuccessUserCreate(false);
    }

    private void checkSuccessUserCreate(final boolean sendEmail)
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("add_user");
        tester.assertTextPresent("Create New User");

        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.setFormElement("password", "evilwoman");
        tester.setFormElement("confirm", "evilwoman");
        if (sendEmail)
        {
            tester.checkCheckbox("sendEmail", "true");
        }
        tester.submit("Create");
        tester.assertTextPresent("User: User Tested");

        final String[] userDetails = { "Username:", "user",
                "Full Name:", "User Tested",
                "Email:", "username@email.com"
        };
        text.assertTextSequence(new WebPageLocator(tester), userDetails);
    }

    //JRA-22984
    public void testNewUsersAreNotGivenAdminRights()
    {
        navigation.gotoAdminSection("global_permissions");
        _addPermissionToGroup("1", "jira-administrators");
        tester.assertElementNotPresent("del_1_jira-administrators");
        _addPermissionToGroup("22", "jira-users");
        tester.assertElementPresent("del_22_jira-users");
        checkSuccessUserCreate();
        navigation.logout();
        navigation.login("user", "evilwoman");
        tester.assertElementNotPresent("admin_link");
        navigation.manageFilters().goToDefault();
        navigation.manageFilters().createFilter("Vic 20", "real computer.");
        navigation.issueNavigator().loadFilter(10000);
        tester.clickLink("filtereditshares");
        tester.assertElementPresent("share_div");
    }

    //JRA-25554
    public void testNewUsersNotAddedToNestedGroups()
    {
        addEditNestedGroups();

        checkSuccessUserCreate(false);

        // check the members groups
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("user");
        tester.assertTextPresent("jira-users");
        tester.assertTextNotPresent("accounts");
        tester.assertTextNotPresent("sales");
        tester.assertTextNotPresent("customer-service");

        // Reset the admin password otherwise the next test to run will be fail because some dick hacked the authenticator in most tests.
        resetAdminPassword();
    }

    private void _addPermissionToGroup(final String PermissionType, final String group) {

        tester.setFormElement("permType", PermissionType);
        tester.setFormElement("groupName", group);
        tester.submit("Add");
    }

    private void addEditNestedGroups()
    {
        toggleNestedGroups(true);

        addGroup("accounts");
        addGroup("sales");
        addGroup("customer-service");
        navigation.gotoAdminSection("group_browser");
        tester.clickLink("edit_nested_groups");
        tester.assertTextPresent("This page allows you to edit nested group memberships.");

        selectMultiOption("selectedGroupsStr", "jira-users");

        selectMultiOption("childrenToAssignStr", "accounts");
        selectMultiOption("childrenToAssignStr", "sales");
        selectMultiOption("childrenToAssignStr", "customer-service");

        tester.submit("assign");
    }

    public void selectMultiOption(String selectName, String option)
    {
        // A bit of a hack. The only way to really select multiple options at the moment is to treat it like a checkbox
        String value = tester.getDialog().getValueForOption(selectName, option);
        tester.checkCheckbox(selectName, value);
    }

    private void addGroup(String groupName)
    {
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.setFormElement("addName",groupName);
        tester.submit("add_group");
    }

    public void toggleNestedGroups(boolean enable)
    {
        navigation.gotoAdmin();
        tester.gotoPage("/plugins/servlet/embedded-crowd/directories/list");

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(1, 4).getLinkWith("edit");
        navigation.clickLink(link);
        if (enable)
        {
            tester.checkCheckbox("nestedGroupsEnabled", "true");
        }
        else
        {
            tester.checkCheckbox("nestedGroupsEnabled", "false");
        }

        tester.submit("save");
    }

    public void resetAdminPassword()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("admin");
        tester.clickLinkWithText("Set Password");
        tester.setFormElement("password", "admin");
        tester.setFormElement("confirm", "admin");
        tester.submit("Update");
    }

    private boolean find(final String s, final Pattern pattern)
    {
        return pattern.matcher(s).find();
    }

}