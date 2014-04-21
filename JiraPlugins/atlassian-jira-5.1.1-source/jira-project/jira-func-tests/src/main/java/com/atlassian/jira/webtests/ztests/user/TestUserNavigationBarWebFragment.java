package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * test that the web fragment links on the user navigation bar
 * is visible with correct permissions.
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestUserNavigationBarWebFragment extends JIRAWebTest
{
    private static final String BACK_TO_PREVIOUS_VIEW = "Back to previous view";
    private static final String ISSUE_SUMMARY = "test printable";

    public TestUserNavigationBarWebFragment(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWebFragment.xml");
    }

    public void tearDown()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        restoreBlankInstance();
        super.tearDown();
    }

    public void testUserNavigationBarWebFragment()
    {
        _testLinkVisibilityWhileLoggedIn();
        _testLinkVisibilityWhileNotLoggedIn();
    }

    // Test that the printable view link is rendered properly, even for non-servlet's - JRA-11527
    public void testPrintableViewLink()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //assert printable link is valid in issue view (no query string)
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", ISSUE_SUMMARY);
        gotoIssue(issueKey);
        assertTextPresent("Details");
        assertLinkPresentWithText(issueKey);
        gotoPage("/si/jira.issueviews:issue-html/HSP-1/HSP-1.html");
        assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        assertTextPresent("[" + issueKey + "]");
        assertLinkPresentWithText(ISSUE_SUMMARY);

        //assert printable link is valid in the issue navigator (has query string)
        gotoPage("/secure/IssueNavigator.jspa?reset=true&sorter/field=issuekey&sorter/order=DESC");
        assertTextPresent("Issue Navigator");
        gotoPage("/secure/IssueNavigator.jspa?decorator=printable&reset=true&amp;sorter/field=issuekey&amp;sorter/order=DESC");
        assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        assertTextPresent("Issue Navigator");
        assertTextPresent(issueKey);
        assertTextPresent(ISSUE_SUMMARY);

        //assert printable link is valid for non-servlet based page (eg. securitybreach.jsp)
        gotoPage("/secure/views/securitybreach.jsp");
        assertTextPresent("Access Denied");
        gotoPage("/secure/views/securitybreach.jsp?decorator=printable");
        assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        assertTextPresent("Access Denied");
    }

    private void _testLinkVisibilityWhileLoggedIn()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertLinkNotPresentWithText("Log In");

        assertLinkPresentWithText(ADMIN_FULLNAME);

        assertLinkPresentWithText("Online Help");
        assertLinkPresentWithText("Profile");
        assertLinkPresentWithText("About JIRA");
        assertLinkPresentWithText("Profile");
        assertLinkPresentWithText("Log Out");

    }

    private void _testLinkVisibilityWhileNotLoggedIn()
    {
        //check the links after logging out
        logout();
        beginAt("/secure/Dashboard.jspa"); //go back to the dashboard
        assertLinkPresentWithText("Log In");
        assertLinkNotPresentWithText("Log Out");

        assertions.assertNodeByIdDoesNotExist("header-details-user-fullname");
    }
}
