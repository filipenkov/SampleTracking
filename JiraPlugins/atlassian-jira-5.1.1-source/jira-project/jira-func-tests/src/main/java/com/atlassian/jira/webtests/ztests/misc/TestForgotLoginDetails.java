package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.USERS_AND_GROUPS })
public class TestForgotLoginDetails extends EmailFuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME);
        administration.restoreBlankInstance();
        configureAndStartSmtpServer();
    }

    public void testShouldBlockResettingLoginDetailsIfExternalUserManagementIsOn()
    {
        administration.generalConfiguration().setExternalUserManagement(true);

        navigation.logout();
        tester.gotoPage("login.jsp");
        tester.assertLinkNotPresent("Can't access your account?");
    }

    public void testShouldFailWhenTheActionIsInvokedDirectlyIfExternalUserManagementIsOn()
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        administration.generalConfiguration().setExternalUserManagement(true);

        tester.gotoPage("secure/ForgotLoginDetails.jspa");
        text.assertTextPresent(locator.page(), "User login details can not be reset for this JIRA site.");
    }

    public void testDefaultOptionIsForgotPassword() throws Exception
    {
        gotoForgotLoginDetails();

        // assert that the forgot password option is selected by default
        assertEquals("checked", xpath("//*[@id='forgot-login-rb-forgot-password']/attribute::checked").getText());
    }

    public void testForgotPasswordValidatesNonExistingUserNamesAndEmails()
    {
        gotoForgotLoginDetails();

        // we should now be on the page
        tester.setFormElement("forgotten", "forgotPassword");
        tester.setFormElement("username", "nobody");
        tester.submit();
        tester.assertTextPresent("No user with that username exists");

        tester.setFormElement("forgotten", "forgotUserName");
        tester.setFormElement("email", "nobody");
        tester.submit();
        tester.assertTextPresent("No user with that email address exists");

        // direct page invoke should also not fail -- JRA-15602
        tester.gotoPage("secure/ForgotLoginDetails.jspa");
        tester.assertTextPresent("If you can't access JIRA, fill in this form and an email will be sent to you with the details to access your account again.");
    }

    public void testEmailGeneratedAndResetWhileLoggedOut() throws InterruptedException, IOException, MessagingException
    {
        final String tokenUrl = generateTokenAndGotoResetPage(ADMIN_USERNAME, 1);
        navigation.logout();
        tester.gotoPage(tokenUrl);
        text.assertTextPresent(locator.id("reset-password-user-name"), ADMIN_USERNAME);

        // blank passwords
        tester.setFormElement("password", "");
        tester.setFormElement("confirm", "");
        tester.submit("Reset");
        tester.assertTextPresent("The password must not be blank");

        // mismatch passwords
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", "FAIL");
        tester.submit("Reset");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.assertTextPresent("Reset Password");
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.submit("Reset");
        tester.assertTextPresent("Your password has been reset");
        tester.clickLinkWithText("log in");
    }

    public void testEmailGeneratedAndResetWhileLoggedIn() throws InterruptedException, IOException, MessagingException
    {
        final String tokenUrl = generateTokenAndGotoResetPage(ADMIN_USERNAME, 1);
        tester.gotoPage(tokenUrl);
        text.assertTextPresent(locator.id("reset-password-user-name"), ADMIN_USERNAME);

        tester.assertTextPresent("Reset Password");
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.submit("Reset");
        tester.assertTextPresent("Your password has been reset");
        tester.clickLinkWithText("use JIRA");
    }

    public void testResetPasswordEmailIsSentToUserNamesWithSpaces()
            throws MessagingException, IOException, InterruptedException
    {
        // first check the "user created email"
        final String user = "joe t";
        backdoor.usersAndGroups().addUser(user, user, "Joe Thomas", "joet@example.com", true);
        checkEmailFor_joe_t(1);

        // Now check the forgot login password email
        navigation.login(user);
        gotoForgotLoginDetails();
        tester.setFormElement("forgotten", "forgotPassword");
        tester.setFormElement("username", user);
        tester.submit();
        checkEmailFor_joe_t(2);
    }

    private void checkEmailFor_joe_t(int messages) throws InterruptedException, IOException, MessagingException
    {
        // we should now have an email
        navigation.login(ADMIN_USERNAME);
        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(messages, mimeMessages.length);

        String body = mimeMessages[messages-1].getContent().toString();

        // check that the username is URL escaped in the email
        assertTrue(body.contains("ResetPassword!default.jspa?os_username=joe+t"));
        assertTrue(body.contains("ForgotLoginDetails.jspa?username=joe+t"));
    }

    public void testBadInputOnResetPasswordAction() throws IOException, MessagingException, InterruptedException
    {
        // bad user name
        tester.gotoPage("secure/ResetPassword!default.jspa?os_username=baduser&token=3DB7FA2C8DF56A3128B5692B2C7652D");
        tester.assertTextPresent("The user cannot be found");
        tester.submit("Reset");
        tester.assertTextPresent("The user cannot be found");
        tester.assertTextPresent("The password must not be blank");

        // token never been seen
        tester.gotoPage("secure/ResetPassword!default.jspa?os_username=admin&token=3DB7FA2C8DF56A3128B5692B2C7652D");
        tester.assertTextPresent("The reset password token you have provided has timed out.");
        text.assertTextPresent(locator.id("reset-password-get-new-token"),"You can get a new token here.");
        tester.clickLink("reset-password-get-new-token-link");
        tester.assertTextPresent("Can't access your account?");
        tester.assertTextPresent("If you can't access JIRA, fill in this form and an email will be sent to you with the details to access your account again.");

        // burn a token
        final String tokenUrl1 = generateTokenAndGotoResetPage(ADMIN_USERNAME, 1);
        generateTokenAndGotoResetPage(ADMIN_USERNAME, 2);

        tester.gotoPage(tokenUrl1);
        tester.assertTextPresent("The reset password token you have provided is no longer valid");
        tester.clickLink("reset-password-get-new-token-link");
        tester.assertTextPresent("Can't access your account?");
        tester.assertTextPresent("If you can't access JIRA, fill in this form and an email will be sent to you with the details to access your account again.");        
    }

    private String generateTokenAndGotoResetPage(final String userName, final int expectedMails)
            throws InterruptedException, IOException, MessagingException
    {
        gotoForgotLoginDetails();

        tester.setFormElement("forgotten", "forgotPassword");
        tester.setFormElement("username", userName);
        tester.submit();

        tester.assertTextPresent("A reset password link has been sent to you via email.");

        // we should now have an email
        navigation.login(ADMIN_USERNAME);
        flushMailQueueAndWait(1);

        MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(expectedMails, mimeMessages.length);

        String body = mimeMessages[expectedMails-1].getContent().toString();

        assertTrue(body.contains("you will be able to personally reset your password"));

        final Pattern urlPattern = Pattern.compile("(secure/ResetPassword!default\\.jspa\\?os_username=" + userName + "&token=.*)", Pattern.MULTILINE);
        final Matcher matcher = urlPattern.matcher(body);
        assertTrue(matcher.find());
        assertEquals(1, matcher.groupCount());

        // now goto that page and reset the password
        return matcher.group(0);
    }

    private void gotoForgotLoginDetails()
    {
        navigation.logout();
        tester.gotoPage("login.jsp");
        tester.clickLinkWithText("Can't access your account?");
        tester.assertTextPresent("Can't access your account?");
        tester.assertTextPresent("If you can't access JIRA, fill in this form and an email will be sent to you with the details to access your account again.");
    }
}
