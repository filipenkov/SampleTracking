package com.atlassian.jira.webtest.selenium.mention;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;


@WebTest ({ Category.SELENIUM_TEST })
public class TestMentionAutocomplete extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestMentions.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");
    }

    public void testMentionAutoCompleteWorksOnLowerCommentField()
    {
        getNavigator().gotoIssue("HSP-1");
        client.runScript("window.resize(800,800)");
        client.click("Id=comment-issue");
        assertThat.elementPresentByTimeout("jquery=#comment");

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @adm");
        assertThat.elementPresentByTimeout("jquery=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);
        assertThat.elementContainsText("jquery=.aui-list .aui-iconised-link", "example.com");

        client.click("jquery=.aui-list .aui-iconised-link");
        assertEquals(client.getValue("jquery=#comment"), "This is test comment for [~admin]");
    }

    public void testMentionAutoCompleteDoesNotShowWithUserWithoutBrowseUsers()
    {
        getNavigator().login("bob", "bob");
        getNavigator().gotoIssue("HSP-1");
        client.runScript("window.resize(800,800)");
        client.click("Id=comment-issue");
        assertThat.elementPresentByTimeout("jquery=#comment");

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @adm");
        assertThat.elementNotPresentByTimeout("jquery=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);
    }

    public void testMentionAutoCompleteStopsWhenUserNotMatchOrOverTenWhitecharsctersAfterUsername()
    {
        getNavigator().gotoIssue("HSP-1");
        client.runScript("window.resize(800,800)");
        client.click("Id=comment-issue");
        assertThat.elementPresentByTimeout("jquery=#comment");

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @adm");
        assertThat.elementPresentByTimeout("jquery=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @admxyz");
        assertThat.notVisibleByTimeout("jquery=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);

    }

    public void testMentionAutoCompleteStopsWhenOverTenWhitecharsctersAfterUsername()
    {
        getNavigator().gotoIssue("HSP-1");
        client.runScript("window.resize(800,800)");
        client.click("Id=comment-issue");
        assertThat.elementPresentByTimeout("jquery=#comment");

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @adm   ");
        assertThat.elementPresentByTimeout("css=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);

        client.typeWithFullKeyEvents("jquery=#comment", "This is test comment for @adm           ");
        assertThat.notVisibleByTimeout("css=.aui-list .aui-iconised-link", DROP_DOWN_WAIT);

    }
}
