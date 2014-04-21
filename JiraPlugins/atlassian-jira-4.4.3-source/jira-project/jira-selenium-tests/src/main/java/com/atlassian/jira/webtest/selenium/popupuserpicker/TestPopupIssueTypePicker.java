package com.atlassian.jira.webtest.selenium.popupuserpicker;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

import java.util.Arrays;

/**
 *
 */
@WebTest({Category.SELENIUM_TEST })
public class TestPopupIssueTypePicker extends JiraSeleniumTest
{
    public static Test suite()
    {
        return suiteFor(TestPopupIssueTypePicker.class);
    }

    public void testPickerWithBlankIssueType()
    {
        restoreBlankInstance();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);

        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.type("name", "Blank");
        client.click("link=select image");
        client.waitForPopUp("IconPicker", PAGE_LOAD_WAIT);
        client.selectWindow("IconPicker");
        client.click("//tr[2]/td[2]");
        assertFalse("selecting an icon should close the filterpicker window",
                Arrays.asList(client.getAllWindowNames()).contains("IconPicker"));
        client.selectWindow("null");
        client.click("Add", true);
        assertThat.textPresent("Blank");
    }
}
