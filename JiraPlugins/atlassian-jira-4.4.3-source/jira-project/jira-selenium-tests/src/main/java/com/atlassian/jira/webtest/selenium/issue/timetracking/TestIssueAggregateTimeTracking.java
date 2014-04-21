package com.atlassian.jira.webtest.selenium.issue.timetracking;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

@WebTest({Category.SELENIUM_TEST })
public class TestIssueAggregateTimeTracking extends JiraSeleniumTest
{

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestTimeTrackingAggregates.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testAggregateVsSingleIssueTimeGraph()
    {
        getNavigator().gotoIssue("HSP-9");

        assertThat.elementNotVisible("jquery=#tt_info_single");
        assertThat.elementVisible("jquery=#tt_info_aggregate");
        
        client.click("jquery=#tt_include_subtasks input");
        assertThat.visibleByTimeout("jquery=#tt_info_single");
        assertThat.elementNotVisible("jquery=#tt_info_aggregate");

        client.click("jquery=#tt_include_subtasks input");
        assertThat.visibleByTimeout("jquery=#tt_info_aggregate");
        assertThat.elementNotVisible("jquery=#tt_info_single");

        client.click("jquery=#tt_include_subtasks input");
        assertThat.visibleByTimeout("jquery=#tt_info_single");
        assertThat.elementNotVisible("jquery=#tt_info_aggregate");

    }
}