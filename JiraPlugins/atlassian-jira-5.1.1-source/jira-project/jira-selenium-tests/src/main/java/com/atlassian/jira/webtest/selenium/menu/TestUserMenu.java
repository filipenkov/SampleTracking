package com.atlassian.jira.webtest.selenium.menu;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

/**
 * @since v3.13
 */
@WebTest({Category.SELENIUM_TEST })
public class TestUserMenu extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";
    private static final String ADMIN_NAME = "Admin Istrator";
    private static final String TEST_NAME = "test";

    private static final String PROFILE_LINK = "view_profile";
    private static final String HELP_LINK = "view_help";
    private static final String KEYBOARDSHORTCUTS_LINK = "keyshortscuthelp";
    private static final String ABOUT_LINK = "view_about";
    private static final String LOGOUT_LINK = "log_out";

    private static final String USER_MENU_LINK = "jquery=#header-details-user";

    private static final String[] LOGGED_IN_MENU_LIST = {PROFILE_LINK,HELP_LINK,KEYBOARDSHORTCUTS_LINK,ABOUT_LINK,LOGOUT_LINK};
    private static final String[] LOGGED_OUT_MENU_LIST = {HELP_LINK,KEYBOARDSHORTCUTS_LINK,ABOUT_LINK};
    private static final String[] LOGGED_IN_DIFF_LOGGED_OUT = {PROFILE_LINK,LOGOUT_LINK};


    public static Test suite()
    {
        return suiteFor(TestUserMenu.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("AdminMenu.xml");
    }

    public void testUserMenu()
    {
        _testLoggedInAdmin();
        _testLoggedInUser();
        _testLoggedOut();

    }

    private void checkLinks(String... linkOrder)
    {
        client.click(USER_MENU_LINK + " a.drop.aui-dd-link");
        for (int i = 0; i < linkOrder.length; i++)
        {
            assertThat.visibleByTimeout(linkOrder[i], TIMEOUT);
        }
        getNavigator().gotoHome();
    }

    private void checkNoLinks(String... linkOrder)
    {
        for (int i = 0; i < linkOrder.length; i++)
            {
            client.click(USER_MENU_LINK);
            assertThat.elementNotPresentByTimeout(linkOrder[i], TIMEOUT);
        }
        client.click(USER_MENU_LINK);
    }


    private void _testLoggedInAdmin()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertThat.elementContainsText(USER_MENU_LINK + " a", ADMIN_NAME);
        getNavigator().clickAndWaitForPageLoad(USER_MENU_LINK + " a");

        assertTrue(client.getTitle().startsWith("User Profile: Admin Istrator"));

        checkLinks(LOGGED_IN_MENU_LIST);
        getNavigator().logout(getXsrfToken());
    }

    private void _testLoggedInUser()
    {
        getNavigator().login(TEST_USERNAME, TEST_PASSWORD);
        assertThat.elementContainsText(USER_MENU_LINK + " a", TEST_NAME);
        getNavigator().clickAndWaitForPageLoad(USER_MENU_LINK + " a");

        assertTrue(client.getTitle().startsWith("User Profile: test"));
        checkLinks(LOGGED_IN_MENU_LIST);
        getNavigator().logout(getXsrfToken());
    }

    private void _testLoggedOut()
    {
        getNavigator().gotoHome();
        checkLinks(LOGGED_OUT_MENU_LIST);
        checkNoLinks(LOGGED_IN_DIFF_LOGGED_OUT);
        client.clickLinkWithText("Log In", true);
        assertThat.textPresent("Welcome to Your Company JIRA");
        getNavigator().logout(getXsrfToken());
    }

}