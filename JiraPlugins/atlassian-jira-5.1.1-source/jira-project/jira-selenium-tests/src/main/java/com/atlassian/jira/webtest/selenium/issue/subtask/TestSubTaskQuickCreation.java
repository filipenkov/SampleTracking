package com.atlassian.jira.webtest.selenium.issue.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickCreateIssue;
import com.atlassian.jira.webtest.selenium.framework.dialogs.QuickCreateSubtask;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * Assert vertical sub-task quick create form
 *
 * @since v3.12
 */
@SkipInBrowser(browsers={Browser.IE}) //Pop-up problem - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestSubTaskQuickCreation extends JiraSeleniumTest
{
    private static final String ISSUE_PARENT = "HSP-6";
    private QuickCreateSubtask quickCreate;


    public void onSetUp()
    {

        super.onSetUp();
        quickCreate = new QuickCreateSubtask(context());
        restoreData("TestTimeTrackingAggregates.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testFirstQuickCreateSubtask()
    {
        getNavigator().gotoIssue(ISSUE_PARENT);
        quickCreate.open();
        quickCreate.assertReady(5000);
        quickCreate.setFieldValue("summary", "Subtask 1");
        quickCreate.submit(SubmitType.BY_CLICK);
        assertSubtaskWithSummaryExists("Subtask 1");

    }

    private void assertSubtaskWithSummaryExists(final String summary)
    {
        assertThat.elementPresentByTimeout("jquery=.stsummary:contains('" + summary + "')", 5000);
    }
}
