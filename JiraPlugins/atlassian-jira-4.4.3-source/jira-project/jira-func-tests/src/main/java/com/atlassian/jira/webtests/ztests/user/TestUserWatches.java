package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestUserWatches extends FuncTestCase {
    public void testUnresolvedFilter() throws Exception {
        administration.restoreData("TestUserWatches.xml");
        navigation.logout();
        navigation.login(BOB_USERNAME, BOB_PASSWORD);


        // should default to All issues being shown, meaning that Unresolved will be the link
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("watched");

        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-5");

        // switch to Unresolved
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("watched_open");
        assertions.getJiraFormAssertions().assertFormNotificationMsg("No matching issues found.");

        // stop watching that one issue
        navigation.issue().unwatchIssue("HSP-5");

        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("watched");
        assertions.getJiraFormAssertions().assertFormNotificationMsg("No matching issues found.");

    }

}
