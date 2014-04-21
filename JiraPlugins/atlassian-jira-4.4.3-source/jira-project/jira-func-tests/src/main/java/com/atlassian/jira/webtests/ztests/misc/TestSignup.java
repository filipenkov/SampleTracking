/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.apache.commons.lang.StringUtils;

/**
 * Tests the Signup action
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestSignup extends JIRAWebTest
{
    public TestSignup(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testSignupLinkNotPresentIfJiraNotPublic() throws Exception
    {
        boolean enabled;
        // enable PUBLIC mode
        enablePublicMode(enabled = true);
        logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);

        // disable PUBLIC mode
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = false);
        logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);

        // enable PUBLIC mode
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = true);
        logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);
    }

    public void testSignupLinkNotPresentIfJiraNotPublicEditIssue() throws Exception
    {
        addProject("Test", "TST", ADMIN_USERNAME);
        final String issueKey = addIssue("Test", "TST", "Bug", "Nam lobortis; nulla et sollicitudin");
        final String issueId = getIssueIdWithIssueKey(issueKey);

        boolean enabled;
        // enable PUBLIC mode
        enablePublicMode(enabled = true);
        logout();

        assertMessageOnEditIssue(enabled, issueId);

        // disable PUBLIC mode
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = false);
        logout();

        assertMessageOnEditIssue(enabled, issueId);

        // enable PUBLIC mode
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = true);
        logout();

        assertMessageOnEditIssue(enabled, issueId);
    }

    private void assertCommonMessageOnPage(final boolean enabled, final String page)
    {
        gotoPage(page);
        if (enabled)
        {
            // Not a member? Signup for an account.
            assertTextSequence(new String[] { "Not a member?", "Sign up", "for an account." });
            //Disabled until http://jdog.atlassian.com/browse/JRADEV-1422 has been resolved
//            assertLinkWithTextExists("Administrator");
            // Not a member? Contact an Administrator to request an account.
            assertTextNotPresent("to request an account.");
        }
        else
        {
            // Not a member? Signup for an account.
            assertTextNotPresent("for an account");
            // Not a member? Contact an Administrator to request an account.
            assertTextSequence(new String[] { "Not a member?", "To request an account, please contact your", "JIRA administrators" });
            assertLinkWithTextExists("JIRA administrators");
        }
    }

    private void assertMessageOnEditIssue(boolean enabled, String issueId)
    {
        gotoPage("/secure/EditIssue!default.jspa?id=" + issueId);
        if (enabled)
        {
            assertTextPresent("You are not logged in, and do not have the permissions required to act on the selected issue as a guest.");
            // Please login or signup for an account.
            assertTextSequence(new String[] { "Please", "login", "or", "sign up", "for an account." });
            assertLinkWithTextExists("sign up");
        }
        else
        {
            // Not a member? Signup for an account.
            assertTextNotPresent("for an account");
        }
    }

    private void assertMessageOnLoginPage(boolean enabled)
    {
        assertCommonMessageOnPage(enabled, "/login.jsp");
    }

    private void assertMessageOnBrowseProjects(boolean enabled)
    {
        gotoPage("/secure/project/BrowseProjects.jspa");
        if (enabled)
        {
            // To browse projects first login or signup for an account.
            assertTextSequence(new String[] { "To browse projects, first", "login", "or", "sign up", "for an account." });
            assertLinkWithTextExists("sign up");
        }
        else
        {
            assertTextNotPresent("for an account.");
        }
    }

    private void assertMessageOnViewProjects(boolean enabled)
    {
        gotoPage("/secure/project/ViewProjects.jspa");
        if (enabled)
        {
            // If you log in or signup for an account, you might be able to see more here.
            assertTextSequence(new String[] { "If you", "log in", "or", "sign up", "for an account, you might be able to see more here." });
            assertLinkWithTextExists("sign up");
        }
        else
        {
            assertTextNotPresent("for an account, you might be able to see more here.");
        }
    }

    private void enablePublicMode(final boolean enable)
    {
        gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");
        selectOption("mode", enable ? "Public" : "Private");
        submit("Update");
    }

    public void testEnableCaptcha()
    {
        toggleCaptcha(true);
        logout();
        // we need to request the captcha servlet to link a captcha with our session
        beginAt("/captcha");
        beginAt("/");

        tester.gotoPage("login.jsp");
        clickLink("signup");
        //make sure the captcha element is present
        assertFormElementPresent("captcha");
        setFormElement("username", "test");
        setFormElement("fullname", "test");
        setFormElement("email", "test@test.com");
        setFormElement("captcha", "");
        submit();
        assertTextPresent("Please enter the word as shown below");
    }

    public void testDisableCaptcha()
    {
        toggleCaptcha(false);
        logout();
        clickLinkWithText("Log in again.");

        tester.gotoPage("login.jsp");
        clickLink("signup");
        //make sure the captcha element is present
        assertFormElementNotPresent("captcha");
        setFormElement("username", "test");
        setFormElement("fullname", "test");
        setFormElement("password", "password");
        setFormElement("confirm", "password");
        setFormElement("email", "test@test.com");
        submit();
        assertTextNotPresent("You must enter the text exactly as it appears in the picture.");
        assertTextPresent("You have successfully signed up.");
    }

    public void testStayInTouchLink()
    {
        logout();
        clickLinkWithText("Log in again.");
        tester.gotoPage("login.jsp");
        clickLink("signup");
        setFormElement("username", "test");
        setFormElement("fullname", "test");
        setFormElement("password", "password");
        setFormElement("confirm", "password");
        setFormElement("email", "test@test.com");
        submit();
        assertTextPresent("Stay connected with Atlassian. Subscribe to");
        assertLinkWithTextUrlEndsWith("blogs, newsletters, forums and more", "http://www.atlassian.com/about/connected.jsp?s_kwcid=jira-stayintouch");
    }

    private void toggleCaptcha(boolean enable)
    {
        gotoAdmin();
        clickLink("general_configuration");
        clickLinkWithText("Edit Configuration");
        checkCheckbox("captcha", String.valueOf(enable));
        submit("Update");
    }

    public void testEmptyData()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "");
        tester.setFormElement("fullname", "");
        tester.setFormElement("email", "");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a username.");
        tester.assertTextPresent("You must specify a password and a confirmation password.");
        tester.assertTextPresent("You must specify a full name.");
        tester.assertTextPresent("You must specify an email address.");
    }

    public void testNoPasswordSet()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a password and a confirmation password.");
        tester.assertTextNotPresent("You must specify a username.");
        tester.assertTextNotPresent("You must specify a full name.");
        tester.assertTextNotPresent("You must specify an email address.");

    }

    public void testSignUpDuplicateUser()
    {
        checkSuccessUserCreate();

        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("A user with that username already exists.");
    }

    public void testCreateUserSuccess()
    {
        checkSuccessUserCreate();
    }

    private void checkSuccessUserCreate()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You have successfully signed up. If you forget your password, you can have it emailed to you.");
    }

    public void testSignUpUsernameUppercase()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "User");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("The username must be all lowercase.");
    }

    public void testCreateUserInvalidEmail()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "User");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user.email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a valid email address.");
    }

    public void testCreateUserWIthLeadingOrTrailingSpaces()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        final String untrimmedUserName = "   andres  ";
        tester.setFormElement("username", untrimmedUserName);
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@example.com");
        tester.submit();

        tester.assertTextPresent("You have successfully signed up.");

        // Try to login with the untrimmed username
        navigation.loginAttempt(untrimmedUserName, "password");
        tester.assertTextPresent("Sorry, your username and password are incorrect - please try again.");

        // Now use the trimmed user name
        navigation.loginAttempt(untrimmedUserName.trim(), "password");
        tester.assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");
        assertions.getURLAssertions().assertCurrentURLEndsWith("Dashboard.jspa");
    }

    public void testCreateUserFieldsExceed255()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        final String username = StringUtils.repeat("abcdefgh", 32);
        final String fullname = StringUtils.repeat("ABCDEFGH", 32);
        final String email = StringUtils.repeat("x", 246) + "@email.com";

        tester.setFormElement("username", username);
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", fullname);
        tester.setFormElement("email", email);
        tester.submit();

        assertTextPresent("The username must not exceed 255 characters in length.");
        assertTextPresent("The full name must not exceed 255 characters in length.");
        assertTextPresent("The email address must not exceed 255 characters in length.");

        tester.setFormElement("username", username.substring(0, 255));
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", fullname.substring(0, 255));
        tester.setFormElement("email", email.substring(0, 255));
        tester.submit();

        assertTextNotPresent("The username must not exceed 255 characters in length.");
        assertTextNotPresent("The full name must not exceed 255 characters in length.");
        assertTextNotPresent("The email address must not exceed 255 characters in length.");

        login(username.substring(0, 255), "password");
        assertTextPresent(fullname.substring(0, 255));
    }

    public void testCreateUserPassword()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("confirm", "confirm");
        tester.submit();
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "abc");
        tester.setFormElement("confirm", "def");
        tester.submit();
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You have successfully signed up. If you forget your password, you can have it emailed to you.");
    }

}