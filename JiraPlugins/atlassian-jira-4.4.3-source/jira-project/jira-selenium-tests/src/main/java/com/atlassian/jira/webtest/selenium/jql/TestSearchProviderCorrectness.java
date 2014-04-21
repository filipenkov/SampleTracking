package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestSearchProviderCorrectness extends JiraSeleniumTest
{
    public void testFalseQueryReturnsNoResultsInPortlet() throws Exception
    {
        // JRA-17875: when a query with a single clause that generates a false result is used to back a portlet
        // it must bring back no results and not the match all query results

        // data has a saved filter with the query "parent = 'TWO-1'", and subtasks are disabled, so the query should
        // generate a single "false result"
        restoreData("TestFalseQueryReturnsNoResultsInGadget.xml");

        getNavigator().login(ADMIN_USERNAME).gotoHome();
        assertThat.elementDoesNotContainText("id=gadget-10040", "The filter associated with this gadget did not return any issues.");
    }
}
