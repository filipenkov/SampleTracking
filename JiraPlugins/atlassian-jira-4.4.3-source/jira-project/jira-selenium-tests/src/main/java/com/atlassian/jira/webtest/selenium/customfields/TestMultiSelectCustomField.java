package com.atlassian.jira.webtest.selenium.customfields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;
@SkipInBrowser(browsers={Browser.IE}) // Selenium error on click command - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestMultiSelectCustomField extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestMultiSelectCustomField.class);
    }
    
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestMultiSelectCustomField.xml");
    }

    //JRA-12741
    public void testMultiSelectHTML()
    {
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().issueNavigator().displayAllIssues();
		assertTrue(client.isTextPresent("GreenIssue"));
		assertTrue(client.isTextPresent("BlueIssue"));
		assertTrue(client.isTextPresent("RedIssue"));
		assertTrue(client.isTextPresent("BlueGreenIssue"));
		client.removeSelection("customfield_10000", "label=Any");
		client.addSelection("customfield_10000", "label=Green");
        client.click("issue-filter-submit", true);
		assertTrue(client.isTextPresent("GreenIssue"));
		assertTrue(client.isTextPresent("BlueGreenIssue"));
		client.removeSelection("customfield_10000", "label=Green");
		client.addSelection("customfield_10000", "label=Red");
        client.click("issue-filter-submit", true);
		assertTrue(client.isTextPresent("RedIssue"));
		client.removeSelection("customfield_10000", "label=Red");
		client.addSelection("customfield_10000", "label=Blue");
        client.click("issue-filter-submit", true);
		assertTrue(client.isTextPresent("BlueGreenIssue"));
		assertTrue(client.isTextPresent("BlueIssue"));
		client.addSelection("customfield_10000", "label=Green");
		client.addSelection("customfield_10000", "label=Red");
		client.removeSelection("customfield_10000", "label=Blue");
        client.click("issue-filter-submit", true);
		assertTrue(client.isTextPresent("GreenIssue"));
		assertTrue(client.isTextPresent("RedIssue"));
		assertTrue(client.isTextPresent("BlueGreenIssue"));
    }
}