package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests for SER-117 - ensuring the seraph.os.cookie does not contain characters that some application servers might
 * not like (Tomcat 5.5.26 for example).
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestRememberMeCookie extends FuncTestCase
{
    private static final String USER_LAGONIL = "lagonil";
    private static final String SERAPH_REMEMBERME_COOKIE = "seraph.rememberme.cookie";

    public void testRememberMeCookieWorks()
    {
        administration.restoreBlankInstance();
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // username 'lagonil' used to be problematic
        administration.usersAndGroups().addUser(USER_LAGONIL);
        navigation.logout();
        navigation.login(USER_LAGONIL, USER_LAGONIL, true);

        // assert we are logged in by checking if we can see the Find Issues link
        tester.assertLinkPresent("find_link");
        tester.assertLinkPresentWithText(USER_LAGONIL);

        // check cookie for bad values
        String seraphCookie = tester.getDialog().getWebClient().getCookieValue(SERAPH_REMEMBERME_COOKIE);
        assertNotNull("Didnt find cookie as expected", seraphCookie);
        assertFalse("Found bad characters in cookie: " + seraphCookie, seraphCookie.indexOf(">") >= 0);
        assertFalse("Found bad characters in cookie: " + seraphCookie, seraphCookie.indexOf("<") >= 0);

        tester.getDialog().getWebClient().clearCookies();

        // check that we now can't see the Find Issues link
        tester.beginAt("/secure/Dashboard.jspa");
        tester.assertLinkNotPresent("find_link");
        tester.assertLinkNotPresentWithText(USER_LAGONIL);

        tester.getDialog().getWebClient().clearCookies();

        // re-add the cookie
        tester.getDialog().getWebClient().addCookie(SERAPH_REMEMBERME_COOKIE, seraphCookie);

        // check that if we "open a new window" the cookie works
        tester.beginAt("/secure/Dashboard.jspa");
        tester.assertLinkPresent("find_link");
        tester.assertLinkPresentWithText(USER_LAGONIL);
    }
}
