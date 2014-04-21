package com.atlassian.jira.webtest.selenium.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;

/**
 * @since v4.0.1
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestBulkEdit extends JiraSeleniumTest
{
    // http://jira.atlassian.com/browse/JRA-10934 - clicking Assign To Me link or using User Picker should enable checkbox
    public void testCheckboxesOnChange()
    {
        restoreData("TestBulkEdit.xml");

        getNavigator().findIssuesWithJql("key = 'NDT-1'");

        client.click("//a[@id='toolOptions']/span");
        client.click("bulkedit_all");
        client.waitForPageToLoad();
        client.check("bulkedit_10000");
        client.clickButton("Next >>", true);
        client.check("operation", "bulk.edit.operation.name");
        client.clickButton("Next >>", true);

        // set assignee
        assertCheckbox("cbassignee", false);

        client.clickLinkWithText("Assign To Me", false);

        assertCheckbox("cbassignee", true);

        // set reporter
        assertCheckbox("cbreporter", false);

        client.click("//img[@name='reporterImage']");
        client.waitForPopUp("UserPicker", PAGE_LOAD_WAIT);
        client.selectWindow("UserPicker");
        client.click("jquery=#username_row_1 .user-name");
        client.selectWindow("null");

        assertCheckbox("cbreporter", true);

        client.typeWithFullKeyEvents("jquery=#labels-textarea", "newLabel");

        assertTrueByDefaultTimeout
                (
                        "The labels field dropdown did not contain the text 'newLable' entered by the user.",
                        jQuery("#labels-multi-select[data-query='newLabel']", this.context()).element().isPresent()
                );

        client.click("jquery=#user-inputted-option a");
        assertCheckbox("cblabels", true);

        //don't leave the form in a half baked state
        client.click("Next", true);
    }

    private void assertCheckbox(final String id, final boolean checked)
    {
        final String value = checked ? "on" : "off";
        assertEquals("Asserting that checkbox with id '" + id + "' has checked value '" + checked + "' FAILED.", value, client.getValue(id));
    }
}
