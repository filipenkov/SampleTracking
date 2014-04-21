package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.page.admin.EditGeneralConfiguration;
import com.atlassian.jira.webtest.framework.page.admin.ViewGeneralConfiguration;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * Test for JRA-19714.
 * Login gadget now behaves in the same manner as the login.jsp and tells the user that they
 * do not have permission to log in when they get the username and password correct but
 * they dont have permission to login.
 *
 *  Use the TestLoginGadget setup. In this setup the admin user can log in as usual.
 *  Fred does not have permission to log in.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestLoginGadget extends JiraSeleniumTest
{
    protected static final int TIMEOUT = 30000;
    protected static final String BAD_PASSWORD = "asfdasdfasdfa";
    protected static final String FRED_USERNAME = "fred";
    protected static final String FRED_PASSWORD = "fred";
    private static final String LOGIN_INPUT_ID = "id=login-form-username";
    private static final String PASSWORD_INPUT_ID = "id=login-form-password";

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestLoginGadget.xml");
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        waitForLoginForm();
    }

    public void testGoodLogin() throws Exception
    {
        getSeleniumClient().type(LOGIN_INPUT_ID, ADMIN_USERNAME);
        getSeleniumClient().type(PASSWORD_INPUT_ID, ADMIN_PASSWORD);
        assertThat.textNotPresent("Administrator");
        getNavigator().clickAndWaitForPageLoad("id=login");
        assertThat.textPresent("Administrator");
    }

    public void testWrongPassword() throws Exception
    {
        // First test with a user that does have permission to log in
        attemptLoginAndCheckForErrorMessage(ADMIN_USERNAME, BAD_PASSWORD, "Sorry, your username and password are incorrect - please try again.");

        // First test with a user that does NOT have permission to log in
        attemptLoginAndCheckForErrorMessage(FRED_USERNAME, BAD_PASSWORD, "Sorry, your userid is required to answer a CAPTCHA question correctly.");
    }

    public void testCorrectPasswordNoPermissionsToLogin() throws Exception
    {
        attemptLoginAndCheckForErrorMessage(FRED_USERNAME, FRED_PASSWORD, "Sorry, your userid is required to answer a CAPTCHA question correctly.");
    }

    public void testLoginOptions()
    {
        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        client.selectFrame("gadget-0");
        assertThat.linkPresentWithText("Can't access your account?");
        assertThat.textPresent("emember my login on this computer");
        assertThat.linkPresentWithText("Sign Up");
        client.selectFrame("relative=top");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("/secure/admin/EditApplicationProperties!default.jspa", true);
        client.select("mode", "Private");
        client.click("id=edit_property", true);

        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        client.selectFrame("gadget-0");
        assertThat.linkPresentWithText("Can't access your account?");
        assertThat.textPresent("Remember my login on this computer");
        assertThat.linkNotPresentWithText("Sign Up");
        client.selectFrame("relative=top");

        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoPage("/secure/admin/EditApplicationProperties!default.jspa", true);
        client.click("externalUM", false);
        client.click("id=edit_property", true);

        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();
        client.selectFrame("gadget-0");
        assertThat.elementNotVisible("id=forgotpassword");
        assertThat.textPresent("Remember my login on this computer");
        assertThat.linkNotPresentWithText("Sign Up");
        client.selectFrame("relative=top");
    }

    public void testShouldNotShowResetPasswordLinkIfExternalUserManagementIsOn()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        final EditGeneralConfiguration generalConfiguration = globalPages().goToAdministration().goToPage(ViewGeneralConfiguration.class)
                .edit();
        generalConfiguration.setExternalUserManagement(true);
        generalConfiguration.setMode("Private");
        generalConfiguration.submit();

        getNavigator().logout(getXsrfToken());
        getNavigator().gotoHome();

        client.selectFrame("gadget-0");
        assertThat.elementNotVisible("id=forgotpassword");
        client.selectFrame("relative=top");
    }

    private void attemptLoginAndCheckForErrorMessage(final String username, final String password, final String errorMessage)
    {
        getSeleniumClient().type(LOGIN_INPUT_ID, username);
        getSeleniumClient().type(PASSWORD_INPUT_ID, password);
        getNavigator().click("id=login");
        waitForErrorText();
        assertThat.elementContainsText("id=usernameerror", errorMessage);
    }

    private void waitForLoginForm()
    {
        visibleByTimeoutWithDelay("id=loginform", TIMEOUT);
    }

    private void waitForErrorText()
    {
        visibleByTimeoutWithDelay("id=usernameerror",TIMEOUT);
    }
}
