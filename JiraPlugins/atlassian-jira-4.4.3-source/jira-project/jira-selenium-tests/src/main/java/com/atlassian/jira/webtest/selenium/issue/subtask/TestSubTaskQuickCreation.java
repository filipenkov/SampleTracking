package com.atlassian.jira.webtest.selenium.issue.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
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

    private static final String LINK_SHOW = "stqc_show";
    private static final String BUTTON_CANCEL = "stqc_cancel";
    private static final String BUTTON_SUBMIT = "stqc_submit";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestTimeTrackingAggregates.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testShowHideForm()
    {
        setupSTCForm(ISSUE_PARENT);
        assertSTLinkVisible();

        // show the form
        client.click(LINK_SHOW);
        assertSTFormVisible();

        // hide the form by "Cancel" button
        client.click(BUTTON_CANCEL);
        assertSTLinkVisible();
    }

    public void testShowHidePersistedInCookie()
    {
        // go to issue
        setupSTCForm(ISSUE_PARENT);
        assertSTLinkVisible();

        // show the form
        client.click(LINK_SHOW);
        assertSTFormVisible();

        // go to issue and assert form is still visible
        getNavigator().gotoIssue(ISSUE_PARENT);
        assertSTFormVisible();

        // hide the form
        client.click(BUTTON_CANCEL);
        assertSTLinkVisible();

        // go to issue and assert form is still hidden
        getNavigator().gotoIssue(ISSUE_PARENT);
        assertSTLinkVisible();
    }

    public void testCreateIssueAndGoBackToParent()
    {
        // go to issue
        setupSTCForm(ISSUE_PARENT);
        assertSTLinkVisible();

        // show the form
        client.click(LINK_SHOW);
        assertSTFormVisible();

        client.typeWithFullKeyEvents("summary", "This is a Selenium test issue");
        client.select("assignee", "Administrator");
        client.typeWithFullKeyEvents("timetracking", "8h");
        client.click(BUTTON_SUBMIT, true);

        assertThat.textPresent("This is a Selenium test issue");
        assertThat.textPresent("0%");

        // go to issue and assert form is still visible
        getNavigator().gotoIssue(ISSUE_PARENT);
        assertThat.textPresent("parent 1");
        assertSTFormVisible();
        // assert new sub-task is listed
        assertThat.textPresent("This is a Selenium test issue");
    }

    private void assertSTFormVisible()
    {
        assertSubTaskFormPresent();
        assertThat.elementVisible(BUTTON_CANCEL);
        assertThat.textPresent("Create Sub-Task");
        assertThat.elementVisible("summary");
        assertThat.elementVisible("issuetype");
        assertThat.elementVisible("assignee");
        assertThat.elementVisible("timetracking");
        assertThat.elementNotVisible(LINK_SHOW);
    }

    private void assertSTLinkVisible()
    {
        assertSubTaskFormPresent();
        assertThat.elementVisible(LINK_SHOW);
        assertThat.textPresent("Create Sub-Task");
        assertThat.elementNotVisible(BUTTON_CANCEL);
    }

    private void assertSubTaskFormPresent()
    {
        assertThat.elementPresent("subtask_container_vertical");
    }

    public void setupSTCForm(String issueKey)
    {
        getNavigator().gotoIssue(issueKey);

        String cookie = client.getCookie();
        if(cookie.indexOf("issue.stqc=1") >= 0)
        {
            client.click(BUTTON_CANCEL);
        }
    }

}
