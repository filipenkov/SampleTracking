package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestAutoWatches extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAutowatchIsEnabledByDefault() throws Exception
    {
        String key = createIssueAndGotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);
    }

    public void testAutowatchDisabled() throws Exception
    {
        navigation.userProfile().changeAutowatch(false);
        createIssueAndGotoWatched();
        assertions.getJiraFormAssertions().assertFormNotificationMsg("No matching issues found.");
    }

    private String createIssueAndGotoWatched()
    {
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("watched");
        return key;
    }

}
