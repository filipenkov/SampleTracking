package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser (browsers = { Browser.IE }) //Element not found - Responsibility: JIRA Team
@WebTest ({ Category.SELENIUM_TEST })
public class TestEmailOptionsUserPicker extends AbstractTestAjaxUserPicker
{
    public static Test suite()
    {
        return suiteFor(TestEmailOptionsUserPicker.class);
    }

    public void testNavReporterEmailSHOW()
    {
        setupEmailOptions("email_show");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker("reporter");
        assertTrue(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        client.click("issue-filter-submit");
    }

    public void testNavReporterEmailMask()
    {
        setupEmailOptions("email_mask");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker("reporter");
        assertTrue(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        client.click("issue-filter-submit");
    }

    public void testNavReporterEmailHidden()
    {
        setupEmailOptions("email_hide");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker("reporter");
        assertFalse(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        client.click("issue-filter-submit");
    }

    private void goToIssueNavSimpleExpanded()
    {
        globalPages().goToIssueNavigator().toSimpleMode().simpleSearch().expandAllSections();
    }

    private void setupEmailOptions(String link)
    {
        getNavigator().gotoAdmin();
        client.click("general_configuration", true);
        client.click("edit-app-properties", true);
        client.click(link);
        client.click("Update", true);
    }

    private void testUserGroupPicker(String fieldId)
    {
        client.select(fieldId + "Select", "label=" + "Specify User");
        commonPostiveACAsserts("searcher-" + fieldId);
    }
}
