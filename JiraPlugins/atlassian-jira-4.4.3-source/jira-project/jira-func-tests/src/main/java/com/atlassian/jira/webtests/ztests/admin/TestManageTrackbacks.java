package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestManageTrackbacks extends FuncTestCase
{
    private static final String ISSUE_WITH_TRACKBACKS = "HSP-1";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestTrackback.xml");
    }

    public void testManageTrackbacks()
    {
        _testManageTrackbackPermission();
        _testTrackbackDelete();
    }

    private void _testManageTrackbackPermission()
    {
        log("Test manage trackback link visibility");

        //user has no delete permission - so cannot manage trackbacks
        administration.usersAndGroups().addUser("user", "pass", "pass", "user@invalid.com");
        navigation.logout();
        navigation.login("user", "pass");
        navigation.issue().gotoIssue(ISSUE_WITH_TRACKBACKS);
        text.assertTextNotPresent(locator.page(), "Manage Trackbacks");

        //log back in as admin and check manage trackbacks is available
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issue().gotoIssue(ISSUE_WITH_TRACKBACKS);
        text.assertTextSequence(locator.page(), "Manage Trackbacks", "Trackbacks");
    }

    private void _testTrackbackDelete()
    {
        log("Test delete trackbacks");

        navigation.issue().gotoIssue(ISSUE_WITH_TRACKBACKS);
        navigation.clickLinkWithExactText("Manage Trackbacks");
        text.assertTextPresent(locator.page(), "This page allows you to manage the trackback links for a particular issue.");
        tester.clickLink("del_10000");
        tester.submit("Delete");
        tester.clickLink("del_10001");
        tester.submit("Delete");
        text.assertTextPresent(locator.page(), "There are no trackbacks for this issue.");
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(locator.page(), "Trackbacks");
        text.assertTextNotPresent(locator.page(), "Manage Trackbacks");
    }
}
