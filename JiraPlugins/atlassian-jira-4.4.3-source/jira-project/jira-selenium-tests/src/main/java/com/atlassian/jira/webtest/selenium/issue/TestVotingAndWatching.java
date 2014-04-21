package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Tests for issue voting and watching UI. 
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestVotingAndWatching extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestVotingAndWatching.xml");
    }

    public void testVoting()
    {
        getNavigator().gotoIssue("HSP-4");
        //initially shouldn't have voted for anything
        assertThat.elementVisible("//a[@id='vote-toggle' and @title='Vote for this issue']/span[@class='icon icon-vote-off']");
        assertThat.elementNotVisible("//a[@id='vote-toggle']/span[@class='icon icon-vote-on']");
        assertThat.elementHasText("Id=vote-data", "0");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-vote-issue' and @title='Vote for this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-vote-issue' and @title='Remove vote for this issue']");
        assertThat.elementHasText("Id=toggle-vote-issue", "Add Vote");

        client.click("vote-toggle");
        assertThat.elementPresentByTimeout("//a[@id='vote-toggle' and @title='Remove vote for this issue']/span[@class='icon icon-vote-on']", DROP_DOWN_WAIT);
        assertThat.elementNotVisible("//a[@id='vote-toggle']/span[@class='icon icon-vote-off']");
        assertThat.elementHasText("Id=vote-data", "1");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-vote-issue' and @title='Remove vote for this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-vote-issue' and @title='Vote for this issue']");
        assertThat.elementHasText("Id=toggle-vote-issue", "Remove Vote");

        //now click the issue op
        client.click("toggle-vote-issue");
        assertThat.elementPresentByTimeout("//a[@id='vote-toggle' and @title='Vote for this issue']/span[@class='icon icon-vote-off']", DROP_DOWN_WAIT);
        assertThat.elementNotVisible("//a[@id='vote-toggle']/span[@class='icon icon-vote-on']");
        assertThat.elementHasText("Id=vote-data", "0");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-vote-issue' and @title='Vote for this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-vote-issue' and @title='Remove vote for this issue']");
        assertThat.elementHasText("Id=toggle-vote-issue", "Add Vote");

        //reload the issue to make sure stuff got persisted on the server
        getNavigator().gotoIssue("HSP-4");
        assertThat.elementHasText("Id=vote-data", "0");

        //now check that issue that was reported can't be voted on
        getNavigator().gotoIssue("HSP-1");
        assertThat.elementNotPresentByTimeout("//a[@id='vote-toggle']/span[@class='icon icon-vote-off']", DROP_DOWN_WAIT);
        assertThat.elementPresentByTimeout("//span[@id='vote-label' and @title='You cannot vote for an issue you have reported.']/span[@class='icon icon-vote-disabled']", DROP_DOWN_WAIT);

        //also check that a resolved issue can't be voted for
        getNavigator().gotoIssue("HSP-5");
        assertThat.elementNotPresentByTimeout("//a[@id='vote-toggle']/span[@class='icon icon-vote-off']", DROP_DOWN_WAIT);
        assertThat.elementPresentByTimeout("//span[@id='vote-label' and @title='You cannot vote or change your vote on resolved issues.']/span[@class='icon icon-vote-disabled']", DROP_DOWN_WAIT);
    }

    public void testVotingOnLogOut() throws Exception
    {
        getNavigator().gotoIssue("HSP-4");
        backgroundLogout();
        client.click("vote-toggle");
        waitFor(DROP_DOWN_WAIT);
        assertTrue(client.getAlert().startsWith("You are not authorised to perform this operation."));
    }

    public void testVotingError()
    {
        getNavigator().gotoIssue("HSP-4");
        client.runScript(String.format("var contextPath='%s/not-existing-path'", getEnvironmentData().getContext()));
        client.click("vote-toggle");
        waitFor(DROP_DOWN_WAIT);
        assertEquals("The JIRA server was contacted but has returned an error response. We are unsure of the result of this operation.", client.getAlert());
    }

    public void testWatching()
    {
        getNavigator().gotoIssue("HSP-4");
        //initially there's only one watcher (not the current user)
        assertThat.elementVisible("//a[@id='watching-toggle' and @title='Start watching this issue']/span[@class='icon icon-watch-off']");
        assertThat.elementNotVisible("//a[@id='watching-toggle']/span[@class='icon icon-watch-on']");
        assertThat.elementHasText("Id=watcher-data", "1");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-watch-issue' and @title='Start watching this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-watch-issue' and @title='Stop watching this issue']");
        assertThat.elementHasText("Id=toggle-watch-issue", "Watch Issue");

        client.click("watching-toggle");
        assertThat.elementPresentByTimeout("//a[@id='watching-toggle' and @title='Stop watching this issue']/span[@class='icon icon-watch-on']", DROP_DOWN_WAIT);
        assertThat.elementNotVisible("//a[@id='watching-toggle']/span[@class='icon icon-watch-off']");
        assertThat.elementHasText("Id=watcher-data", "2");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-watch-issue' and @title='Stop watching this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-watch-issue' and @title='Start watching this issue']");
        assertThat.elementHasText("Id=toggle-watch-issue", "Stop Watching");

        //now click the issue op
        client.click("toggle-watch-issue");
        assertThat.elementPresentByTimeout("//a[@id='watching-toggle' and @title='Start watching this issue']/span[@class='icon icon-watch-off']", DROP_DOWN_WAIT);
        assertThat.elementNotVisible("//a[@id='watching-toggle']/span[@class='icon icon-watch-on']");
        assertThat.elementHasText("Id=watcher-data", "1");
        //issue op
        assertThat.elementPresent("//a[@id='toggle-watch-issue' and @title='Start watching this issue']");
        assertThat.elementNotPresent("//a[@id='toggle-watch-issue' and @title='Stop watching this issue']");
        assertThat.elementHasText("Id=toggle-watch-issue", "Watch Issue");

        //reload the issue to make sure stuff got persisted on the server
        getNavigator().gotoIssue("HSP-4");
        assertThat.elementHasText("Id=watcher-data", "1");
    }
}
