package com.atlassian.jira.webtest.selenium.ajaxuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestEmailOptionsUserPicker extends AbstractTestAjaxUserPicker
{
    private static final String REPORTER = "reporter";

    public static Test suite()
    {
        return suiteFor(TestEmailOptionsUserPicker.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
    }

    public void testNavReporterEmailSHOW()
    {
        setupEmailOptions("show");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker(REPORTER);
        assertTrue(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        //submit the form so that we're not left with a half baked form!
        client.click("issue-filter-submit");
     }

    public void testNavReporterEmailMASK()
    {
        setupEmailOptions("mask");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker(REPORTER);
        assertTrue(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        //submit the form so that we're not left with a half baked form!
        client.click("issue-filter-submit");
    }

    public void testNavReporterEmailHIDDEN()
    {
        setupEmailOptions("hidden");
        goToIssueNavSimpleExpanded();
        testUserGroupPicker(REPORTER);
        assertFalse(client.getEval("window.jQuery('div.suggestions').text()").contains("@watermelon.com"));
        //submit the form so that we're not left with a half baked form!
        client.click("issue-filter-submit");
    }


    /*----------------------------------------------------------------------------*/

    private void goToIssueNavSimpleExpanded()
    {
        globalPages().goToIssueNavigator().toSimpleMode().simpleSearch().expandAllSections();
    }

    private void setupEmailOptions(String type) {
        getNavigator().gotoAdmin();
        client.click("general_configuration", true);
		client.click("edit-app-properties", true);
        if ("show".equalsIgnoreCase(type)) {
            client.click("email_show");
        }
        if ("hidden".equalsIgnoreCase(type)) {
            client.click("email_hide");
        }
        if ("mask".equalsIgnoreCase(type)) {
            client.click("email_mask");
        }
        if ("user".equalsIgnoreCase(type)) {
            client.click("email_user");
        }
        client.click("Update", true);
   }


    private void testUserGroupPicker(String fieldId)
    {
        toggleUserGroupSelect(fieldId, "Specify User");
        commonPostiveACAsserts("searcher-" + fieldId);
    }

    private void toggleUserGroupSelect(String fieldId, String label)
    {
        client.select(fieldId + "Select", "label=" + label);
    }


}
