package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

@SkipInBrowser(browsers={Browser.IE}) //JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestFilterBrowser extends JiraSeleniumTest
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestFilterBrowser.xml");
        backdoor.darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    @Override
    protected void onTearDown() throws Exception
    {
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.onTearDown();
    }

    public void testUserPicker()
    {
        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        client.click("Next", true);
        client.click("//img[@name='reporterImage']", false);
        client.waitForPopUp("UserPicker", PAGE_LOAD_WAIT);
        client.selectWindow("UserPicker");
        assertTrue(client.isTextPresent("Displaying users 1 to 20 of 42"));

        // Go to next page
        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.click("link=Next", true);
        assertTrue(client.isTextPresent("Displaying users 21 to 40 of 42"));

        // Filter for 'fred'
        client.type("emailFilter","fred");
        client.click("//input[@value='Filter']", true);
        assertTrue(client.isTextPresent("Displaying users 1 to 1 of 1"));
        client.close();
        client.selectWindow(null);
    }

    public void testGroupPicker()
    {
        getNavigator().gotoPage("secure/CreateIssue!default.jspa", true);
        client.click("Next", true);
        client.click("//a[@id='customfield_10000-trigger']", false);
        client.waitForPopUp("GroupPicker", PAGE_LOAD_WAIT);
        client.selectWindow("GroupPicker");
        assertTrue(client.isTextPresent("Displaying groups 1 to 20 of 43"));

        // Go to next page
        //TODO: REMOVE THIS once http://jira.atlassian.com/browse/JRA-17286 is fixed
        //this is a bit of a hack since chooseOkOnNextConfirmation() doesn't work FF2
//        client.chooseOkOnNextConfirmation();
        client.getEval("this.browserbot.getCurrentWindow().onbeforeunload = null");
        client.click("link=Next", true);
        assertTrue(client.isTextPresent("Displaying groups 21 to 40 of 43"));

        // Filter for 'jira'
        client.type("nameFilter","jira");
        client.click("//input[@value='Filter']", true);
        assertTrue(client.isTextPresent("Displaying groups 1 to 3 of 3"));
        client.close();
        client.selectWindow(null);
    }

    public void testUserBrowser()
    {
        getNavigator().gotoAdmin();

        client.click("user_browser", true);
        assertTrue(client.isTextPresent("Displaying users 1 to 20 of 42"));

        // Go to next page
        client.click("link=Next >>", true);
        assertTrue(client.isTextPresent("Displaying users 21 to 40 of 42"));

        // Filter for 'fred' in 'jira-users'
        client.type("emailFilter","fred");
        client.selectOption("group", "jira-users");
        client.click("//input[@value='Filter']", true);
        assertThat.textPresent("Displaying users 1 to 1 of 1");
        //client.close();
        client.selectWindow(null);
    }
}
